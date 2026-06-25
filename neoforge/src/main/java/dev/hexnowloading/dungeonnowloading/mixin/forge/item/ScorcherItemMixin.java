package dev.hexnowloading.dungeonnowloading.mixin.forge.item;

import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.ScorcherRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ScorcherItem.class)
public abstract class ScorcherItemMixin extends Item {

    public ScorcherItemMixin(Properties properties) {
        super(properties);
    }

    /**
     * The Scorcher rewrites its CUSTOM_DATA component every tick while held/firing (heat, burn time,
     * animation StartTime). NeoForge's default {@code shouldCauseReequipAnimation} returns
     * {@code !oldStack.equals(newStack)}, which compares all data components — so the per-tick NBT
     * churn re-triggers the first-person re-equip ("lowering") animation every tick, making the gun
     * jump/drop in first person. Only re-equip when the item type actually changes or it moves slots.
     */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
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
