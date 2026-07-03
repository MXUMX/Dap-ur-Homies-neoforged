package com.cooptest;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
public class PushInteractionHandler {
    private static final float  PUSH_RANGE       = 2.5f;
    private static final long   HOLD_REQUIRED_MS = 1500L;
    private static final long   READY_WINDOW_MS  = 3000L;
    private static final long   COOLDOWN_MS      = 1500L;
    private static final long   PUSH_IMMUNITY_MS = 500L;
    private static final long   JUMP_WINDOW_MS   = 800L;
    private static final double VEL_LOW    = 0.5;
    private static final double VEL_MEDIUM = 1.8;
    private static final double VEL_HIGH   = 3.5;
    private static final HashMap<UUID, UUID> holdTarget  = new HashMap<>();
    private static final HashMap<UUID, Long> holdStart   = new HashMap<>();
    private static final HashMap<UUID, UUID> readyPushers = new HashMap<>();
    private static final HashMap<UUID, Long> readyStart   = new HashMap<>();
    private static final HashMap<UUID, Long> cooldowns    = new HashMap<>();
    public  static final HashMap<UUID, Long> pushImmunity = new HashMap<>();
    public  static final HashMap<UUID, Long> lastJumpTime = new HashMap<>();
    public static final ResourceLocation PUSH_ANIM_ID = ResourceLocation.fromNamespaceAndPath("cooptest", "push_anim");
    public record PushAnimPayload(UUID playerId) implements CustomPacketPayload {
        public static final Type<PushAnimPayload> ID = new Type<>(PUSH_ANIM_ID);
        public static final StreamCodec<FriendlyByteBuf, PushAnimPayload> CODEC =
                StreamCodec.ofMember((p, buf) -> buf.writeUUID(p.playerId), buf -> new PushAnimPayload(buf.readUUID()));
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(PushAnimPayload.ID, PushAnimPayload.CODEC);
    }
    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK
                .register(PushInteractionHandler::tick);
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {
                    if (world.isClientSide) return net.minecraft.world.InteractionResult.PASS;
                    if (!(player instanceof ServerPlayer sp)) return net.minecraft.world.InteractionResult.PASS;
                    if (!(entity instanceof ServerPlayer target)) return net.minecraft.world.InteractionResult.PASS;
                    if (!CoopMovesConfig.get().enablePush) return net.minecraft.world.InteractionResult.PASS;
                    long now = System.currentTimeMillis();
                    if (sp.isShiftKeyDown()) {
                        if (HighFiveHandler.isInBlockingState(sp.getUUID())) return net.minecraft.world.InteractionResult.PASS;
                        if (isOnCooldown(sp.getUUID(), now)) return net.minecraft.world.InteractionResult.PASS;
                        if (readyPushers.containsKey(sp.getUUID())) return net.minecraft.world.InteractionResult.PASS;
                        if (sp.distanceTo(target) > PUSH_RANGE) return net.minecraft.world.InteractionResult.PASS;
                        UUID prevTarget = holdTarget.get(sp.getUUID());
                        if (!target.getUUID().equals(prevTarget)) {
                            holdTarget.put(sp.getUUID(), target.getUUID());
                            holdStart.put(sp.getUUID(), now);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                    UUID intendedTarget = readyPushers.get(target.getUUID());
                    if (intendedTarget == null || !intendedTarget.equals(sp.getUUID())) return net.minecraft.world.InteractionResult.PASS;
                    Long rs = readyStart.get(target.getUUID());
                    if (rs == null || now - rs > READY_WINDOW_MS) return net.minecraft.world.InteractionResult.PASS;
                    if (isOnCooldown(target.getUUID(), now)) return net.minecraft.world.InteractionResult.PASS;
                    double vel;
                    Long jt = lastJumpTime.get(sp.getUUID());
                    boolean recentJump = jt != null && (now - jt) < JUMP_WINDOW_MS;
                    if      (recentJump)     vel = capToCeiling(sp, VEL_HIGH);
                    else if (sp.isShiftKeyDown()) vel = capToCeiling(sp, VEL_LOW);
                    else                     vel = capToCeiling(sp, VEL_MEDIUM);
                    readyPushers.remove(target.getUUID());
                    readyStart.remove(target.getUUID());
                    executePush(target, sp, vel, now);
                    return net.minecraft.world.InteractionResult.SUCCESS;
                });
    }
    public static void tick(MinecraftServer server) {
        long now = System.currentTimeMillis();
        for (var entry : new HashMap<>(holdTarget).entrySet()) {
            UUID pusherId   = entry.getKey();
            UUID targetId   = entry.getValue();
            Long startMs    = holdStart.get(pusherId);
            if (startMs == null) { holdTarget.remove(pusherId); continue; }
            ServerPlayer pusher = server.getPlayerList().getPlayer(pusherId);
            ServerPlayer target = server.getPlayerList().getPlayer(targetId);
            if (pusher == null || !pusher.isShiftKeyDown() || target == null
                    || pusher.distanceTo(target) > PUSH_RANGE) {
                holdTarget.remove(pusherId);
                holdStart.remove(pusherId);
                continue;
            }
            if (readyPushers.containsKey(pusherId)) {
                holdTarget.remove(pusherId);
                holdStart.remove(pusherId);
                continue;
            }
            if (now - startMs >= HOLD_REQUIRED_MS) {
                holdTarget.remove(pusherId);
                holdStart.remove(pusherId);
                readyPushers.put(pusherId, targetId);
                readyStart.put(pusherId, now);
                Vec3 mid = pusher.position().add(target.position()).scale(0.5);
                pusher.serverLevel().playSound(null, mid.x, mid.y, mid.z,
                        net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(),
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.8f);
                pusher.displayClientMessage(net.minecraft.network.chat.Component.literal("§eTell homie to right-click!"), true);
                target.displayClientMessage(net.minecraft.network.chat.Component.literal("§e[Right-click to launch!]"), true);
            }
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.onGround() && p.getDeltaMovement().y > 0.08) lastJumpTime.put(p.getUUID(), now);
        }
        for (var re : new HashMap<>(readyPushers).entrySet()) {
            UUID pusherId = re.getKey();
            UUID intendedTarget = re.getValue();
            Long rs = readyStart.get(pusherId);
            if (rs == null || now - rs > READY_WINDOW_MS) continue;
            ServerPlayer pusher = server.getPlayerList().getPlayer(pusherId);
            if (pusher == null) continue;
            for (ServerPlayer nearby : server.getPlayerList().getPlayers()) {
                if (nearby.getUUID().equals(pusherId)) continue;
                if (pusher.distanceTo(nearby) <= PUSH_RANGE) {
                    UUID nearbyId = nearby.getUUID();
                    if (!nearbyId.equals(intendedTarget)) {
                        readyPushers.put(pusherId, nearbyId);
                        nearby.displayClientMessage(net.minecraft.network.chat.Component.literal("§e[Right-click to launch!]"), true);
                    }
                    break;
                }
            }
        }
        readyPushers.entrySet().removeIf(e -> { Long t = readyStart.get(e.getKey()); return t == null || now - t > READY_WINDOW_MS; });
        readyStart.entrySet().removeIf(e -> now - e.getValue() > READY_WINDOW_MS);
        holdStart.entrySet().removeIf(e -> now - e.getValue() > 10000L);
        lastJumpTime.entrySet().removeIf(e -> now - e.getValue() > JUMP_WINDOW_MS * 4);
        cooldowns.entrySet().removeIf(e -> now - e.getValue() > COOLDOWN_MS * 2);
    }
    private static void executePush(ServerPlayer pusher, ServerPlayer target, double velocity, long now) {
        PushAnimPayload pkt = new PushAnimPayload(pusher.getUUID());
        for (ServerPlayer p : PlayerLookup.tracking(pusher)) ServerPlayNetworking.send(p, pkt);
        ServerPlayNetworking.send(pusher, pkt);
        target.setDeltaMovement(target.getDeltaMovement().x, 0, target.getDeltaMovement().z);
        target.push(0, velocity, 0);
        target.hurtMarked = true;
        pushImmunity.put(target.getUUID(), now);
        LaunchedPlayerTracker.markPlayerAsLaunched(target.getUUID());
        UUID carried = GrabMechanic.holding.get(target.getUUID());
        if (carried != null) {
            ServerPlayer c = target.getServer().getPlayerList().getPlayer(carried);
            if (c != null) { c.push(0, velocity * 0.85, 0); c.hurtMarked = true;
                LaunchedPlayerTracker.markPlayerAsLaunched(c.getUUID()); pushImmunity.put(c.getUUID(), now); }
        }
        cooldowns.put(pusher.getUUID(), now);
        PoseNetworking.broadcastPoseChange(Objects.requireNonNull(pusher.getServer()), pusher.getUUID(), PoseState.PUSH_ACTION);
    }
    private static double capToCeiling(ServerPlayer t, double base) {
        BlockPos pos = t.blockPosition();
        for (int y = 1; y <= 15; y++) {
            BlockPos check = pos.above(y); BlockState state = t.level().getBlockState(check);
            if (!state.isAir() && state.isRedstoneConductor(t.level(), check)) { return Math.min(base, Math.sqrt(2 * 0.08 * 20 * Math.max(2, y - 1))); }
        }
        return base;
    }
    private static boolean isOnCooldown(UUID uuid, long now) { Long t = cooldowns.get(uuid); return t != null && (now - t) < COOLDOWN_MS; }
    public static boolean hasPushImmunity(UUID uuid) {
        Long t = pushImmunity.get(uuid); if (t == null) return false;
        if (System.currentTimeMillis() - t < PUSH_IMMUNITY_MS) return true;
        pushImmunity.remove(uuid); return false;
    }
    public static void cleanupExpiredImmunity() { long now = System.currentTimeMillis(); pushImmunity.entrySet().removeIf(e -> now - e.getValue() > PUSH_IMMUNITY_MS); }
}