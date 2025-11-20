package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.WispLanternAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.control.move.HoveringFlyingMoveControl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WispLanternEntity extends FlyingMob implements Enemy {
    public WispLanternEntity(EntityType<? extends FlyingMob> type, Level level) {
        super(type, level);
        this.moveControl = new HoveringFlyingMoveControl(this);
        this.setNoGravity(true);
        //this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FLYING_SPEED, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WispLanternAttackGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {}

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }
}
