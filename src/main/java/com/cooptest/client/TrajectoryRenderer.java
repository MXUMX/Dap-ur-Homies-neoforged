package com.cooptest.client;
import com.cooptest.GrabInputHandler;
import com.cooptest.PoseNetworking;
import com.cooptest.PoseState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
public class TrajectoryRenderer {
    private static final int TRAJECTORY_POINTS = 30;
    private static final float TIME_STEP = 0.1f;
    private static final float GRAVITY = 0.08f;
    private static final float DRAG = 0.02f;
    private static final float DOT_SIZE = 0.08f;
    private static final float MIN_POWER_MULT = 1.5f;
    private static final float MAX_POWER_MULT = 3.5f;
    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(TrajectoryRenderer::render);
    }
    private static void render(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        PoseState pose = PoseNetworking.poseStates.getOrDefault(
                client.player.getUUID(), PoseState.NONE
        );
        if (pose != PoseState.GRAB_HOLDING) return;
        float chargeProgress = GrabInputHandler.getThrowChargeProgress();
        if (chargeProgress <= 0) return;
        float power = MIN_POWER_MULT + (MAX_POWER_MULT - MIN_POWER_MULT) * chargeProgress;
        Vec3 lookVec = client.player.getViewVector(context.tickCounter().getGameTimeDeltaPartialTick(true));
        Vec3 startPos = client.player.getEyePosition().add(0, 0.5, 0);
        Vec3 velocity = lookVec.scale(power);
        Vec3[] points = new Vec3[TRAJECTORY_POINTS];
        Vec3 pos = startPos;
        Vec3 vel = velocity;
        for (int i = 0; i < TRAJECTORY_POINTS; i++) {
            points[i] = pos;
            vel = vel.add(0, -GRAVITY, 0);
            vel = vel.scale(1.0 - DRAG);
            pos = pos.add(vel);
            if (pos.y < client.player.getY() - 10) break;
        }
        renderTrajectoryDots(context, points, chargeProgress);
    }
    private static void renderTrajectoryDots(WorldRenderContext context, Vec3[] points, float charge) {
        Minecraft client = Minecraft.getInstance();
        Camera camera = context.camera();
        Vec3 camPos = camera.getPosition();
        PoseStack matrices = context.matrixStack();
        matrices.pushPose();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = matrices.last().pose();
        int r = (int)(charge * 255);
        int g = (int)((1 - charge) * 255);
        int b = 50;
        int alpha = 200;
        for (int i = 0; i < points.length && points[i] != null; i++) {
            Vec3 point = points[i];
            float fadeAlpha = alpha * (1.0f - (float)i / points.length);
            float size = DOT_SIZE * (1.0f - (float)i / points.length * 0.5f);
            float x = (float)point.x;
            float y = (float)point.y;
            float z = (float)point.z;
            buffer.addVertex(matrix, x - size, y - size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y - size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x - size, y + size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y - size, z - size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x - size, y - size, z - size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x - size, y + size, z - size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y + size, z - size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x - size, y + size, z - size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x - size, y + size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, (int)fadeAlpha);
            buffer.addVertex(matrix, x + size, y + size, z - size).setColor(r, g, b, (int)fadeAlpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        matrices.popPose();
    }
}