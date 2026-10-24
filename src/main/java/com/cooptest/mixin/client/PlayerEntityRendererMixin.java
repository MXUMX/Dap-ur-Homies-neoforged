package com.cooptest.mixin.client;

import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.cooptest.client.CoopAnimationHandler;
import com.cooptest.client.PlayerPoseSnapshot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void extractPose(Avatar avatar, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (!(avatar instanceof Player player)) {
            state.setRenderData(PlayerPoseSnapshot.KEY, null);
            return;
        }
        boolean playerVehicle = player.getVehicle() instanceof Player;
        state.setRenderData(PlayerPoseSnapshot.KEY, new PlayerPoseSnapshot(player.getUUID(), player.swinging,
                player.isUsingItem(), player.onGround(), player.isPassenger(), playerVehicle,
                playerVehicle ? player.getVehicle().getYRot() : player.getYRot()));
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("TAIL"))
    private void rotateGrabbedPlayer(AvatarRenderState state, PoseStack matrices, float bodyRot,
                                     float scale, CallbackInfo ci) {
        var snapshot = state.getRenderData(PlayerPoseSnapshot.KEY);
        if (snapshot == null || PoseNetworking.poseStates.getOrDefault(snapshot.id(), PoseState.NONE)
                != PoseState.GRABBED) return;
        var animation = CoopAnimationHandler.getAnimState(snapshot.id());
        if (animation == CoopAnimationHandler.AnimState.SPIN
                || animation == CoopAnimationHandler.AnimState.GROUND_POUND_DIVE) return;
        matrices.mulPose(Axis.YP.rotationDegrees(bodyRot - snapshot.holderYaw()));
        matrices.translate(0, 0.9, 0);
        matrices.mulPose(Axis.XP.rotationDegrees(90));
        matrices.translate(0, -0.9, 0);
    }
}
