package net.fabricmc.fabric.api.resource;

import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;

public final class ResourceManagerHelper {
    private static final List<SimpleSynchronousResourceReloadListener> CLIENT_LISTENERS = new ArrayList<>();

    private ResourceManagerHelper() {
    }

    public static ResourceManagerHelper get(PackType type) {
        return new ResourceManagerHelper();
    }

    public void registerReloadListener(SimpleSynchronousResourceReloadListener listener) {
        CLIENT_LISTENERS.add(listener);
    }

    public static void addReloadListeners(AddReloadListenerEvent event) {
        for (SimpleSynchronousResourceReloadListener listener : CLIENT_LISTENERS) {
            event.addListener(listener);
        }
    }
}
