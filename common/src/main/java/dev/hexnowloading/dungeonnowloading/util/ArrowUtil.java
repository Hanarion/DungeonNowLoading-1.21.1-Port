package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.alchemy.PotionContents;

/**
 * 1.21 removed the {@code Arrow.potion} field; an arrow's potion is now carried by
 * the POTION_CONTENTS data component on its pickup item stack. This helper replaces
 * the old {@code arrow.potion != Potions.EMPTY} "is this a tipped/effect arrow?" check.
 */
public final class ArrowUtil {
    private ArrowUtil() {}

    public static boolean hasPotionEffects(AbstractArrow arrow) {
        PotionContents contents = arrow.getPickupItemStackOrigin()
                .getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().isPresent() || contents.hasEffects();
    }
}
