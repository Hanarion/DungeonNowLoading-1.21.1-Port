package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.services.RegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ForgeCommonRegistryHelper implements RegistryHelper {
    public static final DeferredRegister<CreativeModeTab> TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DungeonNowLoading.MOD_ID);
    private static final RegistryMap registryMap = new RegistryMap();

    public static final List<SimpleJsonResourceReloadListener> dataLoaders = new ArrayList<>();

    // 1.21 NeoForge: the mod event bus is provided to the @Mod ctor; capture it so the
    // lazily-created DeferredRegisters can register against it.
    private static IEventBus modBus;
    public static void setModBus(IEventBus bus) { modBus = bus; }

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String name, Supplier<T> entry) {
        return registryMap.register(registry, name, entry);
    }

    @Override
    public void registerEntityDataSerializer(String name, net.minecraft.network.syncher.EntityDataSerializer<?> serializer) {
        // Register EAGERLY into NeoForge's serializer registry (not deferred). Entity entities call
        // SynchedEntityData.defineId(..., serializer) at class-load time, which needs the serializer's
        // ID assigned immediately — a DeferredRegister registers too late, leaving defineId with a bad
        // ID and silently breaking entity creation (the Chaos Spawner wouldn't spawn). Mod-init runs
        // before this registry freezes, so Registry.register works.
        net.minecraft.core.Registry.register(
                net.neoforged.neoforge.registries.NeoForgeRegistries.ENTITY_DATA_SERIALIZERS,
                dev.hexnowloading.dungeonnowloading.DungeonNowLoading.id(name),
                serializer);
    }

    @Override
    public void register(ResourceLocation id, SimpleJsonResourceReloadListener loader) {
        dataLoaders.add(loader);
    }

    @Override
    public SoundType getSoundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound, Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
        return new SoundType(volume, pitch, breakSound.get(), stepSound.get(), placeSound.get(), hitSound.get(), fallSound.get());
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<ItemStack> iconSupplier, CreativeModeTab.DisplayItemsGenerator itemGenerator) {
        return TAB_REGISTRY.register(name, () -> CreativeModeTab.builder()
                .title(Component.translatable("tab." + DungeonNowLoading.MOD_ID + "." + name))
                .icon(iconSupplier)
                .displayItems(itemGenerator)
                .build());
    }

    public static RegistryMap getRegistryMap() {
        return registryMap;
    }

    public static class RegistryMap {

        private final Map<ResourceLocation, DeferredRegister<?>> registries = new HashMap<>();

        private <T> Supplier<T> register(Registry<? super T> registry, String name, Supplier<T> entry) {
            DeferredRegister<T> reg = getDeferred(registry);
            return reg != null ? reg.register(name, entry) : null;
        }

        @SuppressWarnings({"unchecked"})
        public <T> DeferredRegister<T> getDeferred(Registry<? super T> registry) {
            return (DeferredRegister<T>)registries.computeIfAbsent(registry.key().location(), (key) -> {
                DeferredRegister<T> defReg = DeferredRegister.create(registry.key().location(), DungeonNowLoading.MOD_ID);
                if (modBus != null) {
                    defReg.register(modBus);
                }
                return defReg;
            });
        }

    }
}
