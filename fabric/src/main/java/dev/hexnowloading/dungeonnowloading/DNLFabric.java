package dev.hexnowloading.dungeonnowloading;

import dev.hexnowloading.dungeonnowloading.block.entity.PotionBarrelBlockEntity;
import dev.hexnowloading.dungeonnowloading.events.DNLFabricBlockEvents;
import dev.hexnowloading.dungeonnowloading.menu.MendingTableMenu;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLMenuTypes;
import dev.hexnowloading.dungeonnowloading.server.entity.DNLFabricEntities;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class DNLFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DungeonNowLoading.init();
        // Register & bootstrap Fabric menu type (simple, no extra data buffer needed)
        Supplier<MenuType<MendingTableMenu>> mendingTable = Services.REGISTRY.register(
                BuiltInRegistries.MENU,
                "mending_table",
                () -> new MenuType<>((id, inv) -> new MendingTableMenu(id, inv), FeatureFlags.VANILLA_SET)
        );
        DNLMenuTypes.bootstrap(mendingTable);

        // Register Fabric strippable mapping (Forge handled via mixin)
        StrippableBlockRegistry.register(DNLBlocks.AZURO_OAK_LOG.get(), DNLBlocks.STRIPPED_AZURO_OAK_LOG.get());


        FlammableBlockRegistry flammable = FlammableBlockRegistry.getDefaultInstance();

        flammable.add(DNLBlocks.AZURO_LEAVES.get(), 30, 60);
        flammable.add(DNLBlocks.AZURO_HANGING_LEAVES.get(), 30, 60);
        flammable.add(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get(), 30, 60);

        flammable.add(DNLBlocks.AZURO_OAK_LOG.get(), 5, 5);
        flammable.add(DNLBlocks.STRIPPED_AZURO_OAK_LOG.get(), 5, 5);

        flammable.add(DNLBlocks.AZURO_OAK_PLANKS.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_PLANK_FENCE.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_PLANK_FENCE_GATE.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_PLANK_SLAB.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_PLANK_STAIRS.get(), 5, 20);

        flammable.add(DNLBlocks.AZURO_OAK_DOOR.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_BUTTON.get(), 5, 20);
        flammable.add(DNLBlocks.AZURO_OAK_PRESSURE_PLATE.get(), 5, 20);

        registerEvents();
        registerEntityAttributes();
        registerPackets();
        DNLFabricEntities.registerSpawnPlacements();

        ServerLifecycleEvents.SERVER_STARTING.register(PatronRegistry::initOrReload);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, rm, success) -> {
            PatronRegistry.initOrReload(server);
        });

        registerBlockColors();

        // Register item colors so the potion barrel gets tinted in inventories/hand
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex != 0) return 0xFFFFFFFF;
            CompoundTag tag = stack.getTag();
            if (tag == null) return 0xFFFFFFFF;
            if (!tag.contains("BlockEntityTag", 10)) return 0xFFFFFFFF;
            CompoundTag be = tag.getCompound("BlockEntityTag");
            if (!be.contains("Effect", 8)) return 0xFFFFFFFF;
            try {
                ResourceLocation id = new ResourceLocation(be.getString("Effect"));
                MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(id);
                if (effect != null) {
                    int rgb = effect.getColor();
                    return 0xFF000000 | rgb;
                }
            } catch (Exception ignored) {}
            return 0xFFFFFFFF;
        }, DNLItems.POTION_BARREL.get());

        DungeonNowLoading.LOGGER.info("Hello Fabric world!");
    }

    private void registerEvents() { DNLFabricBlockEvents.init(); }

    private void registerEntityAttributes() {
        for (EntityType<? extends LivingEntity> type : DNLEntityTypes.getAllAttributes().keySet()) {
            FabricDefaultAttributeRegistry.register(type, DNLEntityTypes.getAllAttributes().get(type));
        }
    }

    private void registerBlockColors() {
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> {
                    if (tintIndex != 0 || world == null || pos == null) {
                        return 0xFFFFFFFF; // no tint
                    }

                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof PotionBarrelBlockEntity barrel && barrel.getEffect() != null) {
                        int rgb = barrel.getEffect().getColor(); // 0xRRGGBB
                        return 0xFF000000 | rgb; // force full alpha
                    }

                    return 0xFFFFFFFF;
                },
                DNLBlocks.POTION_BARREL.get() // your Fabric block registry object
        );
    }


    private void registerPackets() { }
}
