package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.property.SuspendedWebPart;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SuspendedWebBlock extends Block {
    public static final EnumProperty<SuspendedWebPart> PART = EnumProperty.create("part", SuspendedWebPart.class);
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.DOWN, Direction.SOUTH, Direction.WEST);
    private static final Vec3 ENTITY_SLOWDOWN = new Vec3(0.25D, 0.05D, 0.25D);
    private static final int MAX_STRUCTURE_LENGTH = 15;
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final ThreadLocal<Boolean> SUPPRESS_STRUCTURE_DROPS = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> CREATIVE_PLAYER_REMOVING = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Set<BlockPos>> PLAYER_REMOVED_POSITIONS = ThreadLocal.withInitial(HashSet::new);

    public SuspendedWebBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(PART, SuspendedWebPart.ONE).setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity living && !(living instanceof Spider)) {
            entity.makeStuckInBlock(state, ENTITY_SLOWDOWN);
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.getMainHandItem().is(Items.SHEARS) || player.getMainHandItem().getItem() instanceof SwordItem) {
            return Math.max(super.getDestroyProgress(state, player, level, pos), 0.125F);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Structure structure = findStructure(level, pos, state.getValue(FACING));
            if (structure != null) {
                markPlayerRemovedPositions(structure);
                if (!player.getAbilities().instabuild) {
                    if (player.getMainHandItem().is(Items.SHEARS)) {
                        popResource(level, pos, new ItemStack(DNLItems.SUSPENDED_WEB.get()));
                    } else {
                        dropConnectedStrings(level, pos, structure);
                    }
                }
                removeOtherParts(level, pos, structure);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (tool.is(Items.SHEARS)) {
            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
            return;
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock() && !CREATIVE_PLAYER_REMOVING.get()) {
            if (!removePlayerRemovedPosition(pos) && !SUPPRESS_STRUCTURE_DROPS.get()) {
                Structure structure = findStructure(level, pos, state.getValue(FACING));
                if (structure != null) {
                    dropConnectedStrings(level, pos, structure);
                    removeOtherParts(level, pos, structure);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        Structure structure = findStructure(level, pos, facing);
        return structure != null
                && structure.offsetOf(pos) >= 0
                && partFor(structure.length(), structure.offsetOf(pos)) == state.getValue(PART);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public static SuspendedWebPart partFor(int length, int offsetFromTop) {
        return switch (length) {
            case 2 -> offsetFromTop == 0 ? SuspendedWebPart.ONE : SuspendedWebPart.SIX;
            case 3 -> switch (offsetFromTop) {
                case 0 -> SuspendedWebPart.ONE;
                case 1 -> SuspendedWebPart.C;
                default -> SuspendedWebPart.SIX;
            };
            case 4 -> switch (offsetFromTop) {
                case 0 -> SuspendedWebPart.ONE;
                case 1 -> SuspendedWebPart.TWO;
                case 2 -> SuspendedWebPart.FIVE;
                default -> SuspendedWebPart.SIX;
            };
            case 5 -> switch (offsetFromTop) {
                case 0 -> SuspendedWebPart.ONE;
                case 1 -> SuspendedWebPart.TWO;
                case 2 -> SuspendedWebPart.B;
                case 3 -> SuspendedWebPart.FIVE;
                default -> SuspendedWebPart.SIX;
            };
            default -> {
                if (offsetFromTop == 0) yield SuspendedWebPart.ONE;
                if (offsetFromTop == 1) yield SuspendedWebPart.TWO;
                if (offsetFromTop == 2) yield SuspendedWebPart.THREE;
                if (offsetFromTop == length - 3) yield SuspendedWebPart.FOUR;
                if (offsetFromTop == length - 2) yield SuspendedWebPart.FIVE;
                if (offsetFromTop == length - 1) yield SuspendedWebPart.SIX;
                yield SuspendedWebPart.A;
            }
        };
    }

    private static Structure findStructure(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos start = pos;
        int blocksAbove = 0;
        while (blocksAbove < MAX_STRUCTURE_LENGTH - 1 && isMatchingWeb(level, start.relative(facing.getOpposite()), facing)) {
            start = start.relative(facing.getOpposite());
            blocksAbove++;
        }

        BlockPos end = pos;
        int length = blocksAbove + 1;
        while (length < MAX_STRUCTURE_LENGTH && isMatchingWeb(level, end.relative(facing), facing)) {
            end = end.relative(facing);
            length++;
        }

        if (length < 2 || length > MAX_STRUCTURE_LENGTH) {
            return null;
        }
        if (!isSupport(level, start.relative(facing.getOpposite()), facing)
                || !isSupport(level, end.relative(facing), facing.getOpposite())) {
            return null;
        }
        return new Structure(start, facing, length);
    }

    private static boolean isMatchingWeb(LevelReader level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof SuspendedWebBlock
                && state.hasProperty(FACING)
                && state.getValue(FACING) == facing;
    }

    public static boolean isCeiling(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.DOWN);
    }

    public static boolean isFloor(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
    }

    public static boolean isSupport(LevelReader level, BlockPos pos, Direction faceToStructure) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, faceToStructure);
    }

    public static void markPlayerRemovedStructure(LevelReader level, BlockPos pos, Direction facing) {
        Structure structure = findStructure(level, pos, facing);
        if (structure != null) {
            markPlayerRemovedPositions(structure);
        }
    }

    public static void beginCreativePlayerRemoval() {
        CREATIVE_PLAYER_REMOVING.set(true);
    }

    public static void endCreativePlayerRemoval() {
        CREATIVE_PLAYER_REMOVING.remove();
    }

    private static void dropConnectedStrings(Level level, BlockPos brokenPos, Structure structure) {
        int brokenOffset = structure.offsetOf(brokenPos);
        for (int offset = 0; offset < structure.length(); offset++) {
            BlockPos partPos = structure.start().relative(structure.facing(), offset);
            int distance = Math.abs(offset - brokenOffset);
            if (!partPos.equals(brokenPos)
                    && isMatchingWeb(level, partPos, structure.facing())
                    && (distance == 1 || level.random.nextFloat() < 0.5F)) {
                popResource(level, partPos, new ItemStack(Items.STRING));
            }
        }
    }

    private static void removeOtherParts(Level level, BlockPos brokenPos, Structure structure) {
        SUPPRESS_STRUCTURE_DROPS.set(true);
        try {
            for (int offset = 0; offset < structure.length(); offset++) {
                BlockPos partPos = structure.start().relative(structure.facing(), offset);
                if (!partPos.equals(brokenPos) && isMatchingWeb(level, partPos, structure.facing())) {
                    level.setBlock(partPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        } finally {
            SUPPRESS_STRUCTURE_DROPS.set(false);
        }
    }

    private static void markPlayerRemovedPositions(Structure structure) {
        Set<BlockPos> positions = PLAYER_REMOVED_POSITIONS.get();
        for (int offset = 0; offset < structure.length(); offset++) {
            positions.add(structure.start().relative(structure.facing(), offset).immutable());
        }
    }

    private static boolean removePlayerRemovedPosition(BlockPos pos) {
        Set<BlockPos> positions = PLAYER_REMOVED_POSITIONS.get();
        boolean removed = positions.remove(pos);
        if (positions.isEmpty()) {
            PLAYER_REMOVED_POSITIONS.remove();
        }
        return removed;
    }

    private record Structure(BlockPos start, Direction facing, int length) {
        private int offsetOf(BlockPos pos) {
            int dx = pos.getX() - start.getX();
            int dy = pos.getY() - start.getY();
            int dz = pos.getZ() - start.getZ();
            return dx * facing.getStepX() + dy * facing.getStepY() + dz * facing.getStepZ();
        }
    }
}
