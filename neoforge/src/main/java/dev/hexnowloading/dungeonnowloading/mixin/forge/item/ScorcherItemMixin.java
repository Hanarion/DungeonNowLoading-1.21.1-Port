package dev.hexnowloading.dungeonnowloading.mixin.forge.item;

import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.ScorcherRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

// First-person re-equip ("lowering") jump from the Scorcher's per-tick CUSTOM_DATA churn is handled
// cross-loader by the common items.ItemInHandRendererMixin (matches DNLAnimatedItems by type), so no
// NeoForge-specific shouldCauseReequipAnimation override is needed here.
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
