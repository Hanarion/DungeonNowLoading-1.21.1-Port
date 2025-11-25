package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.ReaperSpiderAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.ReaperSpiderMoveControl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ReaperSpiderEntity extends Spider {

    private boolean fastClimb;

    public ReaperSpiderEntity(EntityType<? extends ReaperSpiderEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new ReaperSpiderMoveControl(this,
                3.0D, // sideStrafeSpeed (very fast sidestep)
                0.8D  // backStrafeSpeed
        );
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 19.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new ReaperSpiderAttackGoal(this));

        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean onClimbable() {
        boolean vanilla = super.onClimbable();
        return vanilla || this.horizontalCollision;
    }

    public void setFastClimb(boolean fastClimb) {
        this.fastClimb = fastClimb;
    }

    public boolean isFastClimb() {
        return fastClimb;
    }

    @Override
    public void travel(Vec3 travelVector) {
        // let vanilla / pathfinding handle movement first
        super.travel(travelVector);

        // then post-adjust climb speed if we want it faster
        if (this.fastClimb && this.onClimbable() && !this.isFallFlying()) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.y > 0.0D) {
                // multiply the existing climb speed
                double multiplier = 2.0D; // 2x vanilla climb speed, tweak to taste
                this.setDeltaMovement(motion.x, motion.y * multiplier, motion.z);
            }
        }
    }
}
