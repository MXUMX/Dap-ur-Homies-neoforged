package com.cooptest;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class MahitoItems {
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, "testcoop");

    public static final DeferredHolder<Potion, Potion> MAHITO_POTION = POTIONS.register(
            "mahito_stuff",
            () -> new Potion(new MobEffectInstance(ModEffects.MAHITO, 1200, 0))
    );

    public static void registerNeoForge(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(content -> {
            ItemStack potionStack = new ItemStack(Items.POTION);
            potionStack.set(DataComponents.POTION_CONTENTS,
                    new PotionContents(MAHITO_POTION));
            content.accept(potionStack);
        });
    }


    public static ItemStack createMahitoPotion() {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS,
                new PotionContents(MAHITO_POTION));
        return stack;
    }
}
