package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.ThumperDiveAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ThumperEntity extends FlyingMob implements Enemy {

    private static final double SLAM_RADIUS = 2.5D;

    // Used by the dive goal to know if it already slammed this fall
    private boolean hasSlammedThisFall = false;

    public ThumperEntity(EntityType<? extends ThumperEntity> type, Level level) {
        super(type, level);
        this.moveControl = new HoveringFlyingMoveControl(this);
        this.xpReward = 10;
    }

    // ---------- ATTRIBUTES ----------

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FLYING_SPEED, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    // ---------- GOALS ----------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ThumperDiveAttackGoal(this));

        // Targeting
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ---------- SLAM HANDLING ----------

    /**
     * Called by the dive goal when Thumper hits the ground while diving.
     */
    public void onSlamImpact() {
        if (this.level().isClientSide || hasSlammedThisFall) {
            return;
        }
        hasSlammedThisFall = true;

        // Damage + knockback in a small radius
        AABB box = this.getBoundingBox().inflate(SLAM_RADIUS, 0.5D, SLAM_RADIUS);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && e != this && e instanceof Player)) {

            DamageSource source = this.damageSources().mobAttack(this);
            target.hurt(source, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));

            // Extra: disable shield if it's a player
            if (target instanceof Player player && player.isBlocking()) {
                player.disableShield(true);
            }

            // Knockback mostly horizontal, low vertical lift
            Vec3 rawDir = target.position().subtract(this.position());
            Vec3 horizDir = new Vec3(rawDir.x, 0.0D, rawDir.z).normalize(); // ignore Y
            double strength = 1.8D; // slightly stronger since it's flat
            double verticalBoost = 0.25D;

            target.push(horizDir.x * strength, verticalBoost, horizDir.z * strength);
            target.hurtMarked = true;
        }

        this.level().playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                this.getSoundSource(),
                1.0F,
                1.0F
        );

        // 💥 Particles on impact
        if (this.level() instanceof ServerLevel serverLevel) {
            // Big poof
            serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    this.getX(),
                    this.getY() + 0.1D,
                    this.getZ(),
                    30,
                    0.6D, 0.1D, 0.6D,
                    0.05D
            );

            // A few block dust bits rising up
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY() + 0.2D,
                    this.getZ(),
                    10,
                    0.4D, 0.1D, 0.4D,
                    0.02D
            );
        }

        this.level().gameEvent(GameEvent.ENTITY_DAMAGE, this.position(), GameEvent.Context.of(this));
    }


    /**
     * Reset per-fall state when we start another dive.
     */
    public void resetSlamState() {
        this.hasSlammedThisFall = false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }
}
