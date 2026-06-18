package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public interface MimiclingFormItem {
    String getMimiclingForm();

    static int getMimiclingBarColor(ItemStack stack) {
        float durability = Math.max(0.0F, ((float)stack.getMaxDamage() - (float)stack.getDamageValue()) / (float)stack.getMaxDamage());
        float hue = 0.78F + (1.0F - durability) * 0.10F;
        return Mth.hsvToRgb(hue, 0.8F, 1.0F);
    }
}
