package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NeoForgePayloadBridge {
    private static final Map<CustomPacketPayload.Type<?>, Entry<?>> ENTRIES = new LinkedHashMap<>();
    private static final Map<CustomPacketPayload.Type<?>, ServerPlayNetworking.PlayPayloadHandler<?>> SERVER_RECEIVERS = new LinkedHashMap<>();
    private static final Map<CustomPacketPayload.Type<?>, ClientPlayNetworking.PlayPayloadHandler<?>> CLIENT_RECEIVERS = new LinkedHashMap<>();

    private NeoForgePayloadBridge() {
    }

    public static synchronized <B extends FriendlyByteBuf, T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> id,
                                                                       StreamCodec<B, T> codec,
                                                                       boolean serverbound) {
        Entry<T> entry = getOrCreate(id, codec);
        if (serverbound) {
            if (entry.serverbound) throw new IllegalStateException("Duplicate serverbound payload " + id);
            entry.serverbound = true;
        } else {
            if (entry.clientbound) throw new IllegalStateException("Duplicate clientbound payload " + id);
            entry.clientbound = true;
        }
    }

    public static synchronized <T extends CustomPacketPayload> void registerServerReceiver(CustomPacketPayload.Type<T> id,
                                                                                    ServerPlayNetworking.PlayPayloadHandler<T> handler) {
        SERVER_RECEIVERS.put(id, handler);
    }

    public static synchronized <T extends CustomPacketPayload> void registerClientReceiver(CustomPacketPayload.Type<T> id,
                                                                                    ClientPlayNetworking.PlayPayloadHandler<T> handler) {
        CLIENT_RECEIVERS.put(id, handler);
    }

    public static synchronized void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        for (Entry<?> entry : ENTRIES.values()) {
            registerEntry(registrar, entry);
        }
    }

    @SuppressWarnings("unchecked")
    private static <B extends FriendlyByteBuf, T extends CustomPacketPayload> Entry<T> getOrCreate(CustomPacketPayload.Type<T> id, StreamCodec<B, T> codec) {
        Entry<?> existing = ENTRIES.get(id);
        if (existing != null) {
            return (Entry<T>) existing;
        }
        Entry<T> entry = new Entry<>(id, codec);
        ENTRIES.put(id, entry);
        return entry;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends CustomPacketPayload> void registerEntry(PayloadRegistrar registrar, Entry<T> entry) {
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec = (StreamCodec) entry.codec;
        if (entry.serverbound && entry.clientbound) {
            registrar.playBidirectional(entry.id, codec, NeoForgePayloadBridge::handleBidirectional);
        } else if (entry.serverbound) {
            registrar.playToServer(entry.id, codec, NeoForgePayloadBridge::handleServerbound);
        } else if (entry.clientbound) {
            registrar.playToClient(entry.id, codec, NeoForgePayloadBridge::handleClientbound);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void handleServerbound(T payload, IPayloadContext context) {
        ServerPlayNetworking.PlayPayloadHandler<T> handler =
                (ServerPlayNetworking.PlayPayloadHandler<T>) SERVER_RECEIVERS.get(payload.type());
        if (handler != null && context.player() instanceof ServerPlayer player) {
            handler.receive(payload, new ServerPlayNetworking.Context(player, player.getServer(), context));
        }
    }

    private static <T extends CustomPacketPayload> void handleBidirectional(T payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer) {
            handleServerbound(payload, context);
        } else {
            handleClientbound(payload, context);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends CustomPacketPayload> void handleClientbound(T payload, IPayloadContext context) {
        ClientPlayNetworking.PlayPayloadHandler<T> handler =
                (ClientPlayNetworking.PlayPayloadHandler<T>) CLIENT_RECEIVERS.get(payload.type());
        if (handler != null) {
            handler.receive(payload, new ClientPlayNetworking.Context(Minecraft.getInstance(), context));
        }
    }

    private static final class Entry<T extends CustomPacketPayload> {
        private final CustomPacketPayload.Type<T> id;
        private final StreamCodec<? extends FriendlyByteBuf, T> codec;
        private boolean serverbound;
        private boolean clientbound;

        private <B extends FriendlyByteBuf> Entry(CustomPacketPayload.Type<T> id, StreamCodec<B, T> codec) {
            this.id = id;
            this.codec = codec;
        }
    }
}
