package com.cooptest.mixin.client;

import com.cooptest.client.CoopImpactHandler;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class ImpactRendererMixin {
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void impactGeometry(LivingEntityRenderState state, boolean visible, boolean transparent,
                                 boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (state instanceof AvatarRenderState && CoopImpactHandler.playing && visible) {
            cir.setReturnValue(RenderTypes.debugQuads());
        }
    }

    @Inject(method = "getModelTint", at = @At("HEAD"), cancellable = true)
    private void impactColor(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (state instanceof AvatarRenderState && CoopImpactHandler.playing) {
            cir.setReturnValue(CoopImpactHandler.whiteFrame ? 0xFF000000 : 0xFFFFFFFF);
        }
    }

    @Inject(method = "shouldRenderLayers", at = @At("HEAD"), cancellable = true)
    private void impactLayers(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof AvatarRenderState && CoopImpactHandler.playing) cir.setReturnValue(false);
    }
}
