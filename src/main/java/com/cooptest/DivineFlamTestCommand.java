package com.cooptest;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
public class DivineFlamTestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("testdivine")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayer();
                    if (player == null) {
                        source.sendFailure(net.minecraft.network.chat.Component.literal("Must be a player!"));
                        return 0;
                    }
                    ServerLevel world = player.serverLevel();
                    Vec3 pos = player.position().add(0, 1, 0);
                    Vec3 direction = player.getLookAngle();
                    TestPillar pillar = new TestPillar(world, pos, direction);
                    TestPillarTicker.addPillar(pillar);
                    source.sendSuccess(() -> net.minecraft.network.chat.Component.literal("§a✓ Divine Flame pillar spawned! It will move forward for 5 seconds."), false);
                    return 1;
                })
        );
    }
    public static class TestPillar {
        private final ServerLevel world;
        private Vec3 position;
        private final Vec3 direction;
        private int ticksAlive = 0;
        private static final int MAX_LIFETIME = 100;
        public TestPillar(ServerLevel world, Vec3 startPos, Vec3 direction) {
            this.world = world;
            this.position = startPos;
            this.direction = direction.normalize();
            System.out.println("[Test Pillar] Spawned at " + startPos + " moving in direction " + direction);
        }
        public void tick() {
            ticksAlive++;
            if (ticksAlive % 20 == 0) {
                System.out.println("[FIRE VORTEX] Tick " + ticksAlive);
            }
            double MAX_HEIGHT = 60.0;
            double BASE_RADIUS = 20.0;
            double TOP_RADIUS = 3.0;
            double SAFE_RADIUS = 1.5;
            double baseRotation = (ticksAlive * 0.3) % (Math.PI * 2);
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                for (double radius = 3.0; radius <= BASE_RADIUS; radius += 2.0) {
                    double spinAngle = angle + baseRotation;
                    double x = position.x + Math.cos(spinAngle) * radius;
                    double z = position.z + Math.sin(spinAngle) * radius;
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            x, position.y + 0.5, z, 2, 0.3, 0.2, 0.3, 0.03);
                    if (radius > BASE_RADIUS * 0.8) {
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                                x, position.y, z, 1, 0.2, 0.05, 0.2, 0);
                    }
                }
            }
            if (ticksAlive % 3 == 0) {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * SAFE_RADIUS * 0.8;
                    double x = position.x + Math.cos(angle) * radius;
                    double z = position.z + Math.sin(angle) * radius;
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                            x, position.y + 0.3, z, 1, 0.1, 0.1, 0.1, 0.01);
                }
            }
            for (double y = 0; y <= MAX_HEIGHT; y += 2.0) {
                double worldY = position.y + y;
                double heightProgress = y / MAX_HEIGHT;
                double currentRadius = BASE_RADIUS - (heightProgress * (BASE_RADIUS - TOP_RADIUS));
                double rotationOffset = heightProgress * Math.PI * 4;
                int particlesPerRing = (int) (8 - (heightProgress * 4));
                double angleStep = (Math.PI * 2) / particlesPerRing;
                for (int i = 0; i < particlesPerRing; i++) {
                    double angle = baseRotation + rotationOffset + (i * angleStep);
                    double x = position.x + Math.cos(angle) * currentRadius;
                    double z = position.z + Math.sin(angle) * currentRadius;
                    if (y < 15) {
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                                x, worldY, z, 3, 0.3, 0.3, 0.3, 0.04);
                    } else if (y < 40) {
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                                x, worldY, z, 2, 0.25, 0.25, 0.25, 0.03);
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                                x, worldY, z, 1, 0.2, 0.2, 0.2, 0.02);
                    } else {
                        world.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                                x, worldY, z, 1, 0.2, 0.2, 0.2, 0.02);
                        if (y > 50) {
                            world.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                                    x, worldY, z, 1, 0.2, 0.2, 0.2, 0.01);
                        }
                    }
                }
            }
            for (int stream = 0; stream < 3; stream++) {
                double streamAngle = baseRotation + (stream * Math.PI * 2 / 3);
                for (double y = 0; y <= MAX_HEIGHT; y += 3.0) {
                    double heightProgress = y / MAX_HEIGHT;
                    double currentRadius = BASE_RADIUS - (heightProgress * (BASE_RADIUS - TOP_RADIUS));
                    double spiralAngle = streamAngle + (heightProgress * Math.PI * 6);
                    double x = position.x + Math.cos(spiralAngle) * currentRadius * 0.9;
                    double z = position.z + Math.sin(spiralAngle) * currentRadius * 0.9;
                    world.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                            x, position.y + y, z, 2, 0.15, 0.15, 0.15, 0.03);
                }
            }
            if (ticksAlive % 10 == 0) {
                world.playSound(null, position.x, position.y, position.z,
                        net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 6.0f, 0.7f);
            }
            if (ticksAlive == 1) {
                world.playSound(null, position.x, position.y, position.z,
                        net.minecraft.sounds.SoundEvents.BLAZE_SHOOT,
                        net.minecraft.sounds.SoundSource.PLAYERS, 10.0f, 0.4f);
            }
            for (net.minecraft.world.entity.Entity entity : world.getEntities(null,
                    net.minecraft.world.phys.AABB.ofSize(position, BASE_RADIUS * 2, MAX_HEIGHT * 2, BASE_RADIUS * 2))) {
                if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    double distance2D = Math.sqrt(
                            Math.pow(entity.getX() - position.x, 2) +
                                    Math.pow(entity.getZ() - position.z, 2)
                    );
                    double heightDiff = Math.abs(entity.getY() - position.y);
                    if (heightDiff <= MAX_HEIGHT) {
                        double heightProgress = heightDiff / MAX_HEIGHT;
                        double radiusAtHeight = BASE_RADIUS - (heightProgress * (BASE_RADIUS - TOP_RADIUS));
                        if (distance2D > SAFE_RADIUS && distance2D < radiusAtHeight) {
                            living.hurt(world.damageSources().onFire(), 30.0f);
                            living.igniteForSeconds(10);
                        }
                    }
                }
            }
        }
        public boolean shouldRemove() {
            return ticksAlive >= MAX_LIFETIME;
        }
    }
    public static class TestPillarTicker {
        private static final java.util.List<TestPillar> pillars = new java.util.ArrayList<>();
        public static void addPillar(TestPillar pillar) {
            pillars.add(pillar);
        }
        public static void tick() {
            java.util.Iterator<TestPillar> it = pillars.iterator();
            while (it.hasNext()) {
                TestPillar pillar = it.next();
                pillar.tick();
                if (pillar.shouldRemove()) {
                    System.out.println("[Test Pillar] Removed after " + pillar.ticksAlive + " ticks");
                    it.remove();
                }
            }
        }
    }
}