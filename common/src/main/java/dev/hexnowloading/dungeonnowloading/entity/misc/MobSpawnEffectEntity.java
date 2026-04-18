package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.util.SpawnEffectType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class MobSpawnEffectEntity extends Entity {

    private ResourceLocation mobId;
    private CompoundTag mobData;
    private SpawnEffectType effectType = SpawnEffectType.NONE;
    private int ticks;

    public MobSpawnEffectEntity(EntityType<? extends MobSpawnEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setInvisible(true); // purely visual/FX, no model needed
    }

    // Called by the MobSpawnPointBlockEntity when spawning this
    public void configure(ResourceLocation mobId, CompoundTag mobData, SpawnEffectType effectType) {
        this.mobId = mobId;
        this.mobData = (mobData == null || mobData.isEmpty()) ? null : mobData.copy();
        this.effectType = effectType;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        ticks++;

        switch (effectType) {
            case NONE -> tickNone();
            case POOF_FIRE -> tickPoofFire();
            // Add more cases for new effects
        }
    }

    private void tickNone() {
        // No telegraph – just spawn immediately and disappear
        spawnNowAndDiscard();
    }

    private void tickPoofFire() {
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }

        // First 3 seconds (~60 ticks): poof telegraph
        if (ticks <= 60) {
            if (ticks % 4 == 0) {
                server.sendParticles(
                        ParticleTypes.POOF,
                        getX(),
                        getY() + 0.1D,
                        getZ(),
                        8,
                        0.4D, 0.2D, 0.4D,
                        0.02D
                );
            }
            return; // still telegraphing, don't spawn yet
        }

        // At the end: fire burst + sound
        server.sendParticles(
                ParticleTypes.FLAME,
                getX(),
                getY() + 0.2D,
                getZ(),
                20,
                0.4D, 0.3D, 0.4D,
                0.03D
        );

        server.playSound(
                null,
                getX(), getY(), getZ(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        // Then spawn mob and remove this entity
        spawnNowAndDiscard();
    }

    private void spawnNowAndDiscard() {
        if (!(level() instanceof ServerLevel server)) {
            discard();
            return;
        }
        if (mobId == null) {
            discard();
            return;
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        if (type == null) {
            discard();
            return;
        }

        if (!(type.create(server) instanceof Mob mob)) {
            discard();
            return;
        }

        mob.moveTo(
                getX(),
                getY(),
                getZ(),
                server.random.nextFloat() * 360F,
                0.0F
        );

        if (mobData != null && !mobData.isEmpty()) {
            CompoundTag tag = mob.saveWithoutId(new CompoundTag());
            tag.merge(mobData);
            mob.load(tag);
        }

        server.addFreshEntity(mob);
        discard();
    }

    // --------- NBT (for world saving) ---------

    @Override
    protected void defineSynchedData() {
        // no synched data needed unless you want client-side visuals beyond particles
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.ticks = tag.getInt("Ticks");

        if (tag.contains("MobId")) {
            this.mobId = new ResourceLocation(tag.getString("MobId"));
        }

        if (tag.contains("MobData")) {
            this.mobData = tag.getCompound("MobData");
        }

        this.effectType = SpawnEffectType.fromString(tag.getString("SpawnEffect"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Ticks", this.ticks);

        if (mobId != null) {
            tag.putString("MobId", mobId.toString());
        }
        if (mobData != null && !mobData.isEmpty()) {
            tag.put("MobData", mobData.copy());
        }
        tag.putString("SpawnEffect", effectType.getId());
    }
}
