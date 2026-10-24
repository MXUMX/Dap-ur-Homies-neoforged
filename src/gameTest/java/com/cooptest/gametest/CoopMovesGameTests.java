package com.cooptest.gametest;

import com.cooptest.GrabMechanic;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("coopmoves_tests")
public final class CoopMovesGameTests {
    public CoopMovesGameTests(IEventBus bus) {
        DeferredRegister<Consumer<GameTestHelper>> functions =
                DeferredRegister.create(Registries.TEST_FUNCTION, "coopmoves_tests");
        functions.register("grab_dismount", () -> CoopMovesGameTests::grabDismount);
        functions.register(bus);
    }

    private static void grabDismount(GameTestHelper helper) {
        var level = helper.getLevel();
        var server = level.getServer();
        var holder = testPlayer(level, "grab-holder");
        var held = testPlayer(level, "grab-rider");
        holder.setPos(0, 100, 0);
        held.setPos(1, 100, 0);
        var players = server.getPlayerList().getPlayersByUUID();
        players.put(holder.getUUID(), holder);
        players.put(held.getUUID(), held);
        try {
            // Ordinary vanilla mounting must remain blocked for unsaved player vehicles.
            check(helper, !held.startRiding(holder), "Unforced player mounting should remain blocked");
            PoseNetworking.poseStates.put(holder.getUUID(), PoseState.GRAB_READY);
            check(helper, GrabMechanic.tryGrab(holder, held), "Grab should mount a player on another player");
            check(helper, held.getVehicle() == holder, "Grabbed player must have the holder as vehicle");
            check(helper, GrabMechanic.isHolding(holder), "Holder state must be active");
            held.stopRiding();
            GrabMechanic.tick(server);
            check(helper, !GrabMechanic.isHolding(holder), "Dismount must clear holder state");
            check(helper, !GrabMechanic.isBeingHeld(held), "Dismount must clear grabbed state");
            check(helper, PoseNetworking.poseStates.get(holder.getUUID()) == PoseState.NONE,
                    "Dismount must reset the holder pose");
            check(helper, PoseNetworking.poseStates.get(held.getUUID()) == PoseState.NONE,
                    "Dismount must reset the grabbed player pose");
        } finally {
            held.stopRiding();
            GrabMechanic.forceRelease(holder.getUUID());
            GrabMechanic.forceRelease(held.getUUID());
            players.remove(holder.getUUID());
            players.remove(held.getUUID());
        }
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(Component.literal(message));
    }

    private static ServerPlayer testPlayer(ServerLevel level, String name) {
        var profile = new GameProfile(UUID.randomUUID(), name);
        var player = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        // Use real ServerPlayer mounting behavior with a connection that discards outbound packets.
        player.connection = new FakePlayer(level, profile).connection;
        return player;
    }
}
