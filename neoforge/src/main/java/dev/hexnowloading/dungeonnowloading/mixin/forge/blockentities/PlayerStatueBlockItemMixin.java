package dev.hexnowloading.dungeonnowloading.mixin.forge.blockentities;

import dev.hexnowloading.dungeonnowloading.item.blockitem.PlayerStatueBlockItem;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.PlayerStatueItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(PlayerStatueBlockItem.class)
public abstract class PlayerStatueBlockItemMixin extends Item {

    public PlayerStatueBlockItemMixin(Properties properties) {
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
