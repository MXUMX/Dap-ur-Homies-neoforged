package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ServerPlayNetworking {
    private ServerPlayNetworking() {
    }

    @FunctionalInterface
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void receive(T payload, Context context);
    }

    public record Context(ServerPlayer player, MinecraftServer server, IPayloadContext neoForgeContext) {
    }

    public static <T extends CustomPacketPayload> void registerGlobalReceiver(CustomPacketPayload.Type<T> id, PlayPayloadHandler<T> handler) {
        NeoForgePayloadBridge.registerServerReceiver(id, handler);
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
