package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ServerLivingEntityEvents {
    public static final AllowDamage ALLOW_DAMAGE = new AllowDamage();

    private ServerLivingEntityEvents() {
    }

    @FunctionalInterface
    public interface AllowDamageCallback {
        boolean allowDamage(LivingEntity entity, DamageSource source, float amount);
    }

    public static final class AllowDamage {
        private final List<AllowDamageCallback> callbacks = new CopyOnWriteArrayList<>();

        public void register(AllowDamageCallback callback) {
            callbacks.add(callback);
        }

        public boolean invoke(LivingEntity entity, DamageSource source, float amount) {
            for (AllowDamageCallback callback : callbacks) {
                if (!callback.allowDamage(entity, source, amount)) {
                    return false;
                }
            }
            return true;
        }
    }
}
