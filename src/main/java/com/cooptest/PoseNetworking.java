package com.cooptest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.UUID;
public class PoseNetworking {
    public static final HashMap<UUID, PoseState> poseStates = new HashMap<>();
    public static final HashMap<UUID, Float> chargeProgress = new HashMap<>();
    public record PoseSyncPayload(UUID playerId, int poseOrdinal) implements CustomPacketPayload {
        public static final Type<PoseSyncPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cooptest", "pose_sync"));
        public static final StreamCodec<FriendlyByteBuf, PoseSyncPayload> CODEC = StreamCodec.ofMember(
                (payload, buf) -> {
                    buf.writeUUID(payload.playerId);
                    buf.writeInt(payload.poseOrdinal);
                },
                buf -> new PoseSyncPayload(buf.readUUID(), buf.readInt())
        );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public record ChargeSyncPayload(UUID playerId, float progress) implements CustomPacketPayload {
        public static final Type<ChargeSyncPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cooptest", "charge_sync"));
        public static final StreamCodec<FriendlyByteBuf, ChargeSyncPayload> CODEC = StreamCodec.ofMember(
                (payload, buf) -> {
                    buf.writeUUID(payload.playerId);
                    buf.writeFloat(payload.progress);
                },
                buf -> new ChargeSyncPayload(buf.readUUID(), buf.readFloat())
        );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public record ThrowAnimPayload(UUID playerId) implements CustomPacketPayload {
        public static final Type<ThrowAnimPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cooptest", "throw_anim"));
        public static final StreamCodec<FriendlyByteBuf, ThrowAnimPayload> CODEC = StreamCodec.ofMember(
                (payload, buf) -> buf.writeUUID(payload.playerId),
                buf -> new ThrowAnimPayload(buf.readUUID())
        );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public record AnimStateSyncPayload(UUID playerId, int animStateOrdinal) implements CustomPacketPayload {
        public static final Type<AnimStateSyncPayload> ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cooptest", "anim_state_sync"));
        public static final StreamCodec<FriendlyByteBuf, AnimStateSyncPayload> CODEC = StreamCodec.ofMember(
                (payload, buf) -> {
                    buf.writeUUID(payload.playerId);
                    buf.writeInt(payload.animStateOrdinal);
                },
                buf -> new AnimStateSyncPayload(buf.readUUID(), buf.readInt())
        );
        @Override
        public Type<? extends CustomPacketPayload> type() { return ID; }
    }
    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(PoseSyncPayload.ID, PoseSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PoseSyncPayload.ID, PoseSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ChargeSyncPayload.ID, ChargeSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChargeSyncPayload.ID, ChargeSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThrowAnimPayload.ID, ThrowAnimPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ThrowAnimPayload.ID, ThrowAnimPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AnimStateSyncPayload.ID, AnimStateSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimStateSyncPayload.ID, AnimStateSyncPayload.CODEC);
    }
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(PoseSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            PoseState state = PoseState.values()[payload.poseOrdinal()];
            context.server().execute(() -> {
                ServerPlayer requester = context.server().getPlayerList().getPlayer(id);
                if (state == PoseState.GRAB_READY && HighFiveHandler.isInBlockingState(id)) {
                    if (requester != null) {
                        ServerPlayNetworking.send(requester, new PoseSyncPayload(id, PoseState.NONE.ordinal()));
                    }
                    return;
                }
                if (state == PoseState.PUSH_IDLE) {
                    if (requester != null && !requester.getMainHandItem().isEmpty()) {
                        requester.displayClientMessage(net.minecraft.network.chat.Component.literal("§cHold nothing in your main hand to push!"), true);
                        ServerPlayNetworking.send(requester, new PoseSyncPayload(id, PoseState.NONE.ordinal()));
                        return;
                    }
                }
                poseStates.put(id, state);
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, new PoseSyncPayload(id, state.ordinal()));
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ChargeSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            float progress = payload.progress();
            context.server().execute(() -> {
                chargeProgress.put(id, progress);
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    if (!player.getUUID().equals(id)) {
                        ServerPlayNetworking.send(player, new ChargeSyncPayload(id, progress));
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(ThrowAnimPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            context.server().execute(() -> {
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    if (!player.getUUID().equals(id)) {
                        ServerPlayNetworking.send(player, new ThrowAnimPayload(id));
                    }
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(AnimStateSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            int animState = payload.animStateOrdinal();
            context.server().execute(() -> {
                for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                    if (!player.getUUID().equals(id)) {
                        ServerPlayNetworking.send(player, new AnimStateSyncPayload(id, animState));
                    }
                }
            });
        });
    }
    public static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(PoseSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            PoseState state = PoseState.values()[payload.poseOrdinal()];
            poseStates.put(id, state);
            context.client().execute(() -> {
                if (context.client().level != null) {
                    for (Player player : context.client().level.players()) {
                        if (player.getUUID().equals(id)) {
                            com.cooptest.client.CoopAnimationHandler.updatePlayerAnimation(player, state);
                            break;
                        }
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(ChargeSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            float progress = payload.progress();
            chargeProgress.put(id, progress);
        });
        ClientPlayNetworking.registerGlobalReceiver(ThrowAnimPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            ArmPoseTracker.throwAnimationStart.put(id, System.currentTimeMillis());
        });
        ClientPlayNetworking.registerGlobalReceiver(AnimStateSyncPayload.ID, (payload, context) -> {
            UUID id = payload.playerId();
            int animState = payload.animStateOrdinal();
            context.client().execute(() -> {
                if (context.client().level != null) {
                    var localPlayer = context.client().player;
                    if (animState == 0) {
                        com.cooptest.client.ChargedDapClientHandler.cleanup(id);
                        com.cooptest.client.CoopAnimationHandler.cleanup(id);
                        com.cooptest.client.HighFiveClientHandler.cleanup(id);
                        com.cooptest.client.PushClientHandler.cleanup(id);
                        com.cooptest.client.MahitoClientHandler.cleanup(id);
                        com.cooptest.client.FallDapClientHandler.cleanup(id);
                        ArmPoseTracker.cleanup(id);
                    }
                    Player targetPlayer = null;
                    for (Player player : context.client().level.players()) {
                        if (player.getUUID().equals(id)) {
                            targetPlayer = player;
                            break;
                        }
                    }
                    if (targetPlayer != null) {
                        if (localPlayer != null && (animState == 10 || animState == 18)) {
                            boolean isLocalPlayer = id.equals(localPlayer.getUUID());
                            String animName = (animState == 10) ? "DAP_HIT" : "PERFECT_DAP_HIT";
                            String playerName = targetPlayer.getName().getString();
                        }
                        com.cooptest.client.CoopAnimationHandler.setAnimStateFromNetwork(targetPlayer, animState);
                    }
                }
            });
        });
    }
    public static void sendPoseToServer(UUID playerId, PoseState state) {
        ClientPlayNetworking.send(new PoseSyncPayload(playerId, state.ordinal()));
    }
    public static void sendChargeProgress(UUID playerId, float progress) {
        ClientPlayNetworking.send(new ChargeSyncPayload(playerId, progress));
    }
    public static void sendThrowAnimation(UUID playerId) {
        ClientPlayNetworking.send(new ThrowAnimPayload(playerId));
    }
    public static void sendAnimState(UUID playerId, int animStateOrdinal) {
        ClientPlayNetworking.send(new AnimStateSyncPayload(playerId, animStateOrdinal));
    }
    public static void broadcastPoseChange(MinecraftServer server, UUID playerId, PoseState state) {
        poseStates.put(playerId, state);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, new PoseSyncPayload(playerId, state.ordinal()));
        }
    }
    public static void broadcastAnimState(ServerPlayer sourcePlayer, int animStateOrdinal) {
        var server = sourcePlayer.getServer();
        if (server == null) return;
        UUID playerId = sourcePlayer.getUUID();
        AnimStateSyncPayload payload = new AnimStateSyncPayload(playerId, animStateOrdinal);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}