package dev.hexnowloading.dungeonnowloading.mixin.forge.blockentities;

import dev.hexnowloading.dungeonnowloading.block.client.renderer.DungeonBannerBlockItemRenderer;
import dev.hexnowloading.dungeonnowloading.item.blockitem.DungeonBannerBlockItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(DungeonBannerBlockItem.class)
public abstract class DungeonBannerBlockItemMixin extends Item {

    public DungeonBannerBlockItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return DungeonBannerBlockItemRenderer.getInstance();
            }
        });
    }
}
