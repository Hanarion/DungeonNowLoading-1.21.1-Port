package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class WebCarpetBlock extends MultifaceBlock {

    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    private static final BooleanProperty FACE_UP    = MultifaceBlock.getFaceProperty(Direction.UP);
    private static final BooleanProperty FACE_DOWN  = MultifaceBlock.getFaceProperty(Direction.DOWN);
    private static final BooleanProperty FACE_NORTH = MultifaceBlock.getFaceProperty(Direction.NORTH);
    private static final BooleanProperty FACE_SOUTH = MultifaceBlock.getFaceProperty(Direction.SOUTH);
    private static final BooleanProperty FACE_EAST  = MultifaceBlock.getFaceProperty(Direction.EAST);
    private static final BooleanProperty FACE_WEST  = MultifaceBlock.getFaceProperty(Direction.WEST);

    public static final BooleanProperty BURNING = BooleanProperty.create("burning");

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public WebCarpetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(BURNING, Boolean.FALSE));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BURNING);
    }

    // --- Shape / collision ---

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return super.getShape(state, level, pos, ctx);
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Let entities pass through; we only slow them, not stand on it
        return Shapes.empty();
    }

    // --- Slowing + natural breaking ---

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // First, check if the entity actually intersects the multiface shape,
        // not just the full block cube.
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        // Shape is in local [0,1] coords, move it to world space
        AABB webBox = shape.bounds().move(pos);

        // If the entity's hitbox doesn't touch the web geometry at all, do nothing
        if (!webBox.intersects(entity.getBoundingBox())) {
            return;
        }

        // → apply slowdown like cobweb
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));

        if (level.isClientSide) return;
        if (!(entity instanceof LivingEntity)) return;

        // Check if entity is actually trying to move
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;
        double sq = dx * dx + dy * dy + dz * dz;

        boolean isMoving = sq > 0.0D;

        if (isMoving) {
            if (level.random.nextInt(20) == 0) {
                level.destroyBlock(pos, true, entity);
            }
        }
    }

    // --- Burning logic ---

    /**
     * Called by neighbor fire or chain spread to start the burning phase:
     *  normal -> burning (visual) -> fire (via tick)
     */
    private void ignite(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getValue(BURNING)) return; // already burning

        // 1) Switch to burning state (for texture swap)
        BlockState burningState = state.setValue(BURNING, Boolean.TRUE);
        level.setBlock(pos, burningState, Block.UPDATE_ALL);

        // 2) Immediately ignite entities currently in the web
        VoxelShape shape = burningState.getShape(level, pos);
        AABB box = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);

        List<Entity> entities = level.getEntities(null, box);
        for (Entity e : entities) {
            if (e instanceof LivingEntity living) {
                // Set on fire as soon as it ignites
                living.setSecondsOnFire(3);
            }
        }

        // 3) Schedule burnout in a short delay (e.g. 5 ticks)
        level.scheduleTick(pos, this, 5);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Only do burning logic when in burning state
        if (!state.getValue(BURNING)) {
            return;
        }

        burnOutAndSpread(level, pos, state);
    }

    /**
     * Burning web -> remove web & place fire, then ignite neighbouring webs.
     */
    private void burnOutAndSpread(ServerLevel level, BlockPos pos, BlockState state) {
        // Decide how the fire should face based on the web's faces
        BlockState fireState = Blocks.FIRE.defaultBlockState();

        if (!state.getValue(FACE_DOWN)) {
            Direction chosenFace = null;
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN) continue;
                BooleanProperty faceProp = MultifaceBlock.getFaceProperty(dir);
                if (faceProp != null && state.getValue(faceProp)) {
                    chosenFace = dir;
                    break;
                }
            }

            if (chosenFace != null) {
                BooleanProperty fireFaceProp = switch (chosenFace) {
                    case NORTH -> FireBlock.NORTH;
                    case EAST  -> FireBlock.EAST;
                    case SOUTH -> FireBlock.SOUTH;
                    case WEST  -> FireBlock.WEST;
                    case UP    -> FireBlock.UP;
                    case DOWN  -> FireBlock.UP; // shouldn't happen here
                };

                fireState = fireState
                        .setValue(FireBlock.UP, false)
                        .setValue(fireFaceProp, true);
            }
        }

        // Remove this web (burned away)
        if (!level.isEmptyBlock(pos)) {
            level.removeBlock(pos, false);
        }

        // Try to place fire at this position
        boolean placedFire = false;
        if (level.getBlockState(pos).isAir() && fireState.canSurvive(level, pos)) {
            level.setBlock(pos, fireState, Block.UPDATE_ALL);
            placedFire = true;
        }

        // If fire couldn't be placed, stop spread here
        if (!placedFire) {
            return;
        }

        // Orthogonal spread as before
        for (Direction dir : Direction.values()) {
            BlockPos nextPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(nextPos);

            if (neighbor.getBlock() instanceof WebCarpetBlock) {
                ignite(level, nextPos, neighbor);
            }
        }

        // NEW: diagonal “around the corner” rules
        igniteDiagonalWebConnections(level, pos, state);
    }

    private void igniteDiagonalWebConnections(ServerLevel level, BlockPos pos, BlockState state) {
        // Helper to check corner solidity
        java.util.function.Predicate<BlockPos> isCornerBlocked =
                cornerPos -> level.getBlockState(cornerPos).isSolid();

        // ------------------------
        // CASE A: this web has DOWN:true (floor) burning
        // ------------------------
        if (state.getValue(FACE_DOWN)) {
            // (-1, -1, 0) east:true, but NOT if (-1, 0, 0) is solid
            BlockPos corner = pos.offset(-1, 0, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(-1, -1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_EAST)) {
                    ignite(level, targetPos, target);
                }
            }

            // (0, -1, -1) south:true, but NOT if (0, 0, -1) is solid
            corner = pos.offset(0, 0, -1);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, -1, -1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_SOUTH)) {
                    ignite(level, targetPos, target);
                }
            }

            // (1, -1, 0) west:true, but NOT if (1, 0, 0) is solid
            corner = pos.offset(1, 0, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(1, -1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_WEST)) {
                    ignite(level, targetPos, target);
                }
            }

            // (0, -1, 1) north:true, but NOT if (0, 0, 1) is solid
            corner = pos.offset(0, 0, 1);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, -1, 1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_NORTH)) {
                    ignite(level, targetPos, target);
                }
            }
        }

        // ------------------------
        // CASE B: this web has UP:true (ceiling) burning
        // ------------------------
        if (state.getValue(FACE_UP)) {
            // (-1, 1, 0) east:true, but NOT if (-1, 0, 0) is solid
            BlockPos corner = pos.offset(-1, 0, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(-1, 1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_EAST)) {
                    ignite(level, targetPos, target);
                }
            }

            // (0, 1, -1) south:true, but NOT if (0, 0, -1) is solid
            corner = pos.offset(0, 0, -1);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, 1, -1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_SOUTH)) {
                    ignite(level, targetPos, target);
                }
            }

            // (1, 1, 0) west:true, but NOT if (1, 0, 0) is solid
            corner = pos.offset(1, 0, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(1, 1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_WEST)) {
                    ignite(level, targetPos, target);
                }
            }

            // (0, 1, 1) north:true, but NOT if (0, 0, 1) is solid
            corner = pos.offset(0, 0, 1);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, 1, 1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_NORTH)) {
                    ignite(level, targetPos, target);
                }
            }
        }

        // ------------------------
        // CASE C: “reverse” – wall burning ignites floor/ceiling
        // ------------------------
        // EAST:true wall burning → down/up webs around corner
        if (state.getValue(FACE_EAST)) {
            // floor (down:true) at ( +1, +1, 0 ), corner at (0, +1, 0 )
            BlockPos corner = pos.offset(0, 1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(1, 1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_DOWN)) {
                    ignite(level, targetPos, target);
                }
            }
            // ceiling (up:true) at ( +1, -1, 0 ), corner at (0, -1, 0 )
            corner = pos.offset(0, -1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(1, -1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_UP)) {
                    ignite(level, targetPos, target);
                }
            }
        }

        // WEST:true wall burning → down/up webs
        if (state.getValue(FACE_WEST)) {
            // floor at ( -1, +1, 0 ), corner at (0, +1, 0 )
            BlockPos corner = pos.offset(0, 1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(-1, 1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_DOWN)) {
                    ignite(level, targetPos, target);
                }
            }
            // ceiling at ( -1, -1, 0 ), corner at (0, -1, 0 )
            corner = pos.offset(0, -1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(-1, -1, 0);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_UP)) {
                    ignite(level, targetPos, target);
                }
            }
        }

        // SOUTH:true wall burning → down/up webs
        if (state.getValue(FACE_SOUTH)) {
            // floor at (0, +1, +1), corner at (0, +1, 0)
            BlockPos corner = pos.offset(0, 1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, 1, 1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_DOWN)) {
                    ignite(level, targetPos, target);
                }
            }
            // ceiling at (0, -1, +1), corner at (0, -1, 0)
            corner = pos.offset(0, -1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, -1, 1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_UP)) {
                    ignite(level, targetPos, target);
                }
            }
        }

        // NORTH:true wall burning → down/up webs
        if (state.getValue(FACE_NORTH)) {
            // floor at (0, +1, -1), corner at (0, +1, 0)
            BlockPos corner = pos.offset(0, 1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, 1, -1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_DOWN)) {
                    ignite(level, targetPos, target);
                }
            }
            // ceiling at (0, -1, -1), corner at (0, -1, 0)
            corner = pos.offset(0, -1, 0);
            if (!isCornerBlocked.test(corner)) {
                BlockPos targetPos = pos.offset(0, -1, -1);
                BlockState target = level.getBlockState(targetPos);
                if (target.getBlock() instanceof WebCarpetBlock && target.getValue(FACE_UP)) {
                    ignite(level, targetPos, target);
                }
            }
        }
    }

    // --- React to neighbouring fire ---

    @Override
    public void neighborChanged(BlockState state,
                                Level level,
                                BlockPos pos,
                                Block neighborBlock,
                                BlockPos neighborPos,
                                boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (level.isClientSide) return;

        BlockState neighborState = level.getBlockState(neighborPos);

        // If any adjacent block becomes fire, ignite this web
        if (neighborState.is(Blocks.FIRE)) {
            ignite((ServerLevel) level, pos, state);
        }

        // Optional: also react to lava
        // if (neighborState.is(Blocks.LAVA)) {
        //     ignite((ServerLevel) level, pos, state);
        // }
    }
}
