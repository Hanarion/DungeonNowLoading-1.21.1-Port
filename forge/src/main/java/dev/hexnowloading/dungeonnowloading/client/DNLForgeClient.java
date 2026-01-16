package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.client.preview.PreviewOverlayForge;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;

public class DNLForgeClient {
    public static void init() {
        DNLPackets.registerClientbound();
        DNLPackets.registerServerbound();
        // register hologram preview overlay
        MinecraftForge.EVENT_BUS.addListener(PreviewOverlayForge::onRenderLevelStage);

        // Register block/item color providers on client without relying on Forge ColorHandlerEvent API
        try {
            var mc = Minecraft.getInstance();
            var blockColors = mc.getBlockColors();
            blockColors.register((state, world, pos, tintIndex) -> {
                if (tintIndex != 0 || world == null || pos == null) return 0xFFFFFFFF;
                var be = world.getBlockEntity(pos);
                if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.PotionBarrelBlockEntity barrel && barrel.getEffect() != null) {
                    return 0xFF000000 | barrel.getEffect().getColor();
                }
                return 0xFFFFFFFF;
            }, DNLBlocks.POTION_BARREL.get());

            var itemColors = mc.getItemColors();
            itemColors.register((stack, tintIndex) -> {
                if (tintIndex != 0) return 0xFFFFFFFF;
                CompoundTag tag = stack.getTag();
                if (tag == null) return 0xFFFFFFFF;
                if (!tag.contains("BlockEntityTag")) return 0xFFFFFFFF;
                var be = tag.getCompound("BlockEntityTag");
                if (!be.contains("Effect")) return 0xFFFFFFFF;
                try {
                    var id = new net.minecraft.resources.ResourceLocation(be.getString("Effect"));
                    var effect = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.get(id);
                    if (effect != null) return 0xFF000000 | effect.getColor();
                } catch (Exception ignored) {}
                return 0xFFFFFFFF;
            }, DNLItems.POTION_BARREL.get());
        } catch (Exception e) {
            DungeonNowLoading.LOGGER.warn("Failed to register Forge color providers in client init", e);
        }
    }

}
