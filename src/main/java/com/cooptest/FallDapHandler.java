package com.cooptest;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
public class FallDapHandler {
    private static final Map<UUID, FallDapState> fallDapPlayers = new HashMap<>();
    private static final Map<UUID, Long> fallChargeStartTime = new HashMap<>();
    private static final Map<UUID, Long> squashedPlayers = new HashMap<>();
    private static final long SQUASHED_DURATION_MS = 25000;
    private static final Map<UUID, Double> fallStartY = new HashMap<>();
    private static final double REQUIRED_FALL_BLOCKS = 20.0;
    private static final long FALL_CHARGE_DURATION_MS = 750;
    public enum FallDapState {
        NONE,
        CHARGING,
        FALLING
    }
    public record FallDapAnimPayload(UUID playerId, int state) implements CustomPacketPayload {
        public static final Type<FallDapAnimPayload> ID =
                new Type<>(ResourceLocation.fromNamespaceAndPath("testcoop", "fall_dap_anim"));
        public static final StreamCodec<FriendlyByteBuf, FallDapAnimPayload> CODEC =
                StreamCodec.ofMember(
                        (payload, buf) -> {
                            buf.writeUUID(payload.playerId);
                            buf.writeInt(payload.state);
                        },
                        buf -> new FallDapAnimPayload(buf.readUUID(), buf.readInt())
                );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public record SquashAnimPayload(UUID playerId) implements CustomPacketPayload {
        public static final Type<SquashAnimPayload> ID =
                new Type<>(ResourceLocation.fromNamespaceAndPath("testcoop", "squash_anim"));
        public static final StreamCodec<FriendlyByteBuf, SquashAnimPayload> CODEC =
                StreamCodec.ofMember(
                        (payload, buf) -> buf.writeUUID(payload.playerId),
                        buf -> new SquashAnimPayload(buf.readUUID())
                );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public static void register() {
        PayloadTypeRegistry.playS2C().register(FallDapAnimPayload.ID, FallDapAnimPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SquashAnimPayload.ID, SquashAnimPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tick(server);
        });
    }
    private static void tick(net.minecraft.server.MinecraftServer server) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> squashIt = squashedPlayers.entrySet().iterator();
        while (squashIt.hasNext()) {
            Map.Entry<UUID, Long> entry = squashIt.next();
            UUID playerId = entry.getKey();
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (now >= entry.getValue()) {
                squashIt.remove();
                if (player != null) {
                    PoseNetworking.broadcastAnimState(player, 0);
                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                    player.removeEffect(MobEffects.JUMP);
                }
            } else if (player != null) {
                if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) ||
                        player.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, false));
                }
                if (!player.hasEffect(MobEffects.JUMP) ||
                        player.getEffect(MobEffects.JUMP).getDuration() < 40) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 250, false, false));
                }
            }
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            if (isSquashed(playerId)) continue;
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
                cleanup(playerId);
                continue;
            }
            boolean isChargingDap = ChargedDapHandler.isCharging(playerId);
            boolean isOnGround = player.onGround();
            boolean isFalling = player.getDeltaMovement().y < -0.1;
            FallDapState currentState = fallDapPlayers.getOrDefault(playerId, FallDapState.NONE);
            if (currentState == FallDapState.NONE) {
                if (isChargingDap && isFalling && !isOnGround) {
                    if (!fallStartY.containsKey(playerId)) {
                        fallStartY.put(playerId, player.getY());
                    }
                    double startY = fallStartY.get(playerId);
                    double fallen = startY - player.getY();
                    if (fallen >= REQUIRED_FALL_BLOCKS && ChargedDapHandler.isFullyCharged(playerId)) {
                        startFallDapCharge(player);
                    }
                } else if (isOnGround) {
                    fallStartY.remove(playerId);
                }
            } else if (currentState == FallDapState.CHARGING) {
                Long chargeStart = fallChargeStartTime.get(playerId);
                if (chargeStart != null && now - chargeStart >= FALL_CHARGE_DURATION_MS) {
                    fallDapPlayers.put(playerId, FallDapState.FALLING);
                    broadcastFallDapAnim(player, 2);
                    player.displayClientMessage(Component.literal("§c§l FALL DAP READY! "), true);
                }
                if (isOnGround) {
                    if (isChargingDap) {
                        resetToNormalCharge(player);
                    } else {
                        cleanup(playerId);
                        PoseNetworking.broadcastAnimState(player, 0);
                    }
                }
            } else if (currentState == FallDapState.FALLING) {
                ServerPlayer victim = findSquashTarget(player, 3.0);
                if (victim != null) {
                    squashPlayer(player, victim);
                    cleanup(playerId);
                    continue;
                }
                if (isOnGround) {
                    if (isChargingDap) {
                        resetToNormalCharge(player);
                    } else {
                        cleanup(playerId);
                        PoseNetworking.broadcastAnimState(player, 0);
                    }
                }
            }
        }
    }
    private static void startFallDapCharge(ServerPlayer player) {
        UUID playerId = player.getUUID();
        fallDapPlayers.put(playerId, FallDapState.CHARGING);
        fallChargeStartTime.put(playerId, System.currentTimeMillis());
        broadcastFallDapAnim(player, 1);
        player.displayClientMessage(Component.literal("§e§l⚡ FALL DAP CHARGING! ⚡"), true);
    }
    private static void resetToNormalCharge(ServerPlayer player) {
        UUID playerId = player.getUUID();
        fallDapPlayers.remove(playerId);
        fallStartY.remove(playerId);
        fallChargeStartTime.remove(playerId);
        broadcastFallDapAnim(player, 0);
        PoseNetworking.broadcastAnimState(player,
                com.cooptest.client.CoopAnimationHandler.AnimState.DAP_CHARGE_IDLE.ordinal());
        player.displayClientMessage(Component.literal("§7Fall dap reset - touched ground"), true);
    }
    public static boolean isInFallDapState(UUID playerId) {
        FallDapState state = fallDapPlayers.get(playerId);
        return state == FallDapState.CHARGING || state == FallDapState.FALLING;
    }
    public static boolean isReadyToFallDap(UUID playerId) {
        return fallDapPlayers.get(playerId) == FallDapState.FALLING;
    }
    public static void executeFallDapHit(ServerLevel world, Vec3 pos,
                                         ServerPlayer attacker, ServerPlayer victim) {
        UUID attackerId = attacker.getUUID();
        broadcastFallDapAnim(attacker, 3);
        cleanup(attackerId);
    }
    private static void squashPlayer(ServerPlayer attacker, ServerPlayer victim) {
        ServerLevel world = attacker.serverLevel();
        Vec3 pos = victim.position();
        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 2.0f, 0.5f);
        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.8f);
        world.sendParticles(ParticleTypes.CRIT, pos.x, pos.y + 1, pos.z, 30, 0.5, 0.3, 0.5, 0.2);
        world.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 20, 0.5, 0.2, 0.5, 0.05);
        dropHandItems(victim, world, pos);
        victim.hurt(world.damageSources().playerAttack(attacker), 10.0f);
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 420, 2, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.JUMP, 420, 250, false, false));
        victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, false, false));
        victim.setDeltaMovement(0, 0, 0);
        victim.hurtMarked = true;
        squashedPlayers.put(victim.getUUID(), System.currentTimeMillis() + SQUASHED_DURATION_MS);
        for (ServerPlayer p : world.getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, new SquashAnimPayload(victim.getUUID()));
        }
        attacker.displayClientMessage(Component.literal("§c§l💀 SQUASHED! 💀"), true);
        victim.displayClientMessage(Component.literal("§c§lYOU GOT SQUASHED FOR 25 SECONDS!"), true);
    }
    private static void dropHandItems(ServerPlayer player, ServerLevel world, Vec3 pos) {
        net.minecraft.world.item.ItemStack mainStack = player.getMainHandItem();
        if (!mainStack.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity mainItem = new net.minecraft.world.entity.item.ItemEntity(
                    world, pos.x, pos.y + 0.5, pos.z, mainStack.copy()
            );
            mainItem.setDeltaMovement(
                    (world.random.nextDouble() - 0.5) * 0.3,
                    world.random.nextDouble() * 0.2 + 0.1,
                    (world.random.nextDouble() - 0.5) * 0.3
            );
            world.addFreshEntity(mainItem);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        net.minecraft.world.item.ItemStack offStack = player.getOffhandItem();
        if (!offStack.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity offItem = new net.minecraft.world.entity.item.ItemEntity(
                    world, pos.x, pos.y + 0.5, pos.z, offStack.copy()
            );
            offItem.setDeltaMovement(
                    (world.random.nextDouble() - 0.5) * 0.3,
                    world.random.nextDouble() * 0.2 + 0.1,
                    (world.random.nextDouble() - 0.5) * 0.3
            );
            world.addFreshEntity(offItem);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        net.minecraft.world.item.ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (!helmet.isEmpty()) {
            net.minecraft.world.entity.item.ItemEntity helmetItem = new net.minecraft.world.entity.item.ItemEntity(
                    world, pos.x, pos.y + 1.0, pos.z, helmet.copy()
            );
            helmetItem.setDeltaMovement(
                    (world.random.nextDouble() - 0.5) * 0.4,
                    world.random.nextDouble() * 0.4 + 0.3,
                    (world.random.nextDouble() - 0.5) * 0.4
            );
            world.addFreshEntity(helmetItem);
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
        }
    }
    private static ServerPlayer findSquashTarget(ServerPlayer attacker, double horizontalRange) {
        double attackerY = attacker.getY();
        for (ServerPlayer other : attacker.serverLevel().players()) {
            if (other == attacker) continue;
            if (isSquashed(other.getUUID())) continue;
            double otherY = other.getY();
            double heightDiff = attackerY - otherY;
            if (heightDiff < 0.5 || heightDiff > 3.0) continue;
            double dx = attacker.getX() - other.getX();
            double dz = attacker.getZ() - other.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDist <= horizontalRange) {
                return other;
            }
        }
        return null;
    }
    private static ServerPlayer findNearbyPlayer(ServerPlayer player, double range) {
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player) continue;
            if (other.distanceToSqr(player) <= range * range) {
                return other;
            }
        }
        return null;
    }
    public static boolean isSquashed(UUID playerId) {
        Long endTime = squashedPlayers.get(playerId);
        if (endTime == null) return false;
        if (System.currentTimeMillis() >= endTime) {
            squashedPlayers.remove(playerId);
            return false;
        }
        return true;
    }
    private static void broadcastFallDapAnim(ServerPlayer player, int state) {
        var server = player.getServer();
        if (server == null) return;
        FallDapAnimPayload payload = new FallDapAnimPayload(player.getUUID(), state);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(p, payload);
        }
    }
    public static void cleanup(UUID playerId) {
        fallDapPlayers.remove(playerId);
        fallStartY.remove(playerId);
        fallChargeStartTime.remove(playerId);
        squashedPlayers.remove(playerId);
    }
}