package com.cooptest.mixin;

import com.cooptest.MahitoCraftingHandler;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CraftingMenu.class)
public abstract class CraftingScreenMixin extends net.minecraft.world.inventory.AbstractCraftingMenu {
    protected CraftingScreenMixin(net.minecraft.world.inventory.MenuType<?> type, int id, int width, int height) {
        super(type, id, width, height);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"), cancellable = true)
    private void onCraftingChanged(net.minecraft.world.Container inventory, CallbackInfo ci) {
        if (MahitoCraftingHandler.isValidMahitoRecipe(craftSlots)) {
            resultSlots.setItem(0, MahitoCraftingHandler.createResult());
            ci.cancel();
        }
    }
}
