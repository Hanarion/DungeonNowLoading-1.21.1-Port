package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.MendstoneChalkMarkBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendstoneChalkMarkBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack stack = ctx.getItemInHand();
        var player = ctx.getPlayer();

        // --- 1) REPAIR PATH: clicking an existing mark reduces damage by 32 (one stage) ---
        if (clickedState.is(markBlock)) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(clickedPos);
                if (be instanceof MendstoneChalkMarkBlockEntity markBe) {
                    int oldDamage = markBe.getDamage();
                    if (oldDamage > 0) {
                        int newDamage = Math.max(0, oldDamage - 32);
                        markBe.setDamage(newDamage);

                        int newOutline = newDamage / 32;
                        BlockState newState = clickedState
                                .setValue(MendstoneChalkMarkBlock.OUTLINE, newOutline)
                                .setValue(MendstoneChalkMarkBlock.LIT, true);
                        level.setBlock(clickedPos, newState, Block.UPDATE_ALL);
                        level.scheduleTick(clickedPos, (MendstoneChalkMarkBlock) markBlock, 20);
                        markBe.setChanged();

                        // fx
                        if (level instanceof ServerLevel sl) {
                            var dust = new DustColorTransitionOptions(
                                    new org.joml.Vector3f(0.45f, 0.80f, 1.0f),
                                    new org.joml.Vector3f(0.90f, 0.95f, 1.0f),
                                    1.0f
                            );
                            sl.sendParticles(dust, clickedPos.getX() + 0.5, clickedPos.getY() + 0.5, clickedPos.getZ() + 0.5,
                                    6, 0.5, 0.5, 0.5, 0.0);
                            level.playSound(null, clickedPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 0.6f, 1.2f);
                        }

                        if (player instanceof ServerPlayer sp) sp.awardStat(Stats.ITEM_USED.get(this));

                        // consume 1 durability (unless creative)
                        if (player != null && !player.getAbilities().instabuild) {
                            if (stack.isDamageableItem()) {
                                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
                            } else {
                                stack.shrink(1); // fallback if not damageable
                            }
                        }
                    } else {
                        // fully repaired already
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // --- 2) PLACEMENT PATH: supports partial placement when <5 dur left ---
        BlockPlaceContext placeCtx = new BlockPlaceContext(ctx);
        BlockPos placePos = placeCtx.getClickedPos();

// refuse if occupied by another mark or not replaceable
        if (level.getBlockState(placePos).is(markBlock)) return InteractionResult.FAIL;
        if (!level.getBlockState(placePos).canBeReplaced(placeCtx)) return InteractionResult.FAIL;

// Pick a face with FULL sturdy support (clicked face first)
        Direction[] candidates = placeCtx.getNearestLookingDirections();
        Direction clickedFace = placeCtx.getClickedFace();
        if (candidates.length == 0 || candidates[0] != clickedFace) {
            Direction[] withFirst = new Direction[candidates.length + 1];
            withFirst[0] = clickedFace;
            System.arraycopy(candidates, 0, withFirst, 1, candidates.length);
            candidates = withFirst;
        }
        Direction chosen = null;
        for (Direction dir : candidates) {
            BlockPos supportPos = placePos.relative(dir.getOpposite());
            BlockState support = level.getBlockState(supportPos);
            if (support.isFaceSturdy(level, supportPos, dir, SupportType.FULL)) {
                chosen = dir;
                break;
            }
        }
        if (chosen == null) return InteractionResult.FAIL;

// compute durability situation
        boolean creative = (player != null && player.getAbilities().instabuild);
        int consume = 0;            // how much durability to take
        int startDamage = 0;        // BE damage at start (0..160)

        if (!creative) {
            if (!stack.isDamageableItem()) {
                // fallback: require one item; if not enough, fail
                if (stack.getCount() <= 0) return InteractionResult.FAIL;
                consume = 1;
            } else {
                int max = stack.getMaxDamage();
                int cur = stack.getDamageValue();
                int remaining = Math.max(0, max - cur); // how many "points" left
                if (remaining <= 0) return InteractionResult.FAIL;

                if (remaining >= 5) {
                    consume = 5;           // fresh placement
                    startDamage = 0;
                } else {
                    // partial: translate the deficit to starting wear
                    int deficit = 5 - remaining;                 // 1..4
                    startDamage = deficit * 32;                  // 32 per stage
                    consume = remaining;                         // consume all; item breaks
                }
            }
        }

// Build initial state (keep your WATERLOGGED logic from getStateForPlacement)
        BlockState state = markBlock.getStateForPlacement(placeCtx);
        if (state == null) state = markBlock.defaultBlockState();
        state = state.setValue(MendstoneChalkMarkBlock.FACING, chosen)
                .setValue(MendstoneChalkMarkBlock.OUTLINE, startDamage / 32);

        if (!level.isClientSide) {
            if (!level.setBlock(placePos, state, Block.UPDATE_ALL)) return InteractionResult.FAIL;

            // init BE damage
            BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.MendstoneChalkMarkBlockEntity markBe) {
                markBe.setDamage(startDamage); // clamps 0..160
                markBe.setChanged();
            }

            // sounds/award
            SoundType st = state.getSoundType();
            level.playSound(null, placePos, DNLSounds.MENDSTONE_CHALK_DRAW.get(), SoundSource.BLOCKS,
                    (st.getVolume() + 1.0F) / 2.0F, st.getPitch() * 0.8F);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, placePos);
            if (player instanceof ServerPlayer sp) sp.awardStat(Stats.ITEM_USED.get(this));

            // consume durability
            if (!creative) {
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(consume, player, p -> p.broadcastBreakEvent(ctx.getHand()));
                } else {
                    stack.shrink(1);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);

    }

    @Override
    public boolean isValidRepairItem(ItemStack $$0, ItemStack $$1) {
        return false;
    }

    @Override public boolean isEnchantable(ItemStack stack) { return false; }
    @Override public int getEnchantmentValue() { return 0; }
}
