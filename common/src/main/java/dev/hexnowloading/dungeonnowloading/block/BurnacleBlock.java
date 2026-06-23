package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.BurnacleBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class BurnacleBlock extends Block implements EntityBlock {

    // Burnacle faces outward from this direction (away from the supporting block).
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);
    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 3);

    private static final Map<Stage, Map<Direction, VoxelShape>> STAGE_SHAPES = new EnumMap<>(Stage.class);

    /** Size presets per stage. Dimensions are in voxel units. */
    public enum HitboxPreset {
        BUD      (14, 7, 14),
        JUVENILE (14, 9, 14),
        MATURE   (14, 11, 14),
        ELDER    (14, 16, 14);

        final Map<Direction, VoxelShape> oriented;

        HitboxPreset(int width, int height, int depth) {
            this.oriented = buildOrientedShapes(width, height, depth);
        }
    }
    /**
     * Central place to tune gas / tick behavior by Burnacle size.
     * Fields map to your NBT:
     *
     *  gasSize           -> "GasSize"               (int)
     *  growthTime        -> "GrowthTime"            (int)
     *  gasSpread         -> "GasSpread"             (float)
     *  gasSpeed          -> "GasSpeed"              (float)
     *  airResistance     -> "AirResistance"         (float)
     *  life              -> "Life"                  (int)
     *  explosionMultiplier -> "ExplosionMultiplier" (float)
     *  chainDelay        -> "ChainDelay"            (int, baseChainedDelay)
     *  ignitionDelay     -> "IgnitionDelay"         (int, baseIgnitionDelay)
     *  explosionDelay    -> "ExplosionDelay"        (int, baseExplosionDelay)
     *
     * Plus:
     *  emissionInterval  -> how often it emits gas
     *  emissionJitter    -> random extra delay (0..jitter)
     */
    private static final Map<Stage, GasCloudSettings> GAS_CONFIG = new EnumMap<>(Stage.class);

    // -----------------------------------------------------------------------------
