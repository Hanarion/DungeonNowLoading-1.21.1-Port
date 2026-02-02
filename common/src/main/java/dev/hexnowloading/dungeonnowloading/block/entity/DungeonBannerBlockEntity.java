package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonBannerBlockEntity extends BlockEntity {
    public DungeonBannerBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.DUNGEON_BANNER.get(), pos, state);
    }

}