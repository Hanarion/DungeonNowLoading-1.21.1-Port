package dev.hexnowloading.dungeonnowloading.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class WebCarpetBlock extends MultifaceBlock {

    public static final MapCodec<WebCarpetBlock> CODEC = simpleCodec(WebCarpetBlock::new);

    @Override
    public MapCodec<WebCarpetBlock> codec() {
        return CODEC;
    }

    private final MultifaceSpreader spreader = new MultifaceSpreader(this);
    private static final Vec3 ENTITY_SLOWDOWN = new Vec3(0.9999D, 0.9999D, 0.9999D);

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

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Only react when the block actually changed to WebCarpet
        if (oldState.is(this)) return;

        // If any neighbor is fire, ignite immediately
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if (neighborState.is(Blocks.FIRE)) {
                ignite(serverLevel, pos, state);
                break;
            }
        }
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        boolean isIgniter =
                stack.is(Items.FLINT_AND_STEEL) ||
                        stack.is(Items.FIRE_CHARGE);

        if (!isIgniter) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            ignite(serverLevel, pos, state);

            if (!player.getAbilities().instabuild) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                } else if (stack.is(Items.FIRE_CHARGE)) {
                    stack.shrink(1);
                }
            }
        }

        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    // --- Slowing ---

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // First, check if the entity actually intersects the multiface shape,
        // not just the full block cube.
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) return;

        AABB webBox = shape.bounds().move(pos);
        if (!webBox.intersects(entity.getBoundingBox())) {
            return;
        }

        // --- 1) Flaming projectile -> ignite ---
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (entity instanceof AbstractArrow arrow) {
                if (arrow.isOnFire()) {
                    ignite(serverLevel, pos, state);
                }
            } else if (entity instanceof Projectile projectile && projectile.isOnFire()) {
                ignite(serverLevel, pos, state);
            }
        }

        // --- 2) Normal slow for living entities ---

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        // Don't slow spiders (vanilla Spider + your SilkSpiderEntity, since it extends Spider)
        if (living instanceof Spider) {
            return;
        }

        entity.makeStuckInBlock(state, ENTITY_SLOWDOWN);
    }



    // --- Burning logic ---


    // --- Spread rule support (shared logic with projectile-style pattern) ---

    private record SpreadRule(
            int dx, int dy, int dz,                  // target offset from center
            Direction faceDir,                       // face direction on the neighbor web (in base orientation)
            int cornerDx, int cornerDy, int cornerDz,// corner-block offset to check
            boolean requiresCornerClear              // if true, skip if corner is solid
    ) {}

    private enum LocalAxis {
        U_POS, U_NEG,
        V_POS, V_NEG,
        N_POS, N_NEG
    }

    // Base pattern for DOWN-facing webs (floor).
    // Same idea as the projectile's DOWN_RULES, but here we "ignite" neighbors instead of placing webs.
    private static final SpreadRule[] DOWN_FIRE_RULES = new SpreadRule[] {
            // down:true in NESW (floor neighbors)
            new SpreadRule( 1, 0,  0, Direction.DOWN, 0, 0, 0, false),
            new SpreadRule(-1, 0,  0, Direction.DOWN, 0, 0, 0, false),
            new SpreadRule( 0, 0,  1, Direction.DOWN, 0, 0, 0, false),
            new SpreadRule( 0, 0, -1, Direction.DOWN, 0, 0, 0, false),

            // diagonal wall webs with corner checks
            // east:true at (-1,-1,0), blocked by corner (-1,0,0)
            new SpreadRule(-1, -1, 0, Direction.EAST,  -1, 0,  0, true),

            // south:true at (0,-1,-1), blocked by corner (0,0,-1)
            new SpreadRule( 0, -1,-1, Direction.SOUTH,  0, 0, -1, true),

            // west:true at (1,-1,0), blocked by corner (1,0,0)
            new SpreadRule( 1, -1, 0, Direction.WEST,   1, 0,  0, true),

            // north:true at (0,-1,1), blocked by corner (0,0,1)
            new SpreadRule( 0, -1, 1, Direction.NORTH,  0, 0,  1, true)
    };

    private LocalAxis toLocalAxis(Direction faceDir) {
        // Base orientation:
        // U_base = EAST, V_base = SOUTH, N_base = DOWN
        return switch (faceDir) {
            case EAST  -> LocalAxis.U_POS;
            case WEST  -> LocalAxis.U_NEG;
            case SOUTH -> LocalAxis.V_POS;
            case NORTH -> LocalAxis.V_NEG;
            case DOWN  -> LocalAxis.N_POS;
            case UP    -> LocalAxis.N_NEG;
            default    -> LocalAxis.N_POS;
        };
    }
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
                living.igniteForSeconds(3);
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
                    case DOWN  -> FireBlock.UP; // shouldn't happen
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

        // 1) Simple orthogonal spread: adjacent webs always ignite
        for (Direction dir : Direction.values()) {
            BlockPos nextPos = pos.relative(dir);
            BlockState neighbor = level.getBlockState(nextPos);

            if (neighbor.getBlock() instanceof WebCarpetBlock) {
                ignite(level, nextPos, neighbor);
            }
        }

        // 2) Pattern-based spread (same logic as projectile), rotated for each face
        spreadFirePattern(level, pos, state);
    }

    private void spreadFirePattern(ServerLevel level, BlockPos center, BlockState state) {
        // For every face this web has, apply the DOWN_FIRE_RULES pattern rotated into that face's frame.
        if (hasFace(state, Direction.DOWN)) {
            applyFireSpreadRules(level, center, Direction.DOWN, DOWN_FIRE_RULES);
        }
        if (hasFace(state, Direction.UP)) {
            applyFireSpreadRules(level, center, Direction.UP, DOWN_FIRE_RULES);
        }
        if (hasFace(state, Direction.NORTH)) {
            applyFireSpreadRules(level, center, Direction.NORTH, DOWN_FIRE_RULES);
        }
        if (hasFace(state, Direction.EAST)) {
            applyFireSpreadRules(level, center, Direction.EAST, DOWN_FIRE_RULES);
        }
        if (hasFace(state, Direction.SOUTH)) {
            applyFireSpreadRules(level, center, Direction.SOUTH, DOWN_FIRE_RULES);
        }
        if (hasFace(state, Direction.WEST)) {
            applyFireSpreadRules(level, center, Direction.WEST, DOWN_FIRE_RULES);
        }
    }

    private void applyFireSpreadRules(ServerLevel level,
                                      BlockPos center,
                                      Direction sourceFace,
                                      SpreadRule[] rules) {
        // Base orientation for DOWN rules:
        // U_base = EAST (+X), V_base = SOUTH (+Z), N_base = DOWN (-Y)

        Direction U_t, V_t, N_t;
        N_t = sourceFace;

        switch (sourceFace) {
            case DOWN -> {
                U_t = Direction.EAST;
                V_t = Direction.SOUTH;
            }
            case UP -> {
                U_t = Direction.EAST;
                V_t = Direction.SOUTH;
            }
            case NORTH -> {
                U_t = Direction.EAST;
                V_t = Direction.DOWN;
            }
            case SOUTH -> {
                U_t = Direction.WEST;
                V_t = Direction.DOWN;
            }
            case EAST -> {
                U_t = Direction.SOUTH;
                V_t = Direction.DOWN;
            }
            case WEST -> {
                U_t = Direction.NORTH;
                V_t = Direction.DOWN;
            }
            default -> {
                U_t = Direction.EAST;
                V_t = Direction.SOUTH;
            }
        }

        int Ux = U_t.getStepX(), Uy = U_t.getStepY(), Uz = U_t.getStepZ();
        int Vx = V_t.getStepX(), Vy = V_t.getStepY(), Vz = V_t.getStepZ();
        int Nx = N_t.getStepX(), Ny = N_t.getStepY(), Nz = N_t.getStepZ();

        for (SpreadRule rule : rules) {
            // --- 1) Base world delta -> local (a,b,c) relative to (U_base, V_base, N_base) ---
            int dx = rule.dx();
            int dy = rule.dy();
            int dz = rule.dz();

            // Base: dx = a, dz = b, dy = -c  => a=dx, b=dz, c=-dy
            int a = dx;
            int b = dz;
            int c = -dy;

            // --- 2) Rotate local (a,b,c) into this face's basis (U_t, V_t, N_t) ---
            int tdx = a * Ux + b * Vx + c * Nx;
            int tdy = a * Uy + b * Vy + c * Ny;
            int tdz = a * Uz + b * Vz + c * Nz;

            BlockPos targetPos = center.offset(tdx, tdy, tdz);

            // --- 3) Corner block check, if required ---
            if (rule.requiresCornerClear()) {
                int cdx = rule.cornerDx();
                int cdy = rule.cornerDy();
                int cdz = rule.cornerDz();

                int ca = cdx;
                int cb = cdz;
                int cc = -cdy;

                int tCdx = ca * Ux + cb * Vx + cc * Nx;
                int tCdy = ca * Uy + cb * Vy + cc * Ny;
                int tCdz = ca * Uz + cb * Vz + cc * Nz;

                BlockPos cornerPos = center.offset(tCdx, tCdy, tCdz);
                if (level.getBlockState(cornerPos).isSolid()) {
                    continue;
                }
            }

            // --- 4) Rotate faceDir (EAST/WEST/etc.) from base into this basis ---
            Direction baseFace = rule.faceDir();
            LocalAxis localAxis = toLocalAxis(baseFace);

            Direction faceDirWorld = switch (localAxis) {
                case U_POS -> U_t;
                case U_NEG -> U_t.getOpposite();
                case V_POS -> V_t;
                case V_NEG -> V_t.getOpposite();
                case N_POS -> N_t;
                case N_NEG -> N_t.getOpposite();
            };

            // Ignite only if there is a web at targetPos with that face set
            BlockState neighbor = level.getBlockState(targetPos);
            if (neighbor.getBlock() instanceof WebCarpetBlock web) {
                BooleanProperty faceProp = MultifaceBlock.getFaceProperty(faceDirWorld);
                if (faceProp != null && neighbor.hasProperty(faceProp) && neighbor.getValue(faceProp)) {
                    ignite(level, targetPos, neighbor);
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
