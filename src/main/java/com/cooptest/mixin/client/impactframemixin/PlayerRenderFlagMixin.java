package com.cooptest.mixin.client.impactframemixin;

import com.cooptest.client.CoopImpactHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public class PlayerRenderFlagMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD")
    )
    private void onRenderStart(AbstractClientPlayer player, float yaw, float tickDelta,
                                PoseStack matrices, MultiBufferSource provider,
                                int light, CallbackInfo ci) {
        CoopImpactHandler.renderingPlayer = true;
    }

    @Inject(
            method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN")
    )
    private void onRenderEnd(AbstractClientPlayer player, float yaw, float tickDelta,
                              PoseStack matrices, MultiBufferSource provider,
                              int light, CallbackInfo ci) {
        CoopImpactHandler.renderingPlayer = false;
    }
}
