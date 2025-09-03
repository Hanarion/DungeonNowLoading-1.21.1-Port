package dev.hexnowloading.dungeonnowloading.mixin.forge.block;

import dev.hexnowloading.dungeonnowloading.block.AzuroLogBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AzuroLogBlock.class)
public abstract class AzuroLogBlockMixin extends RotatedPillarBlock {

    @Shadow public abstract RotatedPillarBlock getStripped();

    public AzuroLogBlockMixin(Properties properties) { super(properties); }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        ItemStack itemStack = context.getItemInHand();
        if (!itemStack.canPerformAction(toolAction)) return null;
        return toolAction == ToolActions.AXE_STRIP ? getStripped().defaultBlockState().setValue(AXIS, state.getValue(AXIS)) : null;
    }
}
