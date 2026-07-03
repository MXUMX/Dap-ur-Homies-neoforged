package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ServerPlayConnectionEvents {
    public static final Disconnect DISCONNECT = new Disconnect();

    private ServerPlayConnectionEvents() {
    }

    @FunctionalInterface
    public interface DisconnectCallback {
        void onDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server);
    }

    public static final class Disconnect {
        private final List<DisconnectCallback> callbacks = new CopyOnWriteArrayList<>();

        public void register(DisconnectCallback callback) {
            callbacks.add(callback);
        }

        public void invoke(ServerGamePacketListenerImpl handler, MinecraftServer server) {
            for (DisconnectCallback callback : callbacks) {
                callback.onDisconnect(handler, server);
            }
        }
    }
}
