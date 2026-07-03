package net.fabricmc.fabric.api.event.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class UseEntityCallback {
    public static final Event EVENT = new Event();

    private UseEntityCallback() {
    }

    @FunctionalInterface
    public interface Callback {
        InteractionResult interact(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult);
    }

    public static final class Event {
        private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

        public void register(Callback callback) {
            callbacks.add(callback);
        }

        public InteractionResult invoke(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
            for (Callback callback : callbacks) {
                InteractionResult result = callback.interact(player, world, hand, entity, hitResult);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        }
    }
}
