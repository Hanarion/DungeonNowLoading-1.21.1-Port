package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.config.DNLClientConfig;
import dev.hexnowloading.dungeonnowloading.config.DNLServerConfig;
import dev.hexnowloading.dungeonnowloading.registry.*;
import dev.hexnowloading.dungeonnowloading.supporter.DNLSupporters;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DungeonNowLoading {

    public static final String MOD_ID = "dungeonnowloading";
    public static final String MOD_NAME = "Dungeon Now Loading";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /**
     * Cached registry access for code paths (e.g. ItemStack NBT (de)serialization) that
     * need a {@link net.minecraft.core.HolderLookup.Provider} but have no Level at hand.
     * Set on server start (and updated by the client on level load).
     */
    private static net.minecraft.core.HolderLookup.Provider registryAccess;

    public static void setRegistryAccess(net.minecraft.core.HolderLookup.Provider provider) {
        if (provider != null) {
            registryAccess = provider;
        }
    }

    public static net.minecraft.core.HolderLookup.Provider registryAccess() {
        return registryAccess;
    }

    public static void init() {
        DNLPackets.registerServerbound();
        DNLPackets.registerClientbound();
        initRegistries();
        registerConfigs();
    }

    private static void initRegistries()
    {
        DNLEntityTypes.init();
        DNLBlocks.init();
        DNLBlockEntityTypes.init();
        DNLProperties.init();
        DNLItems.init();
        DNLMenuTypes.init();
        // Ensure custom enchantments are registered and available to commands and books
        DNLEnchantments.init();
        DNLMobEffects.init();
        DNLSounds.init();
        DNLMusics.init();
        DNLParticleTypes.init();
        DNLFeatures.init();
        DNLStructures.init();
        DNLProcessors.init();
        DNLCreativeModeTabs.init();
        DNLGameEvents.init();
        DNLLootInjections.setup();
        DNLRecallables.registerAll();
        DNLReloadListeners.init();
        DNLSupporters.loadSupporters();
    }

    private static void registerConfigs() {
        DNLServerConfig.register();
        DNLClientConfig.register();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
