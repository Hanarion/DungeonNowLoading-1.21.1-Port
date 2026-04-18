package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.client.DNLForgeClient;
import dev.hexnowloading.dungeonnowloading.client.DNLForgeClientEvents;
import dev.hexnowloading.dungeonnowloading.platform.ForgeCommonRegistryHelper;
import dev.hexnowloading.dungeonnowloading.registry.DNLCommands;
import dev.hexnowloading.dungeonnowloading.server.DNLForgeEntityEvents;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DungeonNowLoading.MOD_ID)
public class DNLForge {

    public DNLForge() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        DungeonNowLoading.init();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //MinecraftForge.EVENT_BUS.register(this);
        addModListeners(bus);
        if (FMLEnvironment.dist.isClient()) {
            // register client-only listeners
            addModClientListeners(bus);
            // run init when registries are ready
            bus.addListener(DNLForgeClient::init);
        }

        addForgeListeners();

        DungeonNowLoading.LOGGER.info("Hello Forge world!");
    }

    private void addModListeners(IEventBus bus) {
        ForgeCommonRegistryHelper.TAB_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        bus.addListener(DNLForgeEntityEvents::onEntityAttributeCreation);
        bus.addListener(DNLForgeEntityEvents::registerSpawnPlacements);
    }

    private void addModClientListeners(IEventBus bus) {
        bus.addListener(DNLForgeClientEvents::onRegisterRenderer);
        bus.addListener(DNLForgeClientEvents::onRegisterLayers);
        bus.addListener(DNLForgeClientEvents::onRegisterParticleProviders);
        bus.addListener(DNLForgeClientEvents::onRegisterBlockRenderTypes);
    }

    private void addForgeListeners() {
        MinecraftForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingDamageEvent);
        MinecraftForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingHurtEvent);

        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent e) -> {
            // Synchronous:
            // PatronRegistry.initOrReload(e.getServer());

            // Optional: do it in background to avoid blocking the server thread
            java.util.concurrent.CompletableFuture.runAsync(
                    () -> PatronRegistry.initOrReload(e.getServer()),
                    net.minecraft.Util.backgroundExecutor()
            );
            // Data registry reload removed — not present in this build
        });

        // Optional: /dnlpatrons reload for admins
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
            DNLCommands.register(e.getDispatcher());
            e.getDispatcher().register(
                    Commands.literal("dnlpatrons")
                            .requires(src -> src.hasPermission(2)) // OP-only
                            .then(Commands.literal("reload").executes(ctx -> {
                                // You can keep this synchronous; it’s quick, and you already cache to disk.
                                PatronRegistry.initOrReload(ctx.getSource().getServer());
                                ctx.getSource().sendSuccess(() -> Component.literal("[DNL] Patrons reloaded."), true);
                                return 1;
                            }))
            );
        });
    }
}
