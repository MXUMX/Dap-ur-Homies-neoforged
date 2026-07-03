package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.vertex.PoseStack;

public interface WorldRenderContext {
    PoseStack matrixStack();

    Camera camera();

    DeltaTracker tickCounter();
}
