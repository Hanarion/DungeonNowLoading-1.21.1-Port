package dev.hexnowloading.dungeonnowloading.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.hexnowloading.dungeonnowloading.block.entity.DungeonBannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DungeonBannerBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final EnumProperty<DungeonBannerVariant> VARIANT = EnumProperty.create("variant", DungeonBannerVariant.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;


    private final DungeonBannerVariant defaultVariant;

    // Banner-like thin wall shape (you can keep your 2..16 Y bounds if you want)
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0, 2.0, 14.0, 16.0, 16.0, 16.0),
            Direction.SOUTH, Block.box(0.0, 2.0, 0.0, 16.0, 16.0, 2.0),
            Direction.WEST,  Block.box(14.0, 2.0, 0.0, 16.0, 16.0, 16.0),
            Direction.EAST,  Block.box(0.0, 2.0, 0.0, 2.0, 16.0, 16.0)
    ));

    public DungeonBannerBlock(DungeonBannerVariant defaultVariant, BlockBehaviour.Properties props) {
        super(props);
        this.defaultVariant = defaultVariant;

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(VARIANT, defaultVariant)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    // === Wall-banner placement behavior ===
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = this.defaultBlockState();
        LevelReader level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();

        // Same logic as WallBannerBlock: find a horizontal face the player is looking at that can survive
        for (Direction dir : ctx.getNearestLookingDirections()) {
            if (dir.getAxis().isHorizontal()) {
                Direction facing = dir.getOpposite(); // banner faces away from the wall
                state = state.setValue(FACING, facing).setValue(VARIANT, this.defaultVariant);

                if (state.canSurvive(level, pos)) {
                    return state;
                }
            }
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        // same as vanilla: solid block behind it
        return level.getBlockState(supportPos).isSolid();
        // If you want stricter/modern: return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {

        // If the supporting block changed and we can’t survive, pop off.
        if (dir == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // === Shape / rendering ===
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use MODEL unless you *already* registered a BER for DNLBlockEntityTypes.DUNGEON_BANNER on Fabric client.
        //return RenderShape.MODEL;
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonBannerBlockEntity(pos, state);
    }

    public enum DungeonBannerVariant implements StringRepresentable {
        SPAWNER_MAGENTA("spawner_magenta"),
        SPAWNER_BLACK("spawner_black"),
        SPAWNER_BLUE("spawner_blue"),
        SPAWNER_PURPLE("spawner_purple"),
        SPAWNER_GREEN("spawner_green"),
        HOLLOW("hollow"),
        SPAWNER_CARRIER("spawner_carrier"),
        EXPERIENCE_BOTTLE("experience_bottle"),
        CHAOS_SPAWNER("chaos_spawner"),
        WHIMPER_LANTERN("whimper_lantern"),
        GARHOLD_UPSIDEDOWN("garhold_upsidedown"),
        SKULL_OF_CHAOS("skull_of_chaos");

        private final String id;

        DungeonBannerVariant(String id) { this.id = id; }

        @Override
        public String getSerializedName() { return this.id; }
    }
}
