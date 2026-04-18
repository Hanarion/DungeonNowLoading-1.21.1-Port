package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface ZoneReceiverBlockEntity {
    /**
     * @param cornerAWorld world-space corner A
     * @param cornerBWorld world-space corner B
     * @param authoredFacing direction the zone was authored in (usually the block's current facing)
     */
    void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing);
}
