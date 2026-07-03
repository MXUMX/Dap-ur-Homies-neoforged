package com.cooptest.neoforge;

import com.cooptest.MahitoItems;
import com.cooptest.ModEffects;
import com.cooptest.ModSounds;
import com.cooptest.TestCoop;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.NeoForgePayloadBridge;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("coopmoves")
public final class CoopMovesNeoForge {
    public CoopMovesNeoForge(IEventBus modEventBus) {
        ModSounds.registerNeoForge(modEventBus);
        ModEffects.registerNeoForge(modEventBus);
        MahitoItems.registerNeoForge(modEventBus);

        new TestCoop().onInitialize();

        modEventBus.addListener(NeoForgePayloadBridge::registerPayloads);
        modEventBus.addListener(KeyBindingHelper::registerNeoForgeKeyMappings);
        modEventBus.addListener(this::onBuildCreativeTab);

        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(this::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onRenderGui);
        NeoForge.EVENT_BUS.addListener(this::onRenderLevelStage);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init();
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        ServerTickEvents.END_SERVER_TICK.invoke(event.getServer());
    }

    private void onClientTick(ClientTickEvent.Post event) {
        ClientTickEvents.END_CLIENT_TICK.invoke(Minecraft.getInstance());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistrationCallback.EVENT.invoke(event);
    }

    private void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        InteractionResult result = UseEntityCallback.EVENT.invoke(
                event.getEntity(),
                event.getLevel(),
                event.getHand(),
                event.getTarget(),
                new EntityHitResult(event.getTarget(), event.getLocalPos()));
        applyActionResult(event, result);
    }

    private void onAttackEntity(AttackEntityEvent event) {
        InteractionResult result = AttackEntityCallback.EVENT.invoke(
                event.getEntity(),
                event.getEntity().level(),
                InteractionHand.MAIN_HAND,
                event.getTarget(),
                new EntityHitResult(event.getTarget()));
        applyActionResult(event, result);
    }

    private void onLivingDamage(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (!ServerLivingEntityEvents.ALLOW_DAMAGE.invoke(entity, event.getSource(), event.getOriginalDamage())) {
            event.setNewDamage(0);
        }
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getServer() != null) {
            ServerPlayConnectionEvents.DISCONNECT.invoke(player.connection, player.getServer());
        }
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ItemGroupEvents.apply(event);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        ResourceManagerHelper.addReloadListeners(event);
    }

    private void onRenderGui(RenderGuiEvent.Post event) {
        HudRenderCallback.EVENT.invoke(event.getGuiGraphics(), event.getPartialTick());
    }

    private void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        PoseStack matrixStack = event.getPoseStack();
        Camera camera = event.getCamera();
        DeltaTracker tickCounter = event.getPartialTick();
        WorldRenderEvents.AFTER_TRANSLUCENT.invoke(new WorldRenderContext() {
            @Override
            public PoseStack matrixStack() {
                return matrixStack;
            }

            @Override
            public Camera camera() {
                return camera;
            }

            @Override
            public DeltaTracker tickCounter() {
                return tickCounter;
            }
        });
    }

    private static void applyActionResult(Object event, InteractionResult result) {
        if (result == InteractionResult.PASS) {
            return;
        }
        if (event instanceof ICancellableEvent cancellableEvent) {
            cancellableEvent.setCanceled(true);
        }
    }

    private static final class ClientInit {
        private static void init() {
            new com.cooptest.TestCoopClient().onInitializeClient();
        }
    }
}
