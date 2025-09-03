package dev.hexnowloading.dungeonnowloading.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DNLMenuUtil {
    @SuppressWarnings("unchecked")
    public static <C extends BlockEntity> C getBlockEntity(final Inventory playerInventory, final BlockPos pos, Class<C> clazz) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if(blockEntity != null && blockEntity.getClass() == clazz)
            return (C)blockEntity; // This unchecked cast is safe as BE.getClass is guaranteed to extend C.
        throw new IllegalStateException("BlockEntity at " + pos + " is not correct");
    }

}
