package dev.hexnowloading.dungeonnowloading.mixin.forge.item;

import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.ScorcherRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ScorcherItem.class)
public abstract class ScorcherItemMixin extends Item {

    public ScorcherItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ScorcherRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ScorcherRenderer();
                }
                return renderer;
            }
        });
    }
}
