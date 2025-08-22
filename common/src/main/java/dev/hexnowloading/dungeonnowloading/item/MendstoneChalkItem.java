package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.MendstoneChalkMarkBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class MendstoneChalkItem extends Item {

    private final Block markBlock;

    public MendstoneChalkItem(Properties properties, Block markBlock) {
        super(properties);
        this.markBlock = markBlock;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos clickedPos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        BlockPos placePos = clickedPos.relative(face);
        BlockPlaceContext placeCtx = new BlockPlaceContext(ctx);

        if (!level.getBlockState(placePos).canBeReplaced(placeCtx)) {
            return InteractionResult.FAIL;
        }

        BlockState state = markBlock.getStateForPlacement(placeCtx);
        if (state == null) {
            state = markBlock.defaultBlockState().setValue(MendstoneChalkMarkBlock.FACING, face);
        }

        if (!state.canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = ctx.getItemInHand();

        if (!level.isClientSide) {
            boolean placed = level.setBlock(placePos, state, Block.UPDATE_ALL);
            if (!placed) return InteractionResult.FAIL;

            SoundType soundType = state.getSoundType();
            level.playSound(null, placePos, DNLSounds.MENDSTONE_CHALK_DRAW.get(), SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            level.gameEvent(ctx.getPlayer(), GameEvent.BLOCK_PLACE, placePos);

            if (ctx.getPlayer() instanceof ServerPlayer sp) {
                sp.awardStat(Stats.ITEM_USED.get(this));
            }

            // 🔽 Consume one use (max 3), but NOT in creative
            if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(1, ctx.getPlayer(), p -> p.broadcastBreakEvent(ctx.getHand()));
                } else {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // Optional: keep it strictly “3 uses” (no repair, no enchants)

    @Override
    public boolean isValidRepairItem(ItemStack $$0, ItemStack $$1) {
        return false;
    }

    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public int getEnchantmentValue() { return 0; }
}
