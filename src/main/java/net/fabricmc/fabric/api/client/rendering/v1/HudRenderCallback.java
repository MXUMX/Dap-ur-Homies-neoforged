package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HudRenderCallback {
    public static final Event EVENT = new Event();

    private HudRenderCallback() {
    }

    @FunctionalInterface
    public interface Callback {
        void onHudRender(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter);
    }

    public static final class Event {
        private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

        public void register(Callback callback) {
            callbacks.add(callback);
        }

        public void invoke(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
            for (Callback callback : callbacks) {
                callback.onHudRender(drawContext, tickCounter);
            }
        }
    }
}
