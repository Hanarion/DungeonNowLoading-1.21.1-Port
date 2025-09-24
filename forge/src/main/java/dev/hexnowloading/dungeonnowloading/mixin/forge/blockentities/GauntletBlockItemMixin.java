package dev.hexnowloading.dungeonnowloading.mixin.forge.blockentities;

import dev.hexnowloading.dungeonnowloading.item.GauntletBlockItem;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.PlayerStatueItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(GauntletBlockItem.class)
public abstract class GauntletBlockItemMixin extends Item {
    public GauntletBlockItemMixin(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private PlayerStatueItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = PlayerStatueItemRenderer.INSTANCE;
                }
                return renderer;
            }
        });
    }
}
