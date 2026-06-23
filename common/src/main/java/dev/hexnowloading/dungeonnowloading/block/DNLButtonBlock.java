package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class DNLButtonBlock extends ButtonBlock {
    public DNLButtonBlock(BlockBehaviour.Properties properties, BlockSetType blockSetType, int ticksToStayPressed, boolean arrowsCanPress) {
        super(blockSetType, ticksToStayPressed, properties);
    }
}
