package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ScrapItem extends Item {
    private static final String ORIGINAL_TAG = "Original";

    public ScrapItem(Properties properties) {
        super(properties);
    }

    public static ItemStack ofOriginal(ItemStack original) {
        ItemStack scrap = new ItemStack(DNLItems.ITEM_SCRAPS.get());
        CompoundTag tag = scrap.getOrCreateTag();
        CompoundTag originalTag = new CompoundTag();
        original.save(originalTag);
        tag.put(ORIGINAL_TAG, originalTag);
        return scrap;
    }

    public static boolean hasOriginal(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(ORIGINAL_TAG, 10); // 10 = Compound
    }

    public static ItemStack getOriginal(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ORIGINAL_TAG, 10)) {
            return ItemStack.of(tag.getCompound(ORIGINAL_TAG));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (hasOriginal(stack)) {
            ItemStack original = getOriginal(stack);
            return Component.translatable("item.dungeonnowloading.item_scraps.named", original.getHoverName());
        }
        return super.getName(stack);
    }
}
