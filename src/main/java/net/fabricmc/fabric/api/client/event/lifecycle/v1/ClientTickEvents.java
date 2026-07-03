package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ClientTickEvents {
    public static final EndTick END_CLIENT_TICK = new EndTick();

    private ClientTickEvents() {
    }

    @FunctionalInterface
    public interface EndTickCallback {
        void onEndTick(Minecraft client);
    }

    public static final class EndTick {
        private final List<EndTickCallback> callbacks = new CopyOnWriteArrayList<>();

        public void register(EndTickCallback callback) {
            callbacks.add(callback);
        }

        public void invoke(Minecraft client) {
            for (EndTickCallback callback : callbacks) {
                callback.onEndTick(client);
            }
        }
    }
}
