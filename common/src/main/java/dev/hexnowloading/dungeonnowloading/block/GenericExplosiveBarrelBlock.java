package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GenericExplosiveBarrelBlock extends FallingBlock implements SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16,14);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty FUSE = IntegerProperty.create("fuse", 0, 60);

    protected GenericExplosiveBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.FALSE).setValue(FUSE, 0));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FUSE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos signalPos, boolean moved) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            this.onImmediateTrigger(level, pos, null, TriggerCause.GENTLY_LIT_ON_FIRE);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (FallingBlock.isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            falling.disableDrop();
            level.removeBlock(pos, false);
        }
        int fuse = state.getValue(FUSE);
        if (fuse > 0) {
            int next = fuse - 1;
            level.setBlock(pos, state.setValue(FUSE, next), Block.UPDATE_ALL);
            if (next == 0) {
                this.handleDetonation(level, pos, null, state);
            } else {
                level.scheduleTick(pos, this, 1);
            }
        }
    }

    protected boolean replaceWithFireOnDetonate(BlockState state) {
        return false;
    }

    protected void prime(Level level, BlockPos pos, LivingEntity owner, int fuseTicks) {
        if (level.isClientSide) return;
        BlockState current = level.getBlockState(pos);
        if (!(current.getBlock() instanceof GenericExplosiveBarrelBlock)) {
            current = this.defaultBlockState();
            level.setBlock(pos, current, Block.UPDATE_ALL);
        }
        int clamped = Math.max(1, Math.min(fuseTicks, 60));
        int existing = current.hasProperty(FUSE) ? current.getValue(FUSE) : 0;
        if (existing == 0 || clamped < existing) {
            level.setBlock(pos, current.setValue(FUSE, clamped), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel sl) {
                sl.scheduleTick(pos, this, 1);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource source) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        int fuse = state.getValue(FUSE);
        if (fuse > 0) {
            if (source.nextFloat() < 0.5F) {
                level.addParticle(ParticleTypes.SMOKE, x + 0.5D, y + 1.0D, z + 0.5D, 0.0D, 0.0D, 0.0D);
            }
            int bursts = 2 + source.nextInt(3);
            for (int i = 0; i < bursts; i++) {
                double ox = 0.5D + (source.nextDouble() - 0.5D) * 0.2D;
                double oy = 1.05D;
                double oz = 0.5D + (source.nextDouble() - 0.5D) * 0.2D;
                double vx = (source.nextDouble() - 0.5D) * 0.05D;
                double vy = 0.30D + source.nextDouble() * 0.40D;
                double vz = (source.nextDouble() - 0.5D) * 0.05D;
                level.addParticle(ParticleTypes.LAVA, x + ox, y + oy, z + oz, vx, vy, vz);
            }
        } else if (source.nextBoolean()) {
            level.addParticle(ParticleTypes.SMOKE, x + 0.5D, y + 1.0D, z + 0.5D, 0.0D, 0.0D, 0.0D);
        }
    }

    public enum TriggerCause {
        FALL_IMPACT,
        IMPACT_BY_FALLING_BLOCK,
        PROJECTILE_TRIDENT,
        PROJECTILE_FLAMING_ARROW,
        PROJECTILE_FIRE_CHARGE,
        FIREWORK_EXPLOSION_NEARBY,
        NEARBY_EXPLOSION,
        PROJECTILE_NORMAL_ARROW,
        MINED_WITHOUT_SILK_TOUCH,
        GENTLY_LIT_ON_FIRE
    }

    public void onImmediateTrigger(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause) {
        this.prime(level, pos, owner, 8);
    }

    public void onFuseTrigger(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause, int fuseTicks) {
        this.prime(level, pos, owner, fuseTicks);
    }

    private void handleDetonation(Level level, BlockPos pos, LivingEntity owner, BlockState state) {
        this.removeNearbyArrows(level, pos);
        this.onDetonate(level, pos, owner);
        if (this.replaceWithFireOnDetonate(state)) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
        }
    }

    private void removeNearbyArrows(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel sl)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        AABB box = new AABB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D);
        for (AbstractArrow arrow : sl.getEntitiesOfClass(AbstractArrow.class, box)) {
            arrow.discard();
        }
    }

    protected void detonateNow(Level level, BlockPos pos, LivingEntity owner, TriggerCause cause, Projectile projectile) {
        if (level.isClientSide) return;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof GenericExplosiveBarrelBlock barrel)) {
            return;
        }
        projectile.discard();
        barrel.removeNearbyArrows(level, pos);
        barrel.onDetonate(level, pos, owner);
        if (barrel.replaceWithFireOnDetonate(state)) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.removeBlock(pos, false);
        }
    }

    protected abstract void onDetonate(Level level, BlockPos pos, LivingEntity owner);

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (!level.isClientSide) {
            BlockState fallingState = fallingBlockEntity.getBlockState();
            if (fallingState.getBlock() instanceof GenericExplosiveBarrelBlock barrel) {
                barrel.removeNearbyArrows(level, pos);
                barrel.onDetonate(level, pos, null);
            }
        }
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos bpos = hit.getBlockPos();
            LivingEntity owner = projectile.getOwner() instanceof LivingEntity l ? l : null;
            if (projectile.isOnFire() && projectile.mayInteract(level, bpos)) {
                this.detonateNow(level, bpos, owner, TriggerCause.PROJECTILE_FLAMING_ARROW, projectile);
            } else {
                this.onImmediateTrigger(level, bpos, owner, TriggerCause.PROJECTILE_NORMAL_ARROW);
            }
        }
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.FLINT_AND_STEEL) && !held.is(Items.FIRE_CHARGE)) {
            return super.use(state, level, pos, player, hand, hit);
        } else {
            this.onImmediateTrigger(level, pos, player, TriggerCause.GENTLY_LIT_ON_FIRE);
            if (!player.isCreative()) {
                if (held.is(Items.FLINT_AND_STEEL)) {
                    held.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                } else {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state1, BlockState state2, FallingBlockEntity fallingBlockEntity) {
        if (!level.isClientSide) {
            BlockState fallingState = fallingBlockEntity.getBlockState();
            if (fallingState.getBlock() instanceof GenericExplosiveBarrelBlock barrel) {
                barrel.removeNearbyArrows(level, pos);
                barrel.onDetonate(level, pos, null);
            }
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }
}