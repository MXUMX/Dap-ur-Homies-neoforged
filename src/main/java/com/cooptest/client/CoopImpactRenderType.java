package com.cooptest.client;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;

public class CoopImpactRenderType {

    @Nullable private static ShaderInstance entityWhiteProgram = null;
    @Nullable private static ShaderInstance entityBlackProgram = null;

    public static void reload(ResourceManager manager) {
        if (entityWhiteProgram != null) { entityWhiteProgram.close(); entityWhiteProgram = null; }
        if (entityBlackProgram != null) { entityBlackProgram.close(); entityBlackProgram = null; }

        try {


            entityWhiteProgram = new ShaderInstance(
                    manager,
                    "cooptest_entity_white",
                    DefaultVertexFormat.NEW_ENTITY
            );
        } catch (IOException e) {
            System.err.println("[CoopMod] cooptest_entity_white shader failed: " + e.getMessage());
        }

        try {
            entityBlackProgram = new ShaderInstance(
                    manager,
                    "cooptest_entity_black",
                    DefaultVertexFormat.NEW_ENTITY
            );
        } catch (IOException e) {
            System.err.println("[CoopMod] cooptest_entity_black shader failed: " + e.getMessage());
        }
    }

    @Nullable public static ShaderInstance getWhiteProgram() { return entityWhiteProgram; }
    @Nullable public static ShaderInstance getBlackProgram() { return entityBlackProgram; }
    public static boolean isReady() { return entityWhiteProgram != null && entityBlackProgram != null; }

    public static RenderType getWhiteLayer(ResourceLocation texture) {
        if (entityWhiteProgram == null) return RenderType.entityCutout(texture);
        return RenderType.create(
                "cooptest_entity_white",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(CoopImpactRenderType::getWhiteProgram))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .createCompositeState(false)
        );
    }

    public static RenderType getBlackLayer(ResourceLocation texture) {
        if (entityBlackProgram == null) return RenderType.entityCutout(texture);
        return RenderType.create(
                "cooptest_entity_black",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(CoopImpactRenderType::getBlackProgram))
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(RenderStateShard.OVERLAY)
                        .createCompositeState(false)
        );
    }

    public static net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener createReloadListener() {
        return new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath("cooptest", "impact_shaders");
            }
            @Override
            public void reload(ResourceManager manager) {
                CoopImpactRenderType.reload(manager);
            }
        };
    }
}
