package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class DNLPressurePlateBlock extends PressurePlateBlock{
    public DNLPressurePlateBlock(PressurePlateBlock.Sensitivity sensitivity, BlockBehaviour.Properties properties, BlockSetType blockSetType) {
        super(sensitivity, properties, blockSetType);
    }
}
