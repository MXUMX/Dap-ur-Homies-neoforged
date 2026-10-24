package com.cooptest.mixin;

import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PlayerEntityPassengerOffsetMixin {

    @ModifyExpressionValue(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean allowForcedPlayerMount(boolean serializable, Entity vehicle, boolean force, boolean sendEvents) {
        // Players are not saved as entities, but cooperative grabs need them as vehicles.
        return serializable || (force && (Object) this instanceof Player && vehicle instanceof Player);
    }

    private static final double GRAB_OFFSET_FORWARD = 0.6;
    private static final double GRAB_OFFSET_UP      = 0.8;
    private static final double GRAB_OFFSET_RIGHT   = 3.0;

    @Inject(method = "getPassengerRidingPosition", at = @At("RETURN"), cancellable = true)
    private void customPassengerPosition(Entity passenger, CallbackInfoReturnable<Vec3> cir) {
        Entity vehicle = (Entity)(Object)this;
        if (!(vehicle instanceof Player holder)) return;
        if (!(passenger instanceof Player)) return;

        PoseState holderPose = PoseNetworking.poseStates.getOrDefault(holder.getUUID(), PoseState.NONE);
        if (holderPose == PoseState.GRAB_HOLDING) {
            Vec3 base = cir.getReturnValue();
            float yaw = holder.getYRot();
            double yawRad = Math.toRadians(-yaw);
            double rotX = GRAB_OFFSET_RIGHT * Math.cos(yawRad) + GRAB_OFFSET_FORWARD * Math.sin(yawRad);
            double rotZ = GRAB_OFFSET_RIGHT * Math.sin(yawRad) - GRAB_OFFSET_FORWARD * Math.cos(yawRad);
            cir.setReturnValue(new Vec3(base.x + rotX, base.y + GRAB_OFFSET_UP, base.z + rotZ));
        }
    }
}
