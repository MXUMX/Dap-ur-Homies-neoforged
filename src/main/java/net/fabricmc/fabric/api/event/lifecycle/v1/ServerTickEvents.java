package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ServerTickEvents {
    public static final EndTick END_SERVER_TICK = new EndTick();

    private ServerTickEvents() {
    }

    @FunctionalInterface
    public interface EndTickCallback {
        void onEndTick(MinecraftServer server);
    }

    public static final class EndTick {
        private final List<EndTickCallback> callbacks = new CopyOnWriteArrayList<>();

        public void register(EndTickCallback callback) {
            callbacks.add(callback);
        }

        public void invoke(MinecraftServer server) {
            for (EndTickCallback callback : callbacks) {
                callback.onEndTick(server);
            }
        }
    }
}
