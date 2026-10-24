package com.cooptest.client;

import com.cooptest.GrabInputHandler;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class TrajectoryRenderer {
    private record Preview(Vec3[] points, float charge) {}
    private static final ContextKey<Preview> PREVIEW =
            new ContextKey<>(Identifier.fromNamespaceAndPath("coopmoves", "trajectory"));

    public static void register() {
        NeoForge.EVENT_BUS.addListener(TrajectoryRenderer::extract);
        NeoForge.EVENT_BUS.addListener(TrajectoryRenderer::submit);
    }

    private static void extract(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(PREVIEW, null);
        var player = Minecraft.getInstance().player;
        if (player == null || PoseNetworking.poseStates.getOrDefault(player.getUUID(), PoseState.NONE)
                != PoseState.GRAB_HOLDING) return;
        float charge = GrabInputHandler.getThrowChargeProgress();
        if (charge <= 0) return;
        Vec3 velocity = player.getViewVector(event.getDeltaTracker().getGameTimeDeltaPartialTick(true))
                .scale(1.5f + 2.0f * charge);
        Vec3 pos = player.getEyePosition().add(0, 0.5, 0);
        Vec3[] points = new Vec3[30];
        for (int i = 0; i < points.length; i++) {
            points[i] = pos;
            velocity = velocity.add(0, -0.08, 0).scale(0.98);
            pos = pos.add(velocity);
            if (pos.y < player.getY() - 10) break;
        }
        event.getRenderState().setRenderData(PREVIEW, new Preview(points, charge));
    }

    private static void submit(SubmitCustomGeometryEvent event) {
        Preview preview = event.getLevelRenderState().getRenderData(PREVIEW);
        if (preview == null) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(), RenderTypes.debugQuads(),
                (pose, buffer) -> {
                    for (int i = 0; i < preview.points.length && preview.points[i] != null; i++) {
                        Vec3 point = preview.points[i].subtract(camera);
                        float size = 0.08f * (1 - (float) i / 60);
                        int alpha = (int) (200 * (1 - (float) i / 30));
                        int red = (int) (255 * preview.charge);
                        int green = 255 - red;
                        float x = (float) point.x, y = (float) point.y, z = (float) point.z;
                        buffer.addVertex(pose, x-size, y-size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y-size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y+size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x-size, y+size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y-size, z-size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x-size, y-size, z-size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x-size, y+size, z-size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y+size, z-size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x-size, y+size, z-size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x-size, y+size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y+size, z+size).setColor(red, green, 50, alpha);
                        buffer.addVertex(pose, x+size, y+size, z-size).setColor(red, green, 50, alpha);
                    }
                });
    }
}
