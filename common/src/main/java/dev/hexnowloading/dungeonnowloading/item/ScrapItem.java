package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
            // Format: "{ItemName} Scrap"
            return Component.empty().append(original.getHoverName()).append(Component.literal(" Scrap"));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!hasOriginal(stack)) return;
        ItemStack original = getOriginal(stack);
        if (original.isEmpty()) return;
        Component nativeMat = findNativeRepairMaterialName(original);
        tooltip.add(Component.translatable("item.dungeonnowloading.item_scraps.tooltip.reconstruct.detail1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.dungeonnowloading.item_scraps.tooltip.reconstruct.detail2", nativeMat.copy().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.GRAY));
    }

    private static Component findNativeRepairMaterialName(ItemStack original) {
        try {
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack candidate = new ItemStack(item);
                if (!candidate.isEmpty() && original.getItem().isValidRepairItem(original, candidate)) {
                    return candidate.getHoverName();
                }
            }
        } catch (Exception ignored) {}
        return Component.literal("???");
    }
}
