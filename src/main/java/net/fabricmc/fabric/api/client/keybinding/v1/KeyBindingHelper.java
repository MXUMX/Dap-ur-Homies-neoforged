package net.fabricmc.fabric.api.client.keybinding.v1;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class KeyBindingHelper {
    private static final List<KeyMapping> PENDING = new CopyOnWriteArrayList<>();

    private KeyBindingHelper() {
    }

    public static KeyMapping registerKeyBinding(KeyMapping keyBinding) {
        PENDING.add(keyBinding);
        return keyBinding;
    }

    public static void registerNeoForgeKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyBinding : PENDING) {
            event.register(keyBinding);
        }
    }
}
