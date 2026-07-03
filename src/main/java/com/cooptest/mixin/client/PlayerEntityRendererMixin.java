package com.cooptest.mixin.client;

import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {

    @Unique
    private static final HashMap<UUID, Boolean> matrixPushed = new HashMap<>();

    @Unique
    private static final HashMap<UUID, Float> lockedYaw = new HashMap<>();

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void rotateGrabbedPlayer(AbstractClientPlayer player, float yaw, float tickDelta,
                                     PoseStack matrices, MultiBufferSource vertexConsumers,
                                     int light, CallbackInfo ci) {
        PoseState pose = PoseNetworking.poseStates.getOrDefault(player.getUUID(), PoseState.NONE);

        if (pose == PoseState.GRABBED) {

            com.cooptest.client.CoopAnimationHandler.AnimState animState =
                    com.cooptest.client.CoopAnimationHandler.getAnimState(player.getUUID());
            if (animState == com.cooptest.client.CoopAnimationHandler.AnimState.SPIN
                    || animState == com.cooptest.client.CoopAnimationHandler.AnimState.GROUND_POUND_DIVE) {
                matrixPushed.put(player.getUUID(), false);
                return;
            }

            matrices.pushPose();

            float facingYaw;

            Entity vehicle = player.getVehicle();
            if (vehicle instanceof Player holder) {

                facingYaw = holder.getYRot();
                lockedYaw.put(player.getUUID(), facingYaw);
            } else {

                if (lockedYaw.containsKey(player.getUUID())) {
                    facingYaw = lockedYaw.get(player.getUUID());
                } else {

                    facingYaw = player.getYRot();
                    lockedYaw.put(player.getUUID(), facingYaw);
                }
            }

            float counterRotation = -yaw + facingYaw;

            matrices.mulPose(Axis.YP.rotationDegrees(counterRotation));

            matrices.translate(0, 0.9, 0);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            matrices.translate(0, -0.9, 0);

            matrixPushed.put(player.getUUID(), true);
        } else {
            lockedYaw.remove(player.getUUID());
            matrixPushed.put(player.getUUID(), false);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void restoreMatrix(AbstractClientPlayer player, float yaw, float tickDelta,
                               PoseStack matrices, MultiBufferSource vertexConsumers,
                               int light, CallbackInfo ci) {
        Boolean pushed = matrixPushed.get(player.getUUID());
        if (pushed != null && pushed) {
            matrices.popPose();
            matrixPushed.put(player.getUUID(), false);
        }
    }
}