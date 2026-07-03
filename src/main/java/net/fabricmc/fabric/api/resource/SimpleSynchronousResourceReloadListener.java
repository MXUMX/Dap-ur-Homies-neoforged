package net.fabricmc.fabric.api.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleSynchronousResourceReloadListener extends PreparableReloadListener {
    ResourceLocation getFabricId();

    void reload(ResourceManager manager);

    @Override
    default CompletableFuture<Void> reload(PreparationBarrier synchronizer,
                                           ResourceManager manager,
                                           ProfilerFiller prepareProfiler,
                                           ProfilerFiller applyProfiler,
                                           Executor prepareExecutor,
                                           Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> null, prepareExecutor)
                .thenCompose(synchronizer::wait)
                .thenRunAsync(() -> reload(manager), applyExecutor);
    }
}
