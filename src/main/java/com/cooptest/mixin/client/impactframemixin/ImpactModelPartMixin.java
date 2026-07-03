package com.cooptest.mixin.client.impactframemixin;

import com.cooptest.client.CoopImpactHandler;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelPart.class)
public abstract class ImpactModelPartMixin {
// WASTED 7 HR ON THIS DOSNT WORK AS INTNEDE
    private static boolean shouldFlash() {
        return CoopImpactHandler.playing && CoopImpactHandler.renderingPlayer;
    }

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0  // light
    )
    private int forceLight(int light) {
        return shouldFlash() ? 15728880 : light;
    }

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1  // overlay
    )
    private int forceOverlay(int overlay) {
        if (!shouldFlash()) return overlay;
        return CoopImpactHandler.whiteFrame ? OverlayTexture.NO_OVERLAY : 0;
    }

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumer wrapConsumer(VertexConsumer original) {
        if (!shouldFlash()) return original;
        final boolean white = CoopImpactHandler.whiteFrame;

        return new VertexConsumer() {
            @Override
            public VertexConsumer addVertex(float x, float y, float z) {
                original.addVertex(x, y, z);
                return this;
            }
            @Override
            public VertexConsumer setColor(int r, int g, int b, int a) {
                // whiteFrame=true  → screen WHITE → entity BLACK
                // whiteFrame=false → screen BLACK → entity WHITE (overlay=0 above handles it)
                return white ? original.setColor(0, 0, 0, 255)
                        : original.setColor(255, 255, 255, 255);
            }
            @Override
            public VertexConsumer setUv(float u, float v) {
                original.setUv(u, v);
                return this;
            }
            @Override
            public VertexConsumer setUv1(int u, int v) {
                original.setUv1(u, v);
                return this;
            }
            @Override
            public VertexConsumer setUv2(int u, int v) {
                original.setUv2(240, 240);
                return this;
            }
            @Override
            public VertexConsumer setNormal(float x, float y, float z) {
                original.setNormal(x, y, z);
                return this;
            }
        };
    }
}