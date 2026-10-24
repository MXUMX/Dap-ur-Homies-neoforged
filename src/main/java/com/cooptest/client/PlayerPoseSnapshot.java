package com.cooptest.client;

import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

public record PlayerPoseSnapshot(UUID id, boolean swinging, boolean usingItem, boolean onGround,
                                 boolean passenger, boolean playerVehicle, float holderYaw) {
    public static final ContextKey<PlayerPoseSnapshot> KEY = new ContextKey<>(
            Identifier.fromNamespaceAndPath("coopmoves", "player_pose"));
}
