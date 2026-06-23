package dev.hexnowloading.dungeonnowloading.entity.projectile;

import dev.hexnowloading.dungeonnowloading.entity.monster.SilkSpiderEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WebSpitProjectileEntity extends ThrowableItemProjectile {

    public static final float GRAVITY = 0.03F;

    // Rule describing how to place 1 extra web relative to a "center"
    private record SpreadRule(
            int dx, int dy, int dz,                  // target offset from center
            Direction faceDir,                       // face direction on the placed web (in base orientation)
            int cornerDx, int cornerDy, int cornerDz,// corner-block offset to check for solid
            boolean requiresCornerClear              // if true, skip if corner is solid
    ) {}

    // Used to map faceDir to a local axis in the base orientation
    private enum LocalAxis {
        U_POS, U_NEG,
        V_POS, V_NEG,
        N_POS, N_NEG
    }

    // Base pattern for DOWN-facing webs (floor). Other faces reuse this via rotation.
    // Implements:
    //  - down:true in NESW direction.
    //  - east:true at (-1,-1,0), but NOT if (-1,0,0) is solid.
    //  - south:true at (0,-1,-1), but NOT if (0,0,-1) is solid.
    //  - west:true at (1,-1,0), but NOT if (1,0,0) is solid.
    //  - north:true at (0,-1,1), but NOT if (0,0,1) is solid.
    private static final SpreadRule[] DOWN_RULES = new SpreadRule[] {
            // down:true in NESW
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

    public WebSpitProjectileEntity(EntityType<? extends WebSpitProjectileEntity> type, Level level) {
        super(type, level);
    }

    public WebSpitProjectileEntity(Level level, LivingEntity owner) {
        super(DNLEntityTypes.WEB_SPIT_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    public void tick() {
        super.tick();
        ProjectileUtil.rotateTowardsMovement(this, 1.0F);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this.seedRotationFromMovement();
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        this.seedRotationFromMovement();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.setXRot(packet.getXRot());
        this.setYRot(packet.getYRot());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.seedRotationFromMovement();
    }

    private void seedRotationFromMovement() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() <= 1.0E-7D) {
            return;
        }

        double horizontalDistance = motion.horizontalDistance();
        this.setXRot((float)(Mth.atan2(motion.y, horizontalDistance) * (double)(180F / (float)Math.PI)));
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * (double)(180F / (float)Math.PI)));
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (result.getType() == HitResult.Type.BLOCK) {
                this.spawnWebBreakParticles();
                this.playLandingSound();
            }
            spawnWebField(result);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        Entity hit = hitResult.getEntity();
        if (this.level().isClientSide) return;

        Entity owner = this.getOwner();

        // Prevent friendly fire against spiders if fired by SilkSpiderEntity
        if (owner instanceof SilkSpiderEntity && hit instanceof Spider) {
            return;
        }

        if (hit instanceof LivingEntity living) {

            // Deal damage (same as before)
            float damage = owner instanceof LivingEntity ownerLiving
                    ? (float) ownerLiving.getAttributeValue(Attributes.ATTACK_DAMAGE)
                    : 1.0F;

            living.hurt(this.damageSources().thrown(this, owner), damage);

            // Apply slowness III for 2 seconds (40 ticks)
            living.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    40,   // 2 seconds @ 20 tps
                    2,    // level 3 = amplifier 2
                    false, // ambient
                    true,  // show particles
                    true   // show icon
            ));
        }

        this.playLandingSound();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }

        if (this.level().isClientSide || this.isRemoved()) {
            return true;
        }

        this.spawnWebBreakParticles();

        // Soft, squishy pop
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                DNLSounds.SILK_SPIDER_WEB_SPIT_LAND.get(),
                this.getSoundSource(),
                0.7F,
                1.2F + (this.random.nextFloat() - 0.5F) * 0.3F
        );

        this.discard();
        return true;
    }

    private void spawnWebBreakParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            BlockState webState = DNLBlocks.WEB_CARPET.get().defaultBlockState();
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, webState),
                    this.getX(), this.getY(), this.getZ(),
                    12,
                    0.15D, 0.15D, 0.15D,
                    0.02D
            );
        }
    }

    private void playLandingSound() {
        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                DNLSounds.SILK_SPIDER_WEB_SPIT_LAND.get(),
                this.getSoundSource(),
                0.9F,
                0.95F + this.random.nextFloat() * 0.1F
        );
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    private void spawnWebField(HitResult hitResult) {
        Level level = this.level();
        if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        BlockPos center;
        if (hitResult instanceof BlockHitResult blockHit) {
            center = blockHit.getBlockPos().relative(blockHit.getDirection());
        } else {
            center = this.blockPosition();
        }

        // 1) Place the initial multiface web at the landing position
        if (!placeCentralWeb(level, center)) {
            return;
        }

        BlockState originState = level.getBlockState(center);
        if (originState.getBlock() != DNLBlocks.WEB_CARPET.get()) {
            return;
        }

        // 2) Spread according to faces on the initial web
        spreadFromOrigin(level, center, originState);
    }

    // --- Central placement ---

    /**
     * Place a multiface web at 'pos', attaching to all valid neighboring faces.
     */
    private boolean placeCentralWeb(Level level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir()) {
            return false;
        }

        BlockState state = DNLBlocks.WEB_CARPET.get().defaultBlockState();
        boolean anyFace = false;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Attach only to sturdy faces
            if (!neighborState.isFaceSturdy(level, neighborPos, dir.getOpposite())) {
                continue;
            }

            var faceProp = MultifaceBlock.getFaceProperty(dir);
            if (faceProp != null) {
                state = state.setValue(faceProp, true);
                anyFace = true;
            }
        }

        if (!anyFace) {
            return false;
        }

        if (state.canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, state);
            return true;
        }

        return false;
    }

    // --- Spread logic ---

    private void spreadFromOrigin(Level level, BlockPos origin, BlockState originState) {
        // Use the same DOWN_RULES pattern for every face, rotated into that face's local frame.
        if (hasFace(originState, Direction.DOWN)) {
            applySpreadRules(level, origin, Direction.DOWN, DOWN_RULES);
        }
        if (hasFace(originState, Direction.UP)) {
            applySpreadRules(level, origin, Direction.UP, DOWN_RULES);
        }
        if (hasFace(originState, Direction.NORTH)) {
            applySpreadRules(level, origin, Direction.NORTH, DOWN_RULES);
        }
        if (hasFace(originState, Direction.EAST)) {
            applySpreadRules(level, origin, Direction.EAST, DOWN_RULES);
        }
        if (hasFace(originState, Direction.SOUTH)) {
            applySpreadRules(level, origin, Direction.SOUTH, DOWN_RULES);
        }
        if (hasFace(originState, Direction.WEST)) {
            applySpreadRules(level, origin, Direction.WEST, DOWN_RULES);
        }
    }

    private boolean hasFace(BlockState state, Direction dir) {
        var prop = MultifaceBlock.getFaceProperty(dir);
        return prop != null && state.hasProperty(prop) && state.getValue(prop);
    }

    /**
     * Place a web_carpet at 'pos' with exactly one face 'faceDir' = true,
     * if there's a valid supporting block for that face.
     */
    private boolean placeSingleFaceWeb(Level level, BlockPos pos, Direction faceDir) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir()) {
            return false;
        }

        BlockPos supportPos = pos.relative(faceDir);
        BlockState supportState = level.getBlockState(supportPos);

        // The supporting block must have a sturdy face towards this web
        if (!supportState.isFaceSturdy(level, supportPos, faceDir.getOpposite())) {
            return false;
        }

        var faceProp = MultifaceBlock.getFaceProperty(faceDir);
        if (faceProp == null) {
            return false;
        }

        BlockState webState = DNLBlocks.WEB_CARPET.get().defaultBlockState()
                .setValue(faceProp, true);

        if (!webState.canSurvive(level, pos)) {
            return false;
        }

        level.setBlockAndUpdate(pos, webState);
        return true;
    }

    // Rotate the base DOWN_RULES pattern into the orientation of sourceFace
    private void applySpreadRules(Level level, BlockPos center, Direction sourceFace, SpreadRule[] rules) {
        // Base orientation (for DOWN rules):
        // U_base = EAST (+X), V_base = SOUTH (+Z), N_base = DOWN (-Y)
        // We encode the rule offsets in this base; now we rotate them into (U_t, V_t, N_t).

        // Target basis for this source face
        Direction U_t, V_t, N_t;
        N_t = sourceFace;

        switch (sourceFace) {
            case DOWN -> {
                U_t = Direction.EAST;
                V_t = Direction.SOUTH;
            }
            case UP -> {
                // Same U/V as DOWN, N flipped
                U_t = Direction.EAST;
                V_t = Direction.SOUTH;
            }
            case NORTH -> {
                // North-facing wall: N points NORTH, choose U right (EAST), V = DOWN
                U_t = Direction.EAST;
                V_t = Direction.DOWN;
            }
            case SOUTH -> {
                // South-facing wall: N points SOUTH, choose U right (WEST), V = DOWN
                U_t = Direction.WEST;
                V_t = Direction.DOWN;
            }
            case EAST -> {
                // East-facing wall: N points EAST, choose U = SOUTH, V = DOWN
                U_t = Direction.SOUTH;
                V_t = Direction.DOWN;
            }
            case WEST -> {
                // West-facing wall: N points WEST, choose U = NORTH, V = DOWN
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
            // --- 1) Convert base world delta -> local (a,b,c) relative to (U_base, V_base, N_base) ---
            int dx = rule.dx();
            int dy = rule.dy();
            int dz = rule.dz();

            // In the base orientation:
            // U_base = EAST (1,0,0), V_base = SOUTH (0,0,1), N_base = DOWN (0,-1,0)
            // So: dx = a, dz = b, dy = -c  =>  a=dx, b=dz, c=-dy
            int a = dx;
            int b = dz;
            int c = -dy;

            // --- 2) Rotate local (a,b,c) into this face's basis (U_t, V_t, N_t) ---
            int tdx = a * Ux + b * Vx + c * Nx;
            int tdy = a * Uy + b * Vy + c * Ny;
            int tdz = a * Uz + b * Vz + c * Nz;

            BlockPos targetPos = center.offset(tdx, tdy, tdz);

            // --- 3) Corner offset, if needed ---
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

            placeSingleFaceWeb(level, targetPos, faceDirWorld);
        }
    }

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
}