// Unified presets per Burnacle stage
// This controls BOTH gas NBT defaults and BE behavior defaults.
// If the BE or GasEntity NBT doesn't override a value, it falls back here.
// -----------------------------------------------------------------------------
    public record StagePreset(
            // Gas NBT defaults
            int    gasSize,
            int    growthTime,
            float  gasSpread,
            float  gasSpeed,
            float  airResistance,
            int    life,
            float  explosionMultiplier,
            int    chainDelay,
            int    ignitionDelay,
            int    explosionDelay,

            // BlockEntity behavior defaults
            int    cycleTime,
            int    cycleOffset,
            double initialGasSpeed,
            double playerRange
    ) {}

    private static final Map<Stage, StagePreset> STAGE_PRESETS = new EnumMap<>(Stage.class);

    static {
        // BUD preset
        STAGE_PRESETS.put(Stage.BUD, new StagePreset(
                // --- gas ---
                1,      // gasSize
                40,     // growthTime
                0.5f,   // gasSpread
                0.02f,  // gasSpeed
                0.1f,  // airResistance
                100,    // life
                0.4f,   // explosionMultiplier
                5,      // chainDelay
                10,     // ignitionDelay
                25,     // explosionDelay

                // --- behavior ---
                160,    // cycleTime
                0,      // cycleOffset
                0.2D, // initialGasSpeed
                16.0D   // playerRange
        ));

        // JUVENILE preset
        STAGE_PRESETS.put(Stage.JUVENILE, new StagePreset(
                2,      // gasSize
                40,     // growthTime
                1.0f,   // gasSpread
                0.02f,  // gasSpeed
                0.1f,  // airResistance
                100,    // life
                0.4f,   // explosionMultiplier
                5,      // chainDelay
                10,     // ignitionDelay
                25,     // explosionDelay

                // --- behavior ---
                160,    // cycleTime
                0,      // cycleOffset
                0.2D, // initialGasSpeed
                16.0D   // playerRange
        ));

        // MATURE preset (your original summon example, with stronger behavior)
        STAGE_PRESETS.put(Stage.MATURE, new StagePreset(
                3,      // gasSize
                40,     // growthTime
                1.5f,   // gasSpread
                0.02f,  // gasSpeed
                0.1f,  // airResistance
                100,    // life
                0.4f,   // explosionMultiplier
                5,      // chainDelay
                10,     // ignitionDelay
                25,     // explosionDelay

                // --- behavior ---
                160,    // cycleTime
                0,      // cycleOffset
                0.2D, // initialGasSpeed
                16.0D   // playerRange
        ));

        // ELDER preset
        STAGE_PRESETS.put(Stage.ELDER, new StagePreset(
                4,      // gasSize
                40,     // growthTime
                2.0f,   // gasSpread
                0.02f,  // gasSpeed
                0.1f,  // airResistance
                100,    // life
                0.4f,   // explosionMultiplier
                5,      // chainDelay
                10,     // ignitionDelay
                25,     // explosionDelay

                // --- behavior ---
                160,    // cycleTime
                0,      // cycleOffset
                0.2D, // initialGasSpeed
                16.0D   // playerRange
        ));
    }

    // Helper for BE
    public static StagePreset getPreset(Stage stage) {
        return STAGE_PRESETS.get(stage);
    }


    public BurnacleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.UP)
                        .setValue(STAGE, Stage.BUD)
                        .setValue(ROTATION, 0)
        );
    }

    /* -------------------------------------------------------------------------
     * Blockstate / placement
     * ---------------------------------------------------------------------- */

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, ROTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Faces outward from the clicked face.
        // If attached on ceiling, FACING = DOWN → emits downward.
        Direction face = context.getClickedFace();
        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(STAGE, Stage.BUD)
                .setValue(ROTATION, getRotationForPos(context.getClickedPos()));
    }

    private static int getRotationForPos(BlockPos pos) {
        long seed = Mth.getSeed(pos.getX(), pos.getY(), pos.getZ());
        return (int) Math.floorMod(seed, 4L);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        // Needs solid surface on the side we cling to
        return supportState.isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState neighborState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state,
                                Level level,
                                BlockPos pos,
                                Block neighborBlock,
                                BlockPos neighborPos,
                                boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide()) {
            if (!canSurvive(state, level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Shape / collision
     * ---------------------------------------------------------------------- */

    @Override
    public VoxelShape getShape(BlockState state,
                               BlockGetter level,
                               BlockPos pos,
                               CollisionContext ctx) {
        Stage stage = state.getValue(STAGE);
        Direction facing = state.getValue(FACING);

        Map<Direction, VoxelShape> byDir = STAGE_SHAPES.get(stage);
        if (byDir == null) {
            // Fallback: shouldn't happen, but just in case
            return Block.box(0, 0, 0, 16, 16, 16);
        }
        VoxelShape shape = byDir.get(facing);
        if (shape == null) {
            // Fallback to UP if somehow missing
            return byDir.getOrDefault(Direction.UP, Block.box(0, 0, 0, 16, 16, 16));
        }
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state,
                                        BlockGetter level,
                                        BlockPos pos,
                                        CollisionContext ctx) {
        // Same as outline shape
        return getShape(state, level, pos, ctx);
    }

    @Override
    protected boolean isPathfindable(BlockState state,
                                  PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }



    private static Map<Direction, VoxelShape> buildOrientedShapes(int width, int height, int depth) {
        Map<Direction, VoxelShape> m = new EnumMap<>(Direction.class);

        double minX = (16.0D - width) / 2.0D;
        double maxX = minX + width;
        double minY = (16.0D - width) / 2.0D;
        double maxY = minY + width;
        double minZ = (16.0D - depth) / 2.0D;
        double maxZ = minZ + depth;

        VoxelShape up    = Block.box(minX, 0.0D,       minZ, maxX, height,      maxZ);
        VoxelShape down  = Block.box(minX, 16.0D - height, minZ, maxX, 16.0D,      maxZ);
        VoxelShape north = Block.box(minX, minY, 16.0D - height, maxX, maxY, 16.0D);
        VoxelShape south = Block.box(minX, minY, 0.0D,       maxX, maxY, height);
        VoxelShape east  = Block.box(0.0D, minY, minZ, height,      maxY, maxZ);
        VoxelShape west  = Block.box(16.0D - height, minY, minZ, 16.0D,      maxY, maxZ);

        m.put(Direction.UP,    up);
        m.put(Direction.DOWN,  down);
        m.put(Direction.NORTH, north);
        m.put(Direction.SOUTH, south);
        m.put(Direction.EAST,  east);
        m.put(Direction.WEST,  west);
        return m;
    }

    static {
        // --- your GAS_CONFIG static init stays as-is above or below this ---

        // Map stages -> presets
        STAGE_SHAPES.put(Stage.BUD,      HitboxPreset.BUD.oriented);
        STAGE_SHAPES.put(Stage.JUVENILE, HitboxPreset.JUVENILE.oriented);
        STAGE_SHAPES.put(Stage.MATURE,   HitboxPreset.MATURE.oriented);
        STAGE_SHAPES.put(Stage.ELDER,    HitboxPreset.ELDER.oriented);
    }



    /* -------------------------------------------------------------------------
     * Ticking & gas emission
     * ---------------------------------------------------------------------- */

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack,
                                 BlockState state,
                                 Level level,
                                 BlockPos pos,
                                 Player player,
                                 InteractionHand hand,
                                 BlockHitResult hit) {
        // Only handle bone meal
        if (!stack.is(Items.BONE_MEAL)) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Stage stage = state.getValue(STAGE);

        // Already max size → let other interactions handle it
        if (stage == Stage.ELDER) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // We will grow it: consume item + change state
        if (!level.isClientSide) {
            Stage next = switch (stage) {
                case BUD -> Stage.JUVENILE;
                case JUVENILE -> Stage.MATURE;
                case MATURE -> Stage.ELDER;
                case ELDER -> Stage.ELDER; // unreachable due to early return, but safe
            };

            BlockState newState = state.setValue(STAGE, next);
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            // Vanilla bone meal particle effect
            level.levelEvent(1505, pos, 0);
        }

        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BurnacleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                  BlockState state,
                                                                  BlockEntityType<T> type) {
        return type == DNLBlockEntityTypes.BURNACLE.get()
                ? level.isClientSide
                ? (lvl, pos, st, be) -> BurnacleBlockEntity.clientTick(lvl, pos, st, (BurnacleBlockEntity) be)
                : (lvl, pos, st, be) -> BurnacleBlockEntity.serverTick(lvl, pos, st, (BurnacleBlockEntity) be)
                : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int type) {
        super.triggerEvent(state, level, pos, id, type);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, type);
    }

    /* -------------------------------------------------------------------------
     * Types
     * ---------------------------------------------------------------------- */

    public static GasCloudSettings getGasSettings(Stage stage) {
        return GAS_CONFIG.get(stage);
    }

    public enum Stage implements StringRepresentable {
        BUD("bud"),
        JUVENILE("juvenile"),
        MATURE("mature"),
        ELDER("elder");

        private final String name;

        Stage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public record GasCloudSettings(
            int gasSize,
            int growthTime,
            float gasSpread,
            float gasSpeed,
            float airResistance,
            int life,
            float explosionMultiplier,
            int chainDelay,
            int ignitionDelay,
            int explosionDelay,
            int emissionInterval,
            int emissionJitter
    ) {
    }
}
