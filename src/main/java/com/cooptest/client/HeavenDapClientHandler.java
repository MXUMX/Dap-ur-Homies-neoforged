package com.cooptest.client;
import com.cooptest.HeavenDapPayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
public class HeavenDapClientHandler {
    private static boolean wasPlayingLastTick = false;
    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(CoopImpactRenderType.createReloadListener());
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            HeavenWhiteOverlay.render(context, tickCounter.getGameTimeDeltaTicks());
            if (CoopImpactHandler.playing) {
                int w = context.guiWidth();
                int h = context.guiHeight();
                if (CoopImpactHandler.whiteFrame) {
                    context.fill(0, 0, w, h, 0xE8FFFFFF);
                } else {
                    context.fill(0, 0, w, h, 0xE8000000);
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean wasPlaying = wasPlayingLastTick;
            wasPlayingLastTick = CoopImpactHandler.playing;
            CoopImpactHandler.tick();
            HeavenWhiteOverlay.tick();
            if (wasPlaying && !CoopImpactHandler.playing) {
                HeavenWhiteOverlay.start();
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(
                HeavenDapPayloads.HeavenDapStartPayload.ID,
                (payload, ctx) -> ctx.client().execute(() -> {
                    if (!HeavenWhiteOverlay.isActive()) {
                        HeavenWhiteOverlay.start();
                    }
                })
        );
        ClientPlayNetworking.registerGlobalReceiver(
                HeavenDapPayloads.HeavenDapEndPayload.ID,
                (payload, ctx) -> ctx.client().execute(HeavenWhiteOverlay::stop)
        );
        ClientPlayNetworking.registerGlobalReceiver(
                HeavenDapPayloads.HeavenImpactPayload.ID,
                (payload, ctx) -> ctx.client().execute(() ->
                        CoopImpactHandler.start(20, 33L)
                )
        );
        ClientPlayNetworking.registerGlobalReceiver(
                HeavenDapPayloads.RestoreVolumePayload.ID,
                (payload, ctx) -> ctx.client().execute(() -> {
                    if (ctx.client().options != null) {
                        ctx.client().options.getSoundSourceOptionInstance(
                                net.minecraft.sounds.SoundSource.MASTER).set(1.0);
                    }
                })
        );
    }
    public static record QTEButtonPressPayload(String button) implements CustomPacketPayload {
        public static final ResourceLocation QTE_BUTTON_PRESS_ID = ResourceLocation.fromNamespaceAndPath("cooptest", "qte_button_press");
        public static final Type<QTEButtonPressPayload> ID = new Type<>(QTE_BUTTON_PRESS_ID);
        @Override public Type<? extends CustomPacketPayload> type() { return ID; }
        public static final StreamCodec<RegistryFriendlyByteBuf, QTEButtonPressPayload> CODEC =
                StreamCodec.composite(ByteBufCodecs.STRING_UTF8, QTEButtonPressPayload::button, QTEButtonPressPayload::new);
    }
}