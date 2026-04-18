package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RailPlatformBlock extends Block {

    public static final DirectionProperty FACING =
            DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public static final BooleanProperty RAISED =
            BooleanProperty.create("raised");

    // Top slab the rail visually sits on (4 px thick at the top)
    private static final VoxelShape OUTLINE_SHAPE =
            Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    // Extra vertical support: 4 px deep in one direction, 16 px wide perpendicular, 0–12 high
    private static final VoxelShape SPIDER_SUPPORT_N =
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 4.0D);   // north edge  (toward -Z)
    private static final VoxelShape SPIDER_SUPPORT_S =
            Block.box(0.0D, 0.0D, 12.0D, 16.0D, 12.0D, 16.0D); // south edge  (toward +Z)
    private static final VoxelShape SPIDER_SUPPORT_W =
            Block.box(0.0D, 0.0D, 0.0D, 4.0D, 12.0D, 16.0D);   // west edge   (toward -X)
    private static final VoxelShape SPIDER_SUPPORT_E =
            Block.box(12.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D); // east edge   (toward +X)

    public RailPlatformBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(RAISED, Boolean.FALSE));
    }

    /**
     * Outline (F3+B). We add the extra support piece when there is an
     * ascending lower RailPlatform along the same axis (N/S or E/W).
     */
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = OUTLINE_SHAPE;

        Direction lowerDir = getAscendingLowerDirection(level, pos, state);
        if (lowerDir != null) {
            VoxelShape support = switch (lowerDir) {
                case NORTH -> SPIDER_SUPPORT_N;
                case SOUTH -> SPIDER_SUPPORT_S;
                case WEST  -> SPIDER_SUPPORT_W;
                case EAST  -> SPIDER_SUPPORT_E;
                default    -> Shapes.empty();
            };
            shape = Shapes.or(shape, support);
        }

        return shape;
    }

    /**
     * Collision: only spiders collide.
     * Extra support is added when there is an ascending lower RailPlatform
     * along the same axis (N/S or E/W), regardless of THIS block's RAISED value.
     */
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext ecc && ecc.getEntity() instanceof Spider) {
            VoxelShape shape = OUTLINE_SHAPE;

            Direction lowerDir = getAscendingLowerDirection(level, pos, state);
            if (lowerDir != null) {
                VoxelShape support = switch (lowerDir) {
                    case NORTH -> SPIDER_SUPPORT_N;
                    case SOUTH -> SPIDER_SUPPORT_S;
                    case WEST  -> SPIDER_SUPPORT_W;
                    case EAST  -> SPIDER_SUPPORT_E;
                    default    -> Shapes.empty();
                };
                shape = Shapes.or(shape, support);
            }

            return shape;
        }

        // Everyone else falls through
        return Shapes.empty();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        // Still counts as a full solid top for rails
        return Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        Direction adjustedFacing = adjustFacingTowardUpperPlatform(level, pos, facing);

        BlockState base = this.defaultBlockState().setValue(FACING, adjustedFacing);
        boolean raised = shouldBeRaised(level, pos, base);
        return base.setValue(RAISED, raised);
    }

    @Override
    public void onPlace(BlockState state,
                        Level level,
                        BlockPos pos,
                        BlockState oldState,
                        boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide) {
            return;
        }

        // --- Recompute this block, in case it was placed under an existing upper ---
        Direction facing = state.getValue(FACING);
        Direction adjustedFacing = adjustFacingTowardUpperPlatform(level, pos, facing);
        BlockState base = state.setValue(FACING, adjustedFacing);
        boolean raised = shouldBeRaised(level, pos, base);
        BlockState newState = base.setValue(RAISED, raised);

        if (newState != state) {
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
            state = newState;
        }

        // --- Now update any LOWER platforms for which THIS is the upper one ---

        // We only care about platforms along this axis (N/S or E/W)
        Direction.Axis axis = state.getValue(FACING).getAxis();
        Direction dir1 = (axis == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction dir2 = (axis == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        for (Direction d : new Direction[]{dir1, dir2}) {
            BlockPos lowerPos = pos.relative(d).below();
            BlockState lowerState = level.getBlockState(lowerPos);

            if (!(lowerState.getBlock() instanceof RailPlatformBlock)) {
                continue;
            }

            // Re-run the same logic for the lower platform:
            Direction lowerFacing = lowerState.getValue(FACING);
            Direction lowerAdjustedFacing = adjustFacingTowardUpperPlatform(level, lowerPos, lowerFacing);
            BlockState lowerBase = lowerState.setValue(FACING, lowerAdjustedFacing);
            boolean lowerRaised = shouldBeRaised(level, lowerPos, lowerBase);
            BlockState lowerNew = lowerBase.setValue(RAISED, lowerRaised);

            if (lowerNew != lowerState) {
                level.setBlock(lowerPos, lowerNew, Block.UPDATE_CLIENTS);
            }
        }
    }


    private Direction adjustFacingTowardUpperPlatform(Level level, BlockPos pos, Direction originalFacing) {
        Direction.Axis axis = originalFacing.getAxis();

        // Along this axis, there are only 2 possible directions:
        // Z axis -> NORTH / SOUTH
        // X axis -> WEST / EAST
        Direction dir1 = (axis == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction dir2 = (axis == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        boolean hasUpper1 = hasUpperPlatformAlongAxis(level, pos, dir1, axis);
        boolean hasUpper2 = hasUpperPlatformAlongAxis(level, pos, dir2, axis);

        // Exactly one side has an upper platform → face that side
        if (hasUpper1 && !hasUpper2) {
            return dir1;
        } else if (!hasUpper1 && hasUpper2) {
            return dir2;
        }

        // Both or none → keep original placement direction
        return originalFacing;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, RAISED);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState neighborState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos neighborPos) {
        BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);

        Direction originalFacing = updated.getValue(FACING);
        Direction.Axis axis = originalFacing.getAxis();

        // Along this axis, check both sides for an upper RailPlatform
        Direction dir1 = (axis == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction dir2 = (axis == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        boolean hasUpper1 = hasUpperPlatformAlongAxis(level, pos, dir1, axis);
        boolean hasUpper2 = hasUpperPlatformAlongAxis(level, pos, dir2, axis);

        boolean raised = hasUpper1 || hasUpper2;

        // Decide new facing:
        // - If exactly one side has an upper platform → face that side.
        // - If both or none → keep original facing.
        Direction newFacing = originalFacing;
        if (raised) {
            if (hasUpper1 && !hasUpper2) {
                newFacing = dir1;
            } else if (!hasUpper1 && hasUpper2) {
                newFacing = dir2;
            }
        }

        return updated
                .setValue(FACING, newFacing)
                .setValue(RAISED, raised);
    }

    /**
     * A platform becomes RAISED if there is another RailPlatform one block above
     * and one block away along the same axis (N/S or E/W).
     *
     * i.e. NORTH + SOUTH are equivalent, EAST + WEST are equivalent here.
     * This is used when first placed; updateShape keeps it in sync.
     */
    private boolean shouldBeRaised(LevelAccessor level, BlockPos pos, BlockState state) {
        Direction.Axis axis = state.getValue(FACING).getAxis();

        Direction dir1 = (axis == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction dir2 = (axis == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        return hasUpperPlatformAlongAxis(level, pos, dir1, axis)
                || hasUpperPlatformAlongAxis(level, pos, dir2, axis);
    }

    /**
     * Helper: checks if there's an upper RailPlatform one block in direction d,
     * one block above, with a FACING on the same axis.
     */
    private boolean hasUpperPlatformAlongAxis(LevelAccessor level, BlockPos pos, Direction d, Direction.Axis axis) {
        BlockPos upperPos = pos.relative(d).above();
        BlockState upperState = level.getBlockState(upperPos);

        if (!(upperState.getBlock() instanceof RailPlatformBlock)) {
            return false;
        }

        Direction upperFacing = upperState.getValue(FACING);
        return upperFacing.getAxis() == axis;
    }

    /**
     * Returns the direction (N/E/S/W) toward a lower RailPlatform that:
     *  - is 1 block below and 1 block away along the same axis (N/S or E/W),
     *  - and has RAISED = true (i.e., it is ascending toward this block).
     *
     * If none, returns null.
     */
    @Nullable
    private Direction getAscendingLowerDirection(BlockGetter level, BlockPos pos, BlockState state) {
        Direction.Axis axis = state.getValue(FACING).getAxis();

        // Look for a lower platform along this axis: NORTH/SOUTH or EAST/WEST
        Direction dir1 = (axis == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction dir2 = (axis == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        Direction[] candidates = new Direction[]{dir1, dir2};

        for (Direction d : candidates) {
            BlockPos lowerPos = pos.relative(d).below();
            BlockState lowerState = level.getBlockState(lowerPos);

            if (lowerState.getBlock() instanceof RailPlatformBlock) {
                // "Ascending to this platform" = lower platform is RAISED
                if (lowerState.getValue(RAISED)) {
                    return d;
                }
            }
        }

        return null;
    }
}
