package dev.hexnowloading.dungeonnowloading.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import dev.hexnowloading.dungeonnowloading.block.entity.DungeonBannerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class DungeonBannerBlock extends WallBannerBlock implements EntityBlock {
    public static final EnumProperty<DungeonBannerVariant> VARIANT =
            EnumProperty.create("variant", DungeonBannerVariant.class);
    private static final Map<Direction, VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
            Direction.NORTH, Block.box(0.0, 2.0, 14.0, 16.0, 16, 16.0),
            Direction.SOUTH, Block.box(0.0, 2.0, 0.0, 16.0, 16, 2.0),
            Direction.WEST, Block.box(14.0, 2.0, 0.0, 16.0, 16, 16.0),
            Direction.EAST, Block.box(0.0, 2.0, 0.0, 2.0, 16, 16.0)));

    public DungeonBannerBlock(DyeColor color, BlockBehaviour.Properties props) {
        super(color, props);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(VARIANT, DungeonBannerVariant.SPAWNER_MAGENTA));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    public VoxelShape getShape(BlockState $$0, BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
        return (VoxelShape)SHAPES.get($$0.getValue(FACING));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonBannerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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

        DungeonBannerVariant(String id) {
            this.id = id;
        }

        @Override
        public String getSerializedName() {
            return this.id; // used in blockstates: variant=<id>
        }
    }
}
