package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class PlayerLookup {
    private PlayerLookup() {
    }

    public static Iterable<ServerPlayer> tracking(Entity entity) {
        if (entity.getServer() == null) {
            return List.of();
        }
        return entity.getServer().getPlayerList().getPlayers();
    }

    public static Iterable<ServerPlayer> all(MinecraftServer server) {
        if (server == null) {
            return List.of();
        }
        return server.getPlayerList().getPlayers();
    }

    public static Iterable<ServerPlayer> around(ServerLevel level, Vec3 pos, double radius) {
        if (level == null) {
            return List.of();
        }
        double radiusSqr = radius * radius;
        return level.players().stream()
                .filter(player -> player.distanceToSqr(pos) <= radiusSqr)
                .toList();
    }
}
