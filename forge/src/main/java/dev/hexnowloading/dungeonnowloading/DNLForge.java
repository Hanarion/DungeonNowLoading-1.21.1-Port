package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.client.DNLForgeClient;
import dev.hexnowloading.dungeonnowloading.client.DNLForgeClientEvents;
import dev.hexnowloading.dungeonnowloading.platform.ForgeCommonRegistryHelper;
import dev.hexnowloading.dungeonnowloading.server.DNLForgeEntityEvents;
import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import dev.hexnowloading.dungeonnowloading.registry.DNLMenuTypes;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(DungeonNowLoading.MOD_ID)
public class DNLForge {

    public DNLForge() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        DungeonNowLoading.init();
        // Register & bootstrap menu types
        var mendingTable = Services.REGISTRY.register(BuiltInRegistries.MENU, "mending_table",
                () -> IForgeMenuType.create((id, inv, buf) -> new dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu(id, inv)));
        DNLMenuTypes.bootstrap(mendingTable);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //MinecraftForge.EVENT_BUS.register(this);
        addModListeners(bus);
        if (FMLEnvironment.dist.isClient()) {
            // register client-only listeners
            addModClientListeners(bus);
            // run init when registries are ready
            bus.addListener((FMLClientSetupEvent event) -> DNLForgeClient.init());
        }

        addForgeListeners();

        DungeonNowLoading.LOGGER.info("Hello Forge world!");
    }

    private void addModListeners(IEventBus bus) {
        ForgeCommonRegistryHelper.TAB_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        bus.addListener(DNLForgeEntityEvents::onEntityAttributeCreation);
        bus.addListener(DNLForgeEntityEvents::registerSpawnPlacements);
        bus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(dev.hexnowloading.dungeonnowloading.registry.DNLFlammables::register);
    }

    private void addModClientListeners(IEventBus bus) {
        bus.addListener(DNLForgeClientEvents::onRegisterRenderer);
        bus.addListener(DNLForgeClientEvents::onRegisterLayers);
        bus.addListener(DNLForgeClientEvents::onRegisterParticleProviders);
    }

    private void addForgeListeners() {
        MinecraftForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingDamageEvent);
        MinecraftForge.EVENT_BUS.addListener(DNLForgeEntityEvents::onLivingHurtEvent);
    }
}