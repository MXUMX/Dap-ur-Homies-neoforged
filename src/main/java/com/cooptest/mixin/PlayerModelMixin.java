package com.cooptest.mixin;
import com.cooptest.ArmPoseTracker;
import com.cooptest.GrabInputHandler;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {
    @Unique private static final float ANIMATION_SPEED = 0.2f;
    @Unique private static final float FAST_ANIMATION_SPEED = 0.35f;
    @Unique private static final float GRAB_READY_ARM_PITCH = -70f;
    @Unique private static final float GRAB_READY_ARM_YAW = 5f;
    @Unique private static final float GRAB_READY_ARM_ROLL = 0f;
    @Unique private static final float HOLD_RIGHT_ARM_PITCH = -110f;
    @Unique private static final float HOLD_RIGHT_ARM_YAW = -10f;
    @Unique private static final float HOLD_RIGHT_ARM_ROLL = 0f;
    @Unique private static final float HOLD_LEFT_ARM_PITCH = 10f;
    @Unique private static final float HOLD_LEFT_ARM_YAW = 10f;
    @Unique private static final float HOLD_LEFT_ARM_ROLL = 0f;
    @Unique private static final float CHARGE_RIGHT_ARM_PITCH = -150f;
    @Unique private static final float CHARGE_RIGHT_ARM_YAW = 10f;
    @Unique private static final float CHARGE_RIGHT_ARM_ROLL = 0f;
    @Unique private static final float CHARGE_LEFT_ARM_PITCH = -70f;
    @Unique private static final float CHARGE_LEFT_ARM_YAW = 30f;
    @Unique private static final float CHARGE_BODY_LEAN = -20f;
    @Unique private static final float THROW_RIGHT_ARM_PITCH = -130f;
    @Unique private static final float THROW_RIGHT_ARM_YAW = -5f;
    @Unique private static final float THROW_LEFT_ARM_PITCH = -20f;
    @Unique private static final float THROW_LEFT_ARM_YAW = 5f;
    @Unique private static final float THROW_BODY_LEAN = 25f;
    @Unique private static final float THROW_DURATION_MS = 350f;
    @Unique private static final float PUSH_IDLE_PITCH = -45f;
    @Unique private static final float PUSH_IDLE_YAW = -20f;
    @Unique private static final float PUSH_ACTION_PITCH = -90f;
    @Unique private static final float PUSH_ACTION_YAW = 0f;
    @Unique private static final float PUSH_ANIMATION_SPEED = 0.08f;
    @Unique private static final float SUPERMAN_HEAD_PITCH = -30f;
    @Unique private static final float SUPERMAN_RIGHT_ARM_PITCH = -180f;
    @Unique private static final float SUPERMAN_RIGHT_ARM_ROLL = -10f;
    @Unique private static final float SUPERMAN_LEFT_ARM_PITCH = 10f;
    @Unique private static final float SUPERMAN_LEFT_ARM_ROLL = 10f;
    @Unique private static final float SUPERMAN_LEG_ROLL = 5f;
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void injectPose(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        UUID playerId = player.getUUID();
        PoseState pose = PoseNetworking.poseStates.getOrDefault(playerId, PoseState.NONE);
        PoseState lastPose = ArmPoseTracker.lastPose.getOrDefault(playerId, PoseState.NONE);
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;
        ModelPart rightArm = model.rightArm;
        ModelPart leftArm = model.leftArm;
        ModelPart body = model.body;
        ModelPart head = model.head;
        ModelPart rightLeg = model.rightLeg;
        ModelPart leftLeg = model.leftLeg;
        float baseRightPitch = rightArm.xRot;
        float baseLeftPitch = leftArm.xRot;
        float baseRightYaw = rightArm.yRot;
        float baseLeftYaw = leftArm.yRot;
        boolean isSwinging = player.swinging;
        boolean isUsingItem = player.isUsingItem();
        if (pose == PoseState.GRABBED) {
            if (player.isPassenger() && !(player.getVehicle() instanceof Player)) {
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
            body.xRot = 0;
            body.yRot = 0;
            body.zRot = 0;
            head.xRot = (float) Math.toRadians(SUPERMAN_HEAD_PITCH);
            head.yRot = 0;
            head.zRot = 0;
            rightArm.xRot = (float) Math.toRadians(SUPERMAN_RIGHT_ARM_PITCH);
            rightArm.yRot = 0;
            rightArm.zRot = (float) Math.toRadians(SUPERMAN_RIGHT_ARM_ROLL);
            leftArm.xRot = (float) Math.toRadians(SUPERMAN_LEFT_ARM_PITCH);
            leftArm.yRot = 0;
            leftArm.zRot = (float) Math.toRadians(SUPERMAN_LEFT_ARM_ROLL);
            rightLeg.xRot = 0;
            rightLeg.yRot = 0;
            rightLeg.zRot = (float) Math.toRadians(SUPERMAN_LEG_ROLL);
            leftLeg.xRot = 0;
            leftLeg.yRot = 0;
            leftLeg.zRot = (float) Math.toRadians(-SUPERMAN_LEG_ROLL);
            model.rightSleeve.copyFrom(rightArm);
            model.leftSleeve.copyFrom(leftArm);
            model.rightPants.copyFrom(rightLeg);
            model.leftPants.copyFrom(leftLeg);
            model.jacket.copyFrom(body);
            model.hat.copyFrom(head);
            ArmPoseTracker.lastPose.put(playerId, pose);
            return;
        }
        if (pose == PoseState.NONE && !player.onGround() && !player.isPassenger()) {
            if (com.cooptest.GrabMechanic.isPlayerThrown(playerId)) {
                body.xRot = 0;
                body.yRot = 0;
                body.zRot = 0;
                head.xRot = (float) Math.toRadians(SUPERMAN_HEAD_PITCH);
                head.yRot = 0;
                head.zRot = 0;
                rightArm.xRot = (float) Math.toRadians(SUPERMAN_RIGHT_ARM_PITCH);
                rightArm.yRot = 0;
                rightArm.zRot = (float) Math.toRadians(SUPERMAN_RIGHT_ARM_ROLL);
                leftArm.xRot = (float) Math.toRadians(SUPERMAN_LEFT_ARM_PITCH);
                leftArm.yRot = 0;
                leftArm.zRot = (float) Math.toRadians(SUPERMAN_LEFT_ARM_ROLL);
                rightLeg.xRot = 0;
                rightLeg.yRot = 0;
                rightLeg.zRot = (float) Math.toRadians(SUPERMAN_LEG_ROLL);
                leftLeg.xRot = 0;
                leftLeg.yRot = 0;
                leftLeg.zRot = (float) Math.toRadians(-SUPERMAN_LEG_ROLL);
                model.rightSleeve.copyFrom(rightArm);
                model.leftSleeve.copyFrom(leftArm);
                model.rightPants.copyFrom(rightLeg);
                model.leftPants.copyFrom(leftLeg);
                model.jacket.copyFrom(body);
                model.hat.copyFrom(head);
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
        }
        if (pose == PoseState.GRAB_HOLDING) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPlayer = mc.player != null && mc.player.getUUID().equals(playerId);
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            if (!isLocalPlayer || !isFirstPerson) {
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
        }
        if (pose == PoseState.GRAB_READY) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPlayer = mc.player != null && mc.player.getUUID().equals(playerId);
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            if (!isLocalPlayer || !isFirstPerson) {
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
        }
        if (pose == PoseState.PUSH_IDLE || pose == PoseState.PUSH_ACTION || pose == PoseState.PUSH_RETURN) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPlayer = mc.player != null && mc.player.getUUID().equals(playerId);
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            if (!isLocalPlayer || !isFirstPerson) {
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
        }
        boolean hasHandRaised = com.cooptest.HighFiveHandler.hasHandRaised(playerId);
        if (hasHandRaised) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPlayer = mc.player != null && mc.player.getUUID().equals(playerId);
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            if (isLocalPlayer && isFirstPerson) {
                rightArm.xRot = (float) Math.toRadians(-100f);
                rightArm.yRot = (float) Math.toRadians(30f);
                rightArm.zRot = 0f;
                model.rightSleeve.copyFrom(rightArm);
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
        }
        if (isSwinging || isUsingItem) {
            if (pose != PoseState.GRAB_READY && pose != PoseState.GRAB_HOLDING &&
                    pose != PoseState.PUSH_IDLE && pose != PoseState.PUSH_ACTION) {
                return;
            }
        }
        float targetRightPitch = 0f;
        float targetLeftPitch = 0f;
        float targetRightYaw = 0f;
        float targetLeftYaw = 0f;
        float targetRightRoll = 0f;
        float targetLeftRoll = 0f;
        float targetBodyLean = 0f;
        float animSpeed = ANIMATION_SPEED;
        boolean useAbsoluteAngles = false;
        float directJitterPitch = 0f;
        float directJitterYaw = 0f;
        float directJitterRoll = 0f;
        Long throwStart = ArmPoseTracker.throwAnimationStart.get(playerId);
        boolean inThrowAnimation = false;
        float throwProgress = 0f;
        if (throwStart != null) {
            long elapsed = System.currentTimeMillis() - throwStart;
            if (elapsed < THROW_DURATION_MS) {
                inThrowAnimation = true;
                throwProgress = elapsed / THROW_DURATION_MS;
            } else {
                ArmPoseTracker.throwAnimationStart.remove(playerId);
            }
        }
        float chargeProgress = GrabInputHandler.getChargeProgressFor(playerId);
        boolean isCharging = chargeProgress >= 0f;
        if (inThrowAnimation) {
            animSpeed = FAST_ANIMATION_SPEED;
            useAbsoluteAngles = true;
            if (throwProgress < 0.4f) {
                float throwPhase = throwProgress / 0.4f;
                targetRightPitch = lerp(CHARGE_RIGHT_ARM_PITCH, THROW_RIGHT_ARM_PITCH, throwPhase);
                targetRightYaw = lerp(CHARGE_RIGHT_ARM_YAW, THROW_RIGHT_ARM_YAW, throwPhase);
                targetLeftPitch = lerp(CHARGE_LEFT_ARM_PITCH, THROW_LEFT_ARM_PITCH, throwPhase);
                targetLeftYaw = lerp(CHARGE_LEFT_ARM_YAW, THROW_LEFT_ARM_YAW, throwPhase);
                targetBodyLean = lerp(CHARGE_BODY_LEAN, THROW_BODY_LEAN, throwPhase);
            } else {
                float returnPhase = (throwProgress - 0.4f) / 0.6f;
                targetRightPitch = lerp(THROW_RIGHT_ARM_PITCH, 0f, returnPhase);
                targetRightYaw = lerp(THROW_RIGHT_ARM_YAW, 0f, returnPhase);
                targetLeftPitch = lerp(THROW_LEFT_ARM_PITCH, 0f, returnPhase);
                targetLeftYaw = lerp(THROW_LEFT_ARM_YAW, 0f, returnPhase);
                targetBodyLean = lerp(THROW_BODY_LEAN, 0f, returnPhase);
            }
            targetRightRoll = 0f;
            targetLeftRoll = 0f;
        } else if (pose == PoseState.GRAB_HOLDING && isCharging) {
            useAbsoluteAngles = true;
            animSpeed = ANIMATION_SPEED;
            targetRightPitch = lerp(HOLD_RIGHT_ARM_PITCH, CHARGE_RIGHT_ARM_PITCH, chargeProgress);
            targetRightYaw = lerp(HOLD_RIGHT_ARM_YAW, CHARGE_RIGHT_ARM_YAW, chargeProgress);
            targetRightRoll = lerp(HOLD_RIGHT_ARM_ROLL, CHARGE_RIGHT_ARM_ROLL, chargeProgress);
            targetLeftPitch = lerp(HOLD_LEFT_ARM_PITCH, CHARGE_LEFT_ARM_PITCH, chargeProgress);
            targetLeftYaw = lerp(HOLD_LEFT_ARM_YAW, CHARGE_LEFT_ARM_YAW, chargeProgress);
            targetLeftRoll = HOLD_LEFT_ARM_ROLL;
            targetBodyLean = lerp(0f, CHARGE_BODY_LEAN, chargeProgress);
        } else if (pose == PoseState.GRAB_HOLDING) {
            useAbsoluteAngles = true;
            targetRightPitch = HOLD_RIGHT_ARM_PITCH;
            targetRightYaw = HOLD_RIGHT_ARM_YAW;
            targetRightRoll = HOLD_RIGHT_ARM_ROLL;
            targetLeftPitch = HOLD_LEFT_ARM_PITCH;
            targetLeftYaw = HOLD_LEFT_ARM_YAW;
            targetLeftRoll = HOLD_LEFT_ARM_ROLL;
        } else if (pose == PoseState.GRAB_READY) {
            useAbsoluteAngles = true;
            targetRightPitch = GRAB_READY_ARM_PITCH;
            targetLeftPitch = GRAB_READY_ARM_PITCH;
            targetRightYaw = -GRAB_READY_ARM_YAW;
            targetLeftYaw = GRAB_READY_ARM_YAW;
            targetRightRoll = GRAB_READY_ARM_ROLL;
            targetLeftRoll = -GRAB_READY_ARM_ROLL;
        } else if (pose == PoseState.PUSH_ACTION) {
            useAbsoluteAngles = true;
            animSpeed = 1.0f;
            float pushProgress = com.cooptest.client.PushClientHandler.getPushAnimProgress(playerId);
            if (pushProgress >= 0 && pushProgress < 0.4f) {
                float phase = pushProgress / 0.4f;
                float eased = 1.0f - (1.0f - phase) * (1.0f - phase);
                targetRightPitch = lerp(PUSH_IDLE_PITCH, -110f, eased);
                targetLeftPitch = lerp(PUSH_IDLE_PITCH, -110f, eased);
                targetRightYaw = lerp(PUSH_IDLE_YAW, -5f, eased);
                targetLeftYaw = lerp(-PUSH_IDLE_YAW, 5f, eased);
            } else if (pushProgress >= 0.4f) {
                float phase = (pushProgress - 0.4f) / 0.6f;
                float eased = phase * phase;
                targetRightPitch = lerp(-110f, PUSH_IDLE_PITCH, eased);
                targetLeftPitch = lerp(-110f, PUSH_IDLE_PITCH, eased);
                targetRightYaw = lerp(-5f, PUSH_IDLE_YAW, eased);
                targetLeftYaw = lerp(5f, -PUSH_IDLE_YAW, eased);
            } else {
                targetRightPitch = PUSH_IDLE_PITCH;
                targetLeftPitch = PUSH_IDLE_PITCH;
                targetRightYaw = PUSH_IDLE_YAW;
                targetLeftYaw = -PUSH_IDLE_YAW;
            }
        } else if (pose == PoseState.PUSH_IDLE || pose == PoseState.PUSH_RETURN) {
            useAbsoluteAngles = true;
            animSpeed = PUSH_ANIMATION_SPEED;
            targetRightPitch = PUSH_IDLE_PITCH;
            targetLeftPitch = PUSH_IDLE_PITCH;
            targetRightYaw = PUSH_IDLE_YAW;
            targetLeftYaw = -PUSH_IDLE_YAW;
        } else if (com.cooptest.client.ChargedDapClientHandler.isPlayerCharging(playerId)) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            boolean isLocalPlayer = mc.player != null && mc.player.getUUID().equals(playerId);
            boolean isFirstPerson = mc.options.getCameraType().isFirstPerson();
            if (!isLocalPlayer || !isFirstPerson) {
                ArmPoseTracker.lastPose.put(playerId, pose);
                return;
            }
            useAbsoluteAngles = true;
            animSpeed = FAST_ANIMATION_SPEED;
            float chargePercent = com.cooptest.client.ChargedDapClientHandler.getPlayerChargePercent(playerId);
            float fireLevel = com.cooptest.client.ChargedDapClientHandler.getPlayerFireLevel(playerId);
            float basePitch = -70f;
            float baseYaw = -15f;
            float baseRoll = 5f;
            if (chargePercent > 0.5f) {
                float pullback = (chargePercent - 0.5f) * 2f;
                basePitch -= pullback * 10f;
                baseYaw -= pullback * 10f;
            }
            if (fireLevel > 0.05f) {
                basePitch -= fireLevel * 30f;
                baseYaw -= fireLevel * 35f;
                baseRoll += fireLevel * 20f;
            }
            targetRightPitch = basePitch;
            targetRightYaw = baseYaw;
            targetRightRoll = baseRoll;
            targetLeftPitch = 0f;
            targetLeftYaw = 0f;
            targetLeftRoll = 0f;
        } else if (com.cooptest.client.CatchClientHandler.getCatcherAnimProgress(playerId) >= 0) {
            useAbsoluteAngles = true;
            animSpeed = 1.0f;
            float progress = com.cooptest.client.CatchClientHandler.getCatcherAnimProgress(playerId);
            if (progress < 0.3f) {
                float phase = progress / 0.3f;
                float eased = phase * phase;
                targetRightPitch = lerp(GRAB_READY_ARM_PITCH, -30f, eased);
                targetLeftPitch = lerp(GRAB_READY_ARM_PITCH, -30f, eased);
                targetRightYaw = lerp(GRAB_READY_ARM_YAW, 40f, eased);
                targetLeftYaw = lerp(-GRAB_READY_ARM_YAW, -40f, eased);
            } else {
                float phase = (progress - 0.3f) / 0.7f;
                float eased = 1.0f - (1.0f - phase) * (1.0f - phase);
                targetRightPitch = lerp(-30f, 0f, eased);
                targetLeftPitch = lerp(-30f, 0f, eased);
                targetRightYaw = lerp(40f, 0f, eased);
                targetLeftYaw = lerp(-40f, 0f, eased);
            }
        } else if (com.cooptest.client.CatchClientHandler.getCaughtAnimProgress(playerId) >= 0) {
            useAbsoluteAngles = true;
            animSpeed = 1.0f;
            float progress = com.cooptest.client.CatchClientHandler.getCaughtAnimProgress(playerId);
            if (progress < 0.2f) {
                float phase = progress / 0.2f;
                targetRightPitch = lerp(0f, -20f, phase);
                targetLeftPitch = lerp(0f, -20f, phase);
                targetRightYaw = lerp(0f, -30f, phase);
                targetLeftYaw = lerp(0f, 30f, phase);
            } else {
                float phase = (progress - 0.2f) / 0.8f;
                float eased = phase * phase;
                targetRightPitch = lerp(-20f, 0f, eased);
                targetLeftPitch = lerp(-20f, 0f, eased);
                targetRightYaw = lerp(-30f, 0f, eased);
                targetLeftYaw = lerp(30f, 0f, eased);
            }
        } else {
            useAbsoluteAngles = false;
        }
        float currRightPitch = ArmPoseTracker.rightArmPitch.getOrDefault(playerId, 0f);
        float currLeftPitch = ArmPoseTracker.leftArmPitch.getOrDefault(playerId, 0f);
        float currRightYaw = ArmPoseTracker.rightArmYaw.getOrDefault(playerId, 0f);
        float currLeftYaw = ArmPoseTracker.leftArmYaw.getOrDefault(playerId, 0f);
        float currRightRoll = ArmPoseTracker.rightArmRoll.getOrDefault(playerId, 0f);
        float currLeftRoll = ArmPoseTracker.leftArmRoll.getOrDefault(playerId, 0f);
        float currBodyLean = ArmPoseTracker.bodyLean.getOrDefault(playerId, 0f);
        float targetRightPitchRad = (float) Math.toRadians(targetRightPitch);
        float targetLeftPitchRad = (float) Math.toRadians(targetLeftPitch);
        float targetRightYawRad = (float) Math.toRadians(targetRightYaw);
        float targetLeftYawRad = (float) Math.toRadians(targetLeftYaw);
        float targetRightRollRad = (float) Math.toRadians(targetRightRoll);
        float targetLeftRollRad = (float) Math.toRadians(targetLeftRoll);
        float targetBodyLeanRad = (float) Math.toRadians(targetBodyLean);
        currRightPitch += (targetRightPitchRad - currRightPitch) * animSpeed;
        currLeftPitch += (targetLeftPitchRad - currLeftPitch) * animSpeed;
        currRightYaw += (targetRightYawRad - currRightYaw) * animSpeed;
        currLeftYaw += (targetLeftYawRad - currLeftYaw) * animSpeed;
        currRightRoll += (targetRightRollRad - currRightRoll) * animSpeed;
        currLeftRoll += (targetLeftRollRad - currLeftRoll) * animSpeed;
        currBodyLean += (targetBodyLeanRad - currBodyLean) * animSpeed;
        ArmPoseTracker.rightArmPitch.put(playerId, currRightPitch);
        ArmPoseTracker.leftArmPitch.put(playerId, currLeftPitch);
        ArmPoseTracker.rightArmYaw.put(playerId, currRightYaw);
        ArmPoseTracker.leftArmYaw.put(playerId, currLeftYaw);
        ArmPoseTracker.rightArmRoll.put(playerId, currRightRoll);
        ArmPoseTracker.leftArmRoll.put(playerId, currLeftRoll);
        ArmPoseTracker.bodyLean.put(playerId, currBodyLean);
        if (useAbsoluteAngles) {
            rightArm.xRot = currRightPitch + (float) Math.toRadians(directJitterPitch);
            leftArm.xRot = currLeftPitch;
            rightArm.yRot = currRightYaw + (float) Math.toRadians(directJitterYaw);
            leftArm.yRot = currLeftYaw;
            rightArm.zRot = currRightRoll + (float) Math.toRadians(directJitterRoll);
            leftArm.zRot = currLeftRoll;
        } else {
            rightArm.xRot = baseRightPitch + currRightPitch;
            leftArm.xRot = baseLeftPitch + currLeftPitch;
            rightArm.yRot = baseRightYaw + currRightYaw;
            leftArm.yRot = baseLeftYaw + currLeftYaw;
            rightArm.zRot += currRightRoll;
            leftArm.zRot += currLeftRoll;
        }
        boolean shouldApplyBodyLean = inThrowAnimation || (pose == PoseState.GRAB_HOLDING && isCharging);
        if (shouldApplyBodyLean && Math.abs(currBodyLean) > 0.01f) {
            body.xRot += currBodyLean;
            model.jacket.copyFrom(body);
        }
        model.rightSleeve.copyFrom(rightArm);
        model.leftSleeve.copyFrom(leftArm);
        ArmPoseTracker.lastPose.put(playerId, pose);
    }
    @Unique
    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}