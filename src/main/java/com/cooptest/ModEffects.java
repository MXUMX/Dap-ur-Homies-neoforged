package com.cooptest;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "testcoop");

    public static final DeferredHolder<MobEffect, MahitoEffect> MAHITO = EFFECTS.register("mahito", MahitoEffect::new);

    public static void registerNeoForge(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }

    public static void register() {
    }
}
