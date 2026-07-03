package com.cooptest;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;
public class MarioJumpHandler {
    private static final Map<UUID, Long> jumpCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 500;
    private static final Map<UUID, Long> marioAnimEnd = new HashMap<>();
    private static final Map<UUID, Long> popAnimEnd = new HashMap<>();
    private static final long MARIO_ANIM_DURATION_MS = 500;
    private static final long POP_ANIM_DURATION_MS = 417;
    private static final double LAUNCH_VELOCITY = 0.68;
    public record MarioJumpRequestPayload() implements CustomPacketPayload {
        public static final Type<MarioJumpRequestPayload> ID =
                new Type<>(ResourceLocation.fromNamespaceAndPath("testcoop", "mario_jump_request"));
        public static final StreamCodec<FriendlyByteBuf, MarioJumpRequestPayload> CODEC =
                StreamCodec.unit(new MarioJumpRequestPayload());
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(MarioJumpRequestPayload.ID, MarioJumpRequestPayload.CODEC);
    }
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(MarioJumpRequestPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> onMarioJumpRequest(player));
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> marioIt = marioAnimEnd.entrySet().iterator();
            while (marioIt.hasNext()) {
                Map.Entry<UUID, Long> entry = marioIt.next();
                if (now >= entry.getValue()) {
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player != null) {
                        PoseNetworking.broadcastAnimState(player, 0);
                    }
                    marioIt.remove();
                }
            }
            Iterator<Map.Entry<UUID, Long>> popIt = popAnimEnd.entrySet().iterator();
            while (popIt.hasNext()) {
                Map.Entry<UUID, Long> entry = popIt.next();
                if (now >= entry.getValue()) {
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player != null) {
                        PoseNetworking.broadcastAnimState(player, 0);
                    }
                    popIt.remove();
                }
            }
        });
    }
    private static void onMarioJumpRequest(ServerPlayer jumper) {
        if (jumper == null) return;
        UUID jumperId = jumper.getUUID();
        long now = System.currentTimeMillis();
        if (jumpCooldown.containsKey(jumperId)) {
            if (now - jumpCooldown.get(jumperId) < COOLDOWN_MS) {
                return;
            }
        }
        if (HighFiveHandler.isInBlockingState(jumperId)) {
            return;
        }
        ServerPlayer target = findPlayerBelow(jumper);
        if (target == null) {
            return;
        }
        executeMarioJump(jumper, target);
        jumpCooldown.put(jumperId, now);
    }
    private static ServerPlayer findPlayerBelow(ServerPlayer jumper) {
        ServerLevel world = jumper.serverLevel();
        Vec3 jumperPos = jumper.position();
        double jumperFeetY = jumperPos.y;
        AABB searchBox = new AABB(
                jumperPos.x - 0.8, jumperPos.y - 2.5, jumperPos.z - 0.8,
                jumperPos.x + 0.8, jumperPos.y + 0.5, jumperPos.z + 0.8
        );
        List<ServerPlayer> nearby = world.getEntitiesOfClass(
                ServerPlayer.class, searchBox,
                p -> p != jumper && p.isAlive()
        );
        for (ServerPlayer target : nearby) {
            Vec3 targetPos = target.position();
            double targetHeadY = targetPos.y + target.getEyeHeight() + 0.15;
            double heightDiff = jumperFeetY - targetHeadY;
            if (heightDiff >= -0.35 && heightDiff <= 0.5) {
                double horizDist = Math.sqrt(
                        Math.pow(jumperPos.x - targetPos.x, 2) +
                                Math.pow(jumperPos.z - targetPos.z, 2)
                );
                if (horizDist <= 0.7) {
                    return target;
                }
            }
        }
        return null;
    }
    private static void executeMarioJump(ServerPlayer jumper, ServerPlayer target) {
        ServerLevel world = jumper.serverLevel();
        Vec3 pos = jumper.position();
        long now = System.currentTimeMillis();
        System.out.println("[MARIO JUMP] Executing mario jump!");
        System.out.println("[MARIO JUMP] Jumper: " + jumper.getName().getString() + " (UUID: " + jumper.getUUID() + ")");
        System.out.println("[MARIO JUMP] Target: " + target.getName().getString() + " (UUID: " + target.getUUID() + ")");
        System.out.println("[MARIO JUMP] Broadcasting MARIO_JUMP (ordinal 30) to jumper");
        System.out.println("[MARIO JUMP] Broadcasting POP (ordinal 31) to target");
        Vec3 velocity = jumper.getDeltaMovement();
        jumper.setDeltaMovement(velocity.x, LAUNCH_VELOCITY, velocity.z);
        jumper.hurtMarked = true;
        PoseNetworking.broadcastAnimState(jumper, 30);
        PoseNetworking.broadcastAnimState(target, 31);
        System.out.println("[MARIO JUMP] Animations broadcast complete IT WORKING FINALLY!");
        marioAnimEnd.put(jumper.getUUID(), now + MARIO_ANIM_DURATION_MS);
        popAnimEnd.put(target.getUUID(), now + POP_ANIM_DURATION_MS);
        world.playSound(null, pos.x, pos.y, pos.z,
                ModSounds.MARIO_JUMP, SoundSource.PLAYERS, 1.0f, 1.0f);
        jumper.displayClientMessage(net.minecraft.network.chat.Component.literal("§a WAHOO!"), true);
        target.displayClientMessage(net.minecraft.network.chat.Component.literal("§c BONK!"), true);
    }
    public static void cleanup(UUID playerId) {
        jumpCooldown.remove(playerId);
        marioAnimEnd.remove(playerId);
        popAnimEnd.remove(playerId);
    }
}