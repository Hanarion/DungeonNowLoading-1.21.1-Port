package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class MendingAuraChestBlock extends MendingAuraBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> CHEST_TYPE = BlockStateProperties.CHEST_TYPE;

    // Single-chest box: inset 1px on X/Z, 14px tall
    protected static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public MendingAuraChestBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(CHEST_TYPE, ChestType.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        super.createBlockStateDefinition(b); // keep WATERLOGGED etc. from MendingAuraBlock
        b.add(CHEST_TYPE);
    }

    // --- geometry & lighting ---
    @Override public boolean useShapeForLightOcclusion(BlockState state) { return true; }
    @Override public VoxelShape getShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) { return SHAPE; }
    @Override public VoxelShape getCollisionShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) { return SHAPE; }

    // Cull the internal face between connected halves
    @Override
    public boolean skipRendering(BlockState self, BlockState neighbor, Direction dir) {
        if (neighbor.is(this)
                && self.getValue(CHEST_TYPE) != ChestType.SINGLE
                && neighbor.getValue(CHEST_TYPE) != ChestType.SINGLE
                && self.getValue(FACING) == neighbor.getValue(FACING)) {
            Direction connectDir = connectionDirection(self);
            if (dir == connectDir) return true;
        }
        return super.skipRendering(self, neighbor, dir);
    }

    // --- placement & neighbor updates ---
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Start with MendingAuraBlock's placement (for WATERLOGGED etc.)
        BlockState base = super.getStateForPlacement(ctx);
        if (base == null) base = this.defaultBlockState();

        BlockState state = base
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(CHEST_TYPE, ChestType.SINGLE);

        // Auto-connect on placement unless player is sneaking (secondary use)
        if (!ctx.isSecondaryUseActive()) {
            state = tryConnectOnPlacement(ctx.getLevel(), ctx.getClickedPos(), state);
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState nbr, LevelAccessor level, BlockPos pos, BlockPos nbrPos) {
        // keep water ticks, etc.
        BlockState s = super.updateShape(state, dir, nbr, level, pos, nbrPos);
        // re-evaluate pairing after any neighbor change
        return recomputeConnection(level, pos, s);
    }

    // --- rotation/mirroring like chests ---
    @Override
    public BlockState rotate(BlockState s, Rotation r) {
        return s.setValue(FACING, r.rotate(s.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState s, Mirror m) {
        Direction facing = s.getValue(FACING);
        ChestType type   = s.getValue(CHEST_TYPE);
        BlockState out   = s.rotate(m.getRotation(facing));
        if (type != ChestType.SINGLE && (m == Mirror.LEFT_RIGHT || m == Mirror.FRONT_BACK)) {
            out = out.setValue(CHEST_TYPE, type == ChestType.LEFT ? ChestType.RIGHT : ChestType.LEFT);
        }
        return out;
    }

    // --- pairing helpers ---
    private BlockState tryConnectOnPlacement(LevelAccessor level, BlockPos pos, BlockState self) {
        Direction facing = self.getValue(FACING);
        BlockPos leftPos  = pos.relative(facing.getCounterClockWise());
        BlockPos rightPos = pos.relative(facing.getClockWise());

        BlockState left  = level.getBlockState(leftPos);
        BlockState right = level.getBlockState(rightPos);

        if (canConnect(self, left))  return self.setValue(CHEST_TYPE, ChestType.RIGHT);
        if (canConnect(self, right)) return self.setValue(CHEST_TYPE, ChestType.LEFT);
        return self;
    }

    private BlockState recomputeConnection(LevelAccessor level, BlockPos pos, BlockState self) {
        Direction facing = self.getValue(FACING);
        BlockState left  = level.getBlockState(pos.relative(facing.getCounterClockWise()));
        BlockState right = level.getBlockState(pos.relative(facing.getClockWise()));

        boolean leftOk  = canConnect(self, left)  && left.getValue(FACING) == facing && left.getValue(CHEST_TYPE) == ChestType.SINGLE;
        boolean rightOk = canConnect(self, right) && right.getValue(FACING) == facing && right.getValue(CHEST_TYPE) == ChestType.SINGLE;

        ChestType newType = ChestType.SINGLE;
        if (leftOk ^ rightOk) newType = leftOk ? ChestType.RIGHT : ChestType.LEFT;

        return self.setValue(CHEST_TYPE, newType);
    }

    private boolean canConnect(BlockState self, BlockState other) {
        return other.is(this);
        // If you want stricter rules (e.g., same WATERLOGGED, same lit state), add checks here.
    }

    // Direction toward the partner block
    private Direction connectionDirection(BlockState s) {
        Direction facing = s.getValue(FACING);
        return (s.getValue(CHEST_TYPE) == ChestType.LEFT) ? facing.getClockWise() : facing.getCounterClockWise();
    }
}
