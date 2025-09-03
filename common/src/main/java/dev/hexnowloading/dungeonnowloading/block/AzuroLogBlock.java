package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class AzuroLogBlock extends RotatedPillarBlock {
    private final Supplier<RotatedPillarBlock> stripped;

    public AzuroLogBlock(BlockBehaviour.Properties props, Supplier<RotatedPillarBlock> stripped) {
        super(props.sound(SoundType.WOOD));
        this.stripped = stripped;
    }

    public RotatedPillarBlock getStripped() { return stripped.get(); }
}