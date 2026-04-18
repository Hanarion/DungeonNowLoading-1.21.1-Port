package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.ReloadListenerPlatform;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricReloadListenerPlatformHelper implements ReloadListenerPlatform {

    @Override
    public void registerDataReloadListener(ResourceLocation id, PreparableReloadListener listener) {
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new FabricReloadListenerWrapper(id, listener));
    }


    public class FabricReloadListenerWrapper implements IdentifiableResourceReloadListener {

        private final ResourceLocation id;
        private final PreparableReloadListener delegate;

        public FabricReloadListenerWrapper(ResourceLocation id, PreparableReloadListener delegate) {
            this.id = id;
            this.delegate = delegate;
        }

        @Override
        public ResourceLocation getFabricId() {
            return id;
        }

        @Override
        public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                              ResourceManager manager,
                                              ProfilerFiller prepProfiler,
                                              ProfilerFiller applyProfiler,
                                              Executor prepExecutor,
                                              Executor applyExecutor) {
            return delegate.reload(barrier, manager, prepProfiler, applyProfiler, prepExecutor, applyExecutor);
        }
    }
}
