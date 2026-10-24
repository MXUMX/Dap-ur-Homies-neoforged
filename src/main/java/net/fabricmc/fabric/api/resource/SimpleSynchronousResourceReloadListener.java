package net.fabricmc.fabric.api.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleSynchronousResourceReloadListener extends PreparableReloadListener {
    Identifier getFabricId();

    void reload(ResourceManager manager);

    @Override
    default CompletableFuture<Void> reload(SharedState state, Executor prepareExecutor,
                                           PreparationBarrier synchronizer, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> null, prepareExecutor)
                .thenCompose(synchronizer::wait)
                .thenRunAsync(() -> reload(state.resourceManager()), applyExecutor);
    }
}
