package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Half-block oil spill layer, non-stackable.
 * Behaves like a falling layer and is slippery via its friction property.
 */
public class OilSpillBlock extends FallingBlock {

    // 8 px (0.5 block) high collision & outline shape
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public static final BooleanProperty HEATING = BooleanProperty.create("heating");
    public static final IntegerProperty HEAT_TICKS = IntegerProperty.create("heat_ticks", 0, 60);

    public OilSpillBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HEATING, Boolean.FALSE)
                .setValue(HEAT_TICKS, 0));
    }

    // --- Blockstate ---

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEATING, HEAT_TICKS);
    }

    // --- Shapes ---

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // --- Interaction / ignition ---

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
            // Faster heating: shorter initial duration
            startHeating((ServerLevel) level, pos, 20 + level.random.nextInt(10));
            if (!player.isCreative()) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                } else {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // --- Placement / falling support ---

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            // if support below is non-solid, clear it (replace soft blocks)
            if (!belowState.isSolid()) {
                level.setBlock(below, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            // schedule a tick so FallingBlock logic can engage if unsupported
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
        if (!level.isClientSide) {
            boolean nearbyFire = false;

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(dir)).is(net.minecraft.world.level.block.Blocks.FIRE)) {
                    nearbyFire = true;
                    break;
                }
            }

            if (!nearbyFire) {
                BlockPos[] diagonals = new BlockPos[] {
                        pos.offset(-1, 0, -1),
                        pos.offset(1, 0, -1),
                        pos.offset(-1, 0, 1),
                        pos.offset(1, 0, 1)
                };
                for (BlockPos dp : diagonals) {
                    if (level.getBlockState(dp).is(net.minecraft.world.level.block.Blocks.FIRE)) {
                        nearbyFire = true;
                        break;
                    }
                }
            }

            if (nearbyFire) {
                // Faster heating when near fire
                startHeating((ServerLevel) level, pos, 20 + level.random.nextInt(10));
            }

            // ensure support check on any neighbor change
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hitResult, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            // Faster heating on flaming projectile
            startHeating((ServerLevel) level, hitResult.getBlockPos(), 20 + level.random.nextInt(10));
        }
    }

    // --- Heating / ticking ---

    private void startHeating(ServerLevel level, BlockPos pos, int ticks) {
        BlockState s = level.getBlockState(pos);
        if (!(s.getBlock() instanceof OilSpillBlock)) return;
        if (!s.getValue(HEATING)) {
            s = s.setValue(HEATING, true).setValue(HEAT_TICKS, ticks);
            level.setBlock(pos, s, Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.8F, 1.0F);
            level.scheduleTick(pos, this, 1);
        }
    }

    private void igniteSelf(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Let FallingBlock handle gravity (spawn falling entity when unsupported)
        super.tick(state, level, pos, random);

        if (state.getValue(HEATING)) {
            int t = state.getValue(HEAT_TICKS);
            // Faster decrement per tick
            int next = Math.max(0, t - 2);

            // Keep feedback roughly similar but scale with faster ticks
            if (next % 6 == 0) {
                level.playSound(null, pos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 0.6F, 1.0F);
                level.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5D, pos.getY() + 0.1D, pos.getZ() + 0.5D,
                        3, 0.06D, 0.01D, 0.06D, 0.0D
                );
            }

            if (next == 0) {
                level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1.0F, 1.0F);
                igniteSelf(level, pos);
            } else {
                level.setBlock(pos, state.setValue(HEAT_TICKS, next), Block.UPDATE_ALL);
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    // --- Placement rules (no stacking) ---

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        BlockState below = level.getBlockState(pos.below());

        if (below.getBlock() instanceof OilSpillBlock) {
            return null; // cancel placement if directly on oil
        }
        return super.getStateForPlacement(ctx);
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state1, BlockState state2, FallingBlockEntity fallingBlockEntity) {
        // If landing on another oil spill, do not stack: discard and do not place
        if (state2.getBlock() instanceof OilSpillBlock) {
            fallingBlockEntity.discard();
            // defensive: ensure we don't leave a double layer
            if (level.getBlockState(pos).getBlock() instanceof OilSpillBlock) {
                level.removeBlock(pos, false);
            }
            return;
        }
        // Otherwise default landing behavior
        super.onLand(level, pos, state1, state2, fallingBlockEntity);
    }

    // Make oil spill replaceable so placement logic treats it as a soft layer
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }
}
