package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.BurnacleBlock;
import dev.hexnowloading.dungeonnowloading.block.BurnacleBlock.Stage;
import dev.hexnowloading.dungeonnowloading.entity.projectile.GasCloudEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

public class BurnacleBlockEntity extends BlockEntity {

    private int cycleTime;
    private int cycleOffset;
    private double initialGasSpeed;
    private double playerRange;
    private CompoundTag gasEntityNbt;

    private static final Map<Stage, StageBehaviorSettings> STAGE_BEHAVIOR = new EnumMap<>(Stage.class);
    public record StageBehaviorSettings(
            int cycleTime,
            int cycleOffset,
            double initialGasSpeed,
            double playerRange
    ) {}

    public BurnacleBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.BURNACLE.get(), pos, state);

        Stage stage = state.getValue(BurnacleBlock.STAGE);
        BurnacleBlock.StagePreset preset = BurnacleBlock.getPreset(stage);

        if (preset != null) {
            this.cycleTime       = preset.cycleTime();
            this.cycleOffset     = preset.cycleOffset();
            this.initialGasSpeed = preset.initialGasSpeed();
            this.playerRange     = preset.playerRange();
        } else {
            // Fallback if somehow missing
            this.cycleTime       = 100;
            this.cycleOffset     = 0;
            this.initialGasSpeed = 0.02D;
            this.playerRange     = 16.0D;
        }

        this.gasEntityNbt = null; // optional overrides, if any
    }




    /* -------------------------------------------------------------------------
     * Ticking
     * ---------------------------------------------------------------------- */

    public static void serverTick(Level level, BlockPos pos, BlockState state, BurnacleBlockEntity be) {
        if (level.isClientSide) return;
        if (!(state.getBlock() instanceof BurnacleBlock)) return;

        if (be.cycleTime <= 0) {
            // 0 or negative means "no emission"
            return;
        }

        long time = level.getGameTime();
        if (((time + be.cycleOffset) % be.cycleTime) != 0L) {
            return;
        }

        // --- NEW: player proximity check ---
        if (be.playerRange > 0.0D) {
            double cx = pos.getX() + 0.5D;
            double cy = pos.getY() + 0.5D;
            double cz = pos.getZ() + 0.5D;

            var nearest = level.getNearestPlayer(cx, cy, cz, be.playerRange, false);
            if (nearest == null) {
                // No player in range → skip this cycle
                return;
            }
        }

        be.emitGas(level, pos, state);
    }


    private void emitGas(Level level, BlockPos pos, BlockState state) {
        Stage stage = state.getValue(BurnacleBlock.STAGE);
        BurnacleBlock.StagePreset preset = BurnacleBlock.getPreset(stage);
        if (preset == null) return;

        Direction facing = state.getValue(BurnacleBlock.FACING);

        Vec3 baseCenter = Vec3.atCenterOf(pos);
        Vec3 offset = new Vec3(
                facing.getStepX() * 0.51D,
                facing.getStepY() * 0.51D,
                facing.getStepZ() * 0.51D
        );
        Vec3 spawnPos = baseCenter.add(offset);

        GasCloudEntity gas = DNLEntityTypes.GAS_CLOUD.get().create(level);
        if (gas == null) return;

        gas.moveTo(spawnPos.x, spawnPos.y, spawnPos.z,
                level.random.nextFloat() * 360.0F, 0.0F);

        // Initial velocity along facing, from BE field (which came from preset unless overridden)
        double push = this.initialGasSpeed;
        gas.setDeltaMovement(
                facing.getStepX() * push,
                facing.getStepY() * push,
                facing.getStepZ() * push
        );

        // --- Merge presets with optional GasEntity NBT overrides ---
        CompoundTag nbt = this.gasEntityNbt;

        int   gasSize = (nbt != null && nbt.contains("GasSize", Tag.TAG_INT))
                ? nbt.getInt("GasSize") : preset.gasSize();
        int   growthTime = (nbt != null && nbt.contains("GrowthTime", Tag.TAG_INT))
                ? nbt.getInt("GrowthTime") : preset.growthTime();
        float gasSpread = (nbt != null && nbt.contains("GasSpread", Tag.TAG_FLOAT))
                ? nbt.getFloat("GasSpread") : preset.gasSpread();
        float gasSpeed = (nbt != null && nbt.contains("GasSpeed", Tag.TAG_FLOAT))
                ? nbt.getFloat("GasSpeed") : preset.gasSpeed();
        float airResistance = (nbt != null && nbt.contains("AirResistance", Tag.TAG_FLOAT))
                ? nbt.getFloat("AirResistance") : preset.airResistance();
        int   life = (nbt != null && nbt.contains("Life", Tag.TAG_INT))
                ? nbt.getInt("Life") : preset.life();
        float explosionMultiplier = (nbt != null && nbt.contains("ExplosionMultiplier", Tag.TAG_FLOAT))
                ? nbt.getFloat("ExplosionMultiplier") : preset.explosionMultiplier();
        int   chainDelay = (nbt != null && nbt.contains("ChainDelay", Tag.TAG_INT))
                ? nbt.getInt("ChainDelay") : preset.chainDelay();
        int   ignitionDelay = (nbt != null && nbt.contains("IgnitionDelay", Tag.TAG_INT))
                ? nbt.getInt("IgnitionDelay") : preset.ignitionDelay();
        int   explosionDelay = (nbt != null && nbt.contains("ExplosionDelay", Tag.TAG_INT))
                ? nbt.getInt("ExplosionDelay") : preset.explosionDelay();

        gas.configureFromBurnacle(
                gasSize,
                growthTime,
                gasSpread,
                gasSpeed,
                airResistance,
                life,
                explosionMultiplier,
                chainDelay,
                ignitionDelay,
                explosionDelay
        );

        level.addFreshEntity(gas);
    }



    /* -------------------------------------------------------------------------
     * NBT (Cycle Time / Timing / Speed persistence)
     * ---------------------------------------------------------------------- */

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("CycleTime", this.cycleTime);
        tag.putInt("CycleOffset", this.cycleOffset);
        tag.putDouble("InitialGasSpeed", this.initialGasSpeed);
        tag.putDouble("PlayerRange", this.playerRange);

        if (this.gasEntityNbt != null && !this.gasEntityNbt.isEmpty()) {
            tag.put("GasEntity", this.gasEntityNbt.copy());
        }
    }



    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains("CycleTime")) {
            this.cycleTime = tag.getInt("CycleTime");
        }
        if (tag.contains("CycleOffset")) {
            this.cycleOffset = tag.getInt("CycleOffset");
        }
        if (tag.contains("InitialGasSpeed")) {
            this.initialGasSpeed = tag.getDouble("InitialGasSpeed");
        }
        if (tag.contains("PlayerRange")) {
            this.playerRange = tag.getDouble("PlayerRange");
        }

        if (tag.contains("GasEntity", Tag.TAG_COMPOUND)) {
            this.gasEntityNbt = tag.getCompound("GasEntity").copy();
        } else {
            this.gasEntityNbt = null;
        }
    }



    /* -------------------------------------------------------------------------
     * Public API (so you can tweak from commands / items / structures)
     * ---------------------------------------------------------------------- */

    public int getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
        setChanged();
    }

    public int getCycleOffset() {
        return cycleOffset;
    }

    public void setCycleOffset(int cycleOffset) {
        this.cycleOffset = cycleOffset;
        setChanged();
    }

    public double getInitialGasSpeed() {
        return initialGasSpeed;
    }

    public void setInitialGasSpeed(double initialGasSpeed) {
        this.initialGasSpeed = initialGasSpeed;
        setChanged();
    }

    public double getPlayerRange() {
        return playerRange;
    }

    public void setPlayerRange(double playerRange) {
        this.playerRange = playerRange;
        setChanged();
    }

    public void setGasEntityNbt(CompoundTag tag) {
        this.gasEntityNbt = tag == null ? null : tag.copy();
        setChanged();
    }

    public CompoundTag getGasEntityNbt() {
        return gasEntityNbt == null ? null : gasEntityNbt.copy();
    }

    static StageBehaviorSettings getBehaviorSettings(Stage stage) {
        return STAGE_BEHAVIOR.get(stage);
    }


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
                20,     // growthTime
                1.5f,   // gasSpread
                0.02f,  // gasSpeed
                0.10f,  // airResistance
                160,    // life
                0.2f,   // explosionMultiplier
                5,      // chainDelay
                10,     // ignitionDelay
                25,     // explosionDelay

                // --- behavior ---
                160,    // cycleTime
                0,      // cycleOffset
                0.015D, // initialGasSpeed
                10.0D   // playerRange
        ));

        // JUVENILE preset
        STAGE_PRESETS.put(Stage.JUVENILE, new StagePreset(
                2,
                20,
                2.0f,
                0.03f,
                0.10f,
                200,
                0.3f,
                5,
                10,
                25,

                100,    // cycleTime
                5,      // cycleOffset
                0.02D,  // initialGasSpeed
                12.0D   // playerRange
        ));

        // MATURE preset (your original summon example, with stronger behavior)
        STAGE_PRESETS.put(Stage.MATURE, new StagePreset(
                3,      // GasSize:3
                20,     // GrowthTime:20
                2.0f,   // GasSpread:2.0f
                0.03f,  // GasSpeed:0.03f
                0.10f,  // AirResistance:0.1f
                200,    // Life:200
                0.4f,   // ExplosionMultiplier:0.4f
                5,      // ChainDelay:5
                10,     // IgnitionDelay:10
                25,     // ExplosionDelay:25

                60,     // cycleTime
                10,     // cycleOffset
                0.03D,  // initialGasSpeed
                16.0D   // playerRange
        ));
    }

    // Helper for BE
    static StagePreset getPreset(Stage stage) {
        return STAGE_PRESETS.get(stage);
    }

}
