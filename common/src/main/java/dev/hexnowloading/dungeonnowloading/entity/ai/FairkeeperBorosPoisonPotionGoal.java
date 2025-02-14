package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FairkeeperBorosPoisonPotionGoal extends Goal {

    private final FairkeeperBorosEntity boros;
    private final FairkeeperBorosEntity.FairkeeperBorosState state;
    private LivingEntity target;
    private int attackTicks;
    private FairkeeperBorosPartEntity currentPart;

    private final int SHOOTING_INTERVAL = 10;
    private final int START_UP_DELAY = 40;

    public FairkeeperBorosPoisonPotionGoal(FairkeeperBorosEntity.FairkeeperBorosState state, FairkeeperBorosEntity boros) {
        this.boros = boros;
        this.state = state;
    }

    @Override
    public boolean canUse() {
        this.target = this.boros.getTarget();
        return this.target != null && this.target.isAlive() && this.boros.isState(state);
    }

    @Override
    public void start() {
        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL + START_UP_DELAY);
        this.currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
    }

    @Override
    public void tick() {
        if (this.currentPart == null || this.currentPart.isTail()) {
            this.currentPart = (FairkeeperBorosPartEntity) this.boros.getChild();
            return;
        }

        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }

        this.attackTicks = reducedTickDelay(SHOOTING_INTERVAL);

        while (this.currentPart.hasArmor()) {
            this.currentPart = (FairkeeperBorosPartEntity) this.currentPart.getChild();
            if (this.currentPart == null) {
                this.boros.stopAttacking(20);
                return;
            }
        }

        this.shootPotion(90.0F);
        this.shootPotion(-90.0f);

        this.currentPart = (FairkeeperBorosPartEntity) this.currentPart.getChild();
    }

    private void shootPotion(float angle) {
        double viewDistance = 2.0F;
        Vec3 viewVector = this.currentPart.getViewVector(1.0F);
        double dx = viewVector.x;
        double dz = viewVector.z;
        double angleRadians = Math.toRadians(angle);
        double rx = dx * Math.cos(angleRadians) - dz * Math.sin(angleRadians);
        double rz = dx * Math.sin(angleRadians) - dz * Math.cos(angleRadians);
        ItemStack potion = createPotion();
        ThrownPotion thrownPotion = new ThrownPotion(this.boros.level(), this.boros);
        thrownPotion.setItem(potion);
        thrownPotion.setPos(this.currentPart.getX() + rx * viewDistance, this.currentPart.getY() + this.currentPart.getBoundingBox().getYsize() / 2, this.currentPart.getZ() + rz * viewDistance);
        thrownPotion.shootFromRotation(this.currentPart, this.currentPart.getXRot(), this.currentPart.getYRot() + (float) Math.toDegrees(angleRadians), 0.0F, 0.5F, 1.0F);
        this.currentPart.level().addFreshEntity(thrownPotion);
    }

    private ItemStack createPotion() {
        ItemStack potion = new ItemStack(Items.LINGERING_POTION);
        MobEffectInstance poisonEffect = new MobEffectInstance(MobEffects.POISON, 600, 2);
        PotionUtils.setCustomEffects(potion, List.of(poisonEffect));
        PotionUtils.setPotion(potion, Potions.POISON);
        return potion;
    }
}
