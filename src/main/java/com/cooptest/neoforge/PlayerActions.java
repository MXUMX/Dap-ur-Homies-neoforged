package com.cooptest.neoforge;

import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;

public final class PlayerActions {
    private PlayerActions() {}

    public static void message(Player player, Component text, boolean overlay) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(text, overlay);
        } else if (overlay) {
            player.sendOverlayMessage(text);
        } else {
            player.sendSystemMessage(text);
        }
    }

    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z,
                                float yaw, float pitch) {
        teleport(player, level, x, y, z, Set.of(), yaw, pitch);
    }

    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z,
                                Set<Relative> relatives, float yaw, float pitch) {
        player.teleportTo(level, x, y, z, relatives, yaw, pitch, true);
    }

    public static void teleport(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(x, y, z);
    }
}
