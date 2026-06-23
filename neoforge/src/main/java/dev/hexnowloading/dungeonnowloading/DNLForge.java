package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.capability.forge.DNLAttachments;
import dev.hexnowloading.dungeonnowloading.client.DNLForgeClient;
import dev.hexnowloading.dungeonnowloading.client.DNLForgeClientEvents;
import dev.hexnowloading.dungeonnowloading.platform.ForgeCommonRegistryHelper;
import dev.hexnowloading.dungeonnowloading.registry.DNLCommands;
import dev.hexnowloading.dungeonnowloading.server.DNLForgeEntityEvents;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(DungeonNowLoading.MOD_ID)
public class DNLForge {

    // 1.21 NeoForge: the @Mod constructor receives the mod event bus directly.
    public DNLForge(IEventBus bus) {
        // Capture the mod bus before init() so lazily-created DeferredRegisters bind to it.
        ForgeCommonRegistryHelper.setModBus(bus);
        DungeonNowLoading.init();

        DNLAttachments.ATTACHMENT_TYPES.register(bus);
        bus.addListener(dev.hexnowloading.dungeonnowloading.platform.ForgeNetworkHelper::onRegisterPayloads);
        addModListeners(bus);
        if (FMLEnvironment.dist.isClient()) {
            addModClientListeners(bus);
            bus.addListener(DNLForgeClient::init);
        }

        addForgeListeners();

        DungeonNowLoading.LOGGER.info("Hello NeoForge world!");
    }

    private void addModListeners(IEventBus bus) {
        ForgeCommonRegistryHelper.TAB_REGISTRY.register(bus);
        bus.addListener(DNLForgeEntityEvents::onEntityAttributeCreation);
        bus.addListener(DNLForgeEntityEvents::registerSpawnPlacements);
    }

    private void addModClientListeners(IEventBus bus) {
        DNLClient.registerItemModels();
        bus.addListener(DNLForgeClientEvents::onRegisterAdditionalModels);
        bus.addListener(DNLForgeClientEvents::onModifyBakingResult);
        bus.addListener(DNLForgeClientEvents::onRegisterRenderer);
        bus.addListener(DNLForgeClientEvents::onRegisterLayers);
        bus.addListener(DNLForgeClientEvents::onRegisterParticleProviders);
        bus.addListener(DNLForgeClientEvents::onRegisterBlockRenderTypes);
    }

    private void addForgeListeners() {
        NeoForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingDamageEvent);
        NeoForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingHurtEvent);

        NeoForge.EVENT_BUS.addListener((ServerStartingEvent e) -> {
            // Wire the cached registry access used by ItemNbt (common) for NBT (de)serialization.
            DungeonNowLoading.setRegistryAccess(e.getServer().registryAccess());
            java.util.concurrent.CompletableFuture.runAsync(
                    () -> PatronRegistry.initOrReload(e.getServer()),
                    net.minecraft.Util.backgroundExecutor()
            );
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> {
            DNLCommands.register(e.getDispatcher());
            e.getDispatcher().register(
                    Commands.literal("dnlpatrons")
                            .requires(src -> src.hasPermission(2))
                            .then(Commands.literal("reload").executes(ctx -> {
                                PatronRegistry.initOrReload(ctx.getSource().getServer());
                                ctx.getSource().sendSuccess(() -> Component.literal("[DNL] Patrons reloaded."), true);
                                return 1;
                            }))
            );
        });
    }
}
