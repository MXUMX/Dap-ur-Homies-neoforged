package net.fabricmc.fabric.api.client.networking.v1;

import net.fabricmc.fabric.api.networking.v1.NeoForgePayloadBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPlayNetworking {
    private ClientPlayNetworking() {
    }

    @FunctionalInterface
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void receive(T payload, Context context);
    }

    public record Context(Minecraft client, IPayloadContext neoForgeContext) {
    }

    public static <T extends CustomPacketPayload> void registerGlobalReceiver(CustomPacketPayload.Type<T> id, PlayPayloadHandler<T> handler) {
        NeoForgePayloadBridge.registerClientReceiver(id, handler);
    }

    public static void send(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
