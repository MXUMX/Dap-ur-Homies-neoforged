package com.cooptest;
import com.cooptest.client.GrabClientState;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class GrabInputHandler {
    private static KeyMapping grabKey;
    private static KeyMapping throwKey;
    private static KeyMapping shieldKey;
    private static boolean wasGrabKeyPressed = false;
    private static boolean wasThrowKeyPressed = false;
    private static boolean wasSneakPressed = false;
    private static boolean wasJumpPressed = false;
    private static boolean wasShieldKeyPressed = false;
    private static boolean wasOnGround = true;
    private static boolean spinWasActive = false;
    private static long    spinStopTime  = 0L;
    private static boolean isChargingThrow = false;
    private static long throwChargeStartTime = 0;
    private static final long MAX_CHARGE_TIME_MS = 1500;
    private static float lastSentChargeProgress = -1f;
    public static final Map<UUID, Boolean> clientShieldMode = new HashMap<>();
    public static void register() {
        grabKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coopmoves.grab", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.coopmoves"
        ));
        throwKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coopmoves.throw", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, "category.coopmoves"
        ));
        shieldKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coopmoves.shield", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.coopmoves"
        ));
        ClientPlayNetworking.registerGlobalReceiver(GrabMechanic.ShieldModePayload.ID,
                (payload, context) -> {
                    context.client().execute(() -> {
                        clientShieldMode.put(payload.holderId(), payload.enabled());
                        if (!payload.enabled()) {
                            clientShieldMode.remove(payload.holderId());
                        }
                    });
                }
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            UUID playerId = client.player.getUUID();
            PoseState pose = PoseNetworking.poseStates.getOrDefault(playerId, PoseState.NONE);
            boolean isJumpPressed = client.options.keyJump.isDown();
            if (isJumpPressed && !wasJumpPressed) {
                if (pose == PoseState.GRABBED && !client.player.isPassenger()) {
                    if (client.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem()
                            instanceof net.minecraft.world.item.ElytraItem) {
                        ClientPlayNetworking.send(new GrabNetworking.ElytraBoostRequestPayload());
                    }
                }
            }
            wasJumpPressed = isJumpPressed;
            if (pose == PoseState.GRABBED && !client.player.isPassenger()) {
                float forward = 0f;
                float strafe = 0f;
                if (client.options.keyUp.isDown()) forward += 1f;
                if (client.options.keyDown.isDown()) forward -= 1f;
                if (client.options.keyLeft.isDown()) strafe += 1f;
                if (client.options.keyRight.isDown()) strafe -= 1f;
                if (Math.abs(forward) > 0.01f || Math.abs(strafe) > 0.01f) {
                    ClientPlayNetworking.send(new GrabNetworking.AirMovementPayload(forward, strafe));
                }
            }
            boolean isHolding = pose == PoseState.GRAB_HOLDING || GrabClientState.isHolding(playerId);
            boolean isBeingHeld = (pose == PoseState.GRABBED && client.player.isPassenger())
                    || GrabClientState.isBeingHeld(playerId);
            boolean isGrabKeyPressed = grabKey.isDown();
            if (isGrabKeyPressed && !wasGrabKeyPressed) {
                if (isHolding) {
                    ClientPlayNetworking.send(new GrabNetworking.DropRequestPayload());
                } else if (pose == PoseState.GRAB_READY) {
                    PoseNetworking.poseStates.put(playerId, PoseState.NONE);
                    PoseNetworking.sendPoseToServer(playerId, PoseState.NONE);
                } else if (pose == PoseState.NONE && handsEmpty(client)) {
                    PoseNetworking.poseStates.put(playerId, PoseState.GRAB_READY);
                    PoseNetworking.sendPoseToServer(playerId, PoseState.GRAB_READY);
                }
            }
            wasGrabKeyPressed = isGrabKeyPressed;
            boolean isShieldKeyPressed = shieldKey.isDown();
            if (isShieldKeyPressed && !wasShieldKeyPressed) {
                if (isHolding) {
                    ClientPlayNetworking.send(new GrabNetworking.ShieldTogglePayload());
                } else {
                    boolean blocked = isBeingHeld
                            || pose == PoseState.GRAB_READY
                            || pose == PoseState.PUSH_IDLE
                            || com.cooptest.client.ChargedDapClientHandler.isLocalPlayerCharging()
                            || com.cooptest.client.HighFiveClientHandler.isLocalPlayerInHighFive();
                    if (!blocked && com.cooptest.CoopMovesConfig.get().enableClap
                            && client.player.getMainHandItem().isEmpty()) {
                        com.cooptest.client.FirstPersonAnimationTest.showBothHands();
                        ClientPlayNetworking.send(new com.cooptest.ClapHandler.ClapRequestPayload());
                    }
                }
            }
            wasShieldKeyPressed = isShieldKeyPressed;
            boolean isSneakPressed = client.options.keyShift.isDown();
            boolean sneakJustPressed  = isSneakPressed  && !wasSneakPressed;
            boolean sneakJustReleased = !isSneakPressed && wasSneakPressed;
            boolean isThrownAirborne = (pose == PoseState.GRABBED)
                    && !client.player.isPassenger()
                    && !client.player.onGround();
            boolean isHelicopterMode = isThrownAirborne
                    && com.cooptest.client.SpinClientHandler.isLocalPlayerSpinning()
                    && client.player.getFirstPassenger() != null;
            if (isHelicopterMode && !com.cooptest.client.GroundPoundClientHandler.isLocalPlayerDiving()) {
                if (sneakJustReleased) {
                    spinWasActive = false;
                    ClientPlayNetworking.send(new com.cooptest.SpinHandler.SpinStopPayload());
                    com.cooptest.client.SpinClientHandler.forceStopLocalSpin();
                    ClientPlayNetworking.send(new com.cooptest.GroundPoundHandler.GroundPoundStartPayload());
                }
            } else if (isThrownAirborne && !com.cooptest.client.GroundPoundClientHandler.isLocalPlayerDiving()) {
                if (sneakJustReleased && com.cooptest.client.SpinClientHandler.isLocalPlayerSpinning()) {
                    spinWasActive = true;
                    spinStopTime  = System.currentTimeMillis();
                    ClientPlayNetworking.send(new com.cooptest.SpinHandler.SpinStopPayload());
                    com.cooptest.client.SpinClientHandler.forceStopLocalSpin();
                }
                if (sneakJustPressed) {
                    if (com.cooptest.client.SpinClientHandler.isLocalPlayerSpinning()) {
                        spinWasActive = false;
                        ClientPlayNetworking.send(new com.cooptest.SpinHandler.SpinStopPayload());
                        com.cooptest.client.SpinClientHandler.forceStopLocalSpin();
                        ClientPlayNetworking.send(new com.cooptest.GroundPoundHandler.GroundPoundStartPayload());
                    } else if (spinWasActive && System.currentTimeMillis() - spinStopTime < 300L) {
                        spinWasActive = false;
                        ClientPlayNetworking.send(new com.cooptest.GroundPoundHandler.GroundPoundStartPayload());
                    } else {
                        spinWasActive = false;
                        ClientPlayNetworking.send(new com.cooptest.SpinHandler.SpinStartPayload());
                    }
                }
            } else if (isThrownAirborne && com.cooptest.client.GroundPoundClientHandler.isLocalPlayerDiving()) {
            } else {
                if (sneakJustPressed && isBeingHeld) {
                    ClientPlayNetworking.send(new GrabNetworking.EscapeRequestPayload());
                }
                if (!isThrownAirborne) spinWasActive = false;
            }
            wasSneakPressed = isSneakPressed;
            boolean isThrowKeyPressed = throwKey.isDown();
            boolean justThrowPressed  = isThrowKeyPressed && !wasThrowKeyPressed;
            boolean justThrowReleased = !isThrowKeyPressed && wasThrowKeyPressed;
            if (isHolding) {
                com.cooptest.client.KickClientHandler.cancelIfCharging();
                if (justThrowPressed) {
                    isChargingThrow = true;
                    throwChargeStartTime = System.currentTimeMillis();
                    lastSentChargeProgress = 0f;
                    com.cooptest.client.CoopAnimationHandler.startGrabCharge(client.player);
                } else if (isThrowKeyPressed && isChargingThrow) {
                    float currentProgress = getThrowChargeProgress();
                    GrabClientState.setChargeProgress(playerId, currentProgress);
                    if (Math.abs(currentProgress - lastSentChargeProgress) >= 0.1f) {
                        PoseNetworking.sendChargeProgress(playerId, currentProgress);
                        lastSentChargeProgress = currentProgress;
                    }
                } else if (justThrowReleased && isChargingThrow) {
                    long chargeTime = System.currentTimeMillis() - throwChargeStartTime;
                    float power = Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME_MS);
                    ClientPlayNetworking.send(new GrabNetworking.ThrowRequestPayload(power));
                    isChargingThrow = false;
                    com.cooptest.client.CoopAnimationHandler.playThrowAnimation(client.player);
                    ArmPoseTracker.throwAnimationStart.put(playerId, System.currentTimeMillis());
                    PoseNetworking.sendThrowAnimation(playerId);
                    GrabClientState.setChargeProgress(playerId, 0f);
                    PoseNetworking.sendChargeProgress(playerId, -1f);
                    lastSentChargeProgress = -1f;
                }
            } else {
                if (isChargingThrow) {
                    GrabClientState.setChargeProgress(playerId, 0f);
                    PoseNetworking.sendChargeProgress(playerId, -1f);
                    lastSentChargeProgress = -1f;
                    isChargingThrow = false;
                }
                boolean canKick = !client.player.isPassenger() && pose == PoseState.NONE;
                if (canKick) {
                    com.cooptest.client.KickClientHandler.handleKickTick(
                            client, isThrowKeyPressed, client.player.isSprinting()
                    );
                } else {
                    com.cooptest.client.KickClientHandler.cancelIfCharging();
                }
            }
            wasThrowKeyPressed = isThrowKeyPressed;
            if (pose == PoseState.GRAB_READY && !handsEmpty(client)) {
                PoseNetworking.poseStates.put(playerId, PoseState.NONE);
                PoseNetworking.sendPoseToServer(playerId, PoseState.NONE);
            }
        });
    }
    private static boolean handsEmpty(Minecraft client) {
        return client.player.getMainHandItem().isEmpty();
    }
    public static float getThrowChargeProgress() {
        if (!isChargingThrow) return -1f;
        long chargeTime = System.currentTimeMillis() - throwChargeStartTime;
        return Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME_MS);
    }
    public static float getChargeProgressFor(UUID playerId) {
        @SuppressWarnings("resource")
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.player.getUUID().equals(playerId)) {
            return getThrowChargeProgress();
        }
        return PoseNetworking.chargeProgress.getOrDefault(playerId, -1f);
    }
}