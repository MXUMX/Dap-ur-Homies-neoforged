package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WorldRenderEvents {
    public static final AfterTranslucent AFTER_TRANSLUCENT = new AfterTranslucent();

    private WorldRenderEvents() {
    }

    @FunctionalInterface
    public interface AfterTranslucentCallback {
        void afterTranslucent(WorldRenderContext context);
    }

    public static final class AfterTranslucent {
        private final List<AfterTranslucentCallback> callbacks = new CopyOnWriteArrayList<>();

        public void register(AfterTranslucentCallback callback) {
            callbacks.add(callback);
        }

        public void invoke(WorldRenderContext context) {
            for (AfterTranslucentCallback callback : callbacks) {
                callback.afterTranslucent(context);
            }
        }
    }
}
