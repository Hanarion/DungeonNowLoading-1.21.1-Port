package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.mixin.FireBlockAccessor;
import dev.hexnowloading.dungeonnowloading.platform.services.WoodBehaviorHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;

public class ForgeWoodBehaviorHelper implements WoodBehaviorHelper {
    @Override
    public void registerFlammable(Block block, int encouragement, int flammability) {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        FireBlockAccessor accessor = (FireBlockAccessor) fire;
        accessor.dungeonnowloading$setFlammable(block, encouragement, flammability);
    }

    @Override
    public void registerStrippable(Block input, Block stripped) {
        // Forge uses AzuroLogBlockMixin#getToolModifiedState for stripping.
    }
}
