package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class DNLPressurePlateBlock extends PressurePlateBlock{
    // 1.21 dropped the Sensitivity parameter; sensitivity now derives from the BlockSetType.
    public DNLPressurePlateBlock(BlockBehaviour.Properties properties, BlockSetType blockSetType) {
        super(blockSetType, properties);
    }
}
