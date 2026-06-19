package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.block.WarningSignBlock;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.gameevent.GameEvent;

public class WarningSignBlockItem extends BlockItem {
    public WarningSignBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.getBlockState(pos).is(Blocks.IRON_BARS)) {
            return super.useOn(context);
        }

        if (!context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }

        BlockState state = getBlock().defaultBlockState()
                .setValue(WarningSignBlock.WALL, false)
                .setValue(WarningSignBlock.ROTATION, WarningSignBlock.rotationFor(context.getRotation()))
                .setValue(WarningSignBlock.WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
        if (!level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE)) {
            return InteractionResult.FAIL;
        }

        if (context.getPlayer() instanceof ServerPlayer player) {
            CriteriaTriggers.PLACED_BLOCK.trigger(player, pos, context.getItemInHand());
        }
        level.playSound(context.getPlayer(), pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS,
                (state.getSoundType().getVolume() + 1.0F) / 2.0F,
                state.getSoundType().getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(context.getPlayer(), state));
        context.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
