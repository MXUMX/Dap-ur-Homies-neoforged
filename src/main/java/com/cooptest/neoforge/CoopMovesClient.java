package com.cooptest.neoforge;

import com.cooptest.TestCoopClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

final class CoopMovesClient {
    static void init(IEventBus modBus) {
        new TestCoopClient().onInitializeClient();
        modBus.addListener(KeyBindingHelper::registerNeoForgeKeyMappings);
        modBus.addListener(ResourceManagerHelper::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(CoopMovesClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(CoopMovesClient::onRenderGui);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        ClientTickEvents.END_CLIENT_TICK.invoke(Minecraft.getInstance());
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        HudRenderCallback.EVENT.invoke(event.getGuiGraphics(), event.getPartialTick());
    }
}
