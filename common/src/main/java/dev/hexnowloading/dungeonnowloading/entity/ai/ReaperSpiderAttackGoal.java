package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ReaperSpiderAttackGoal extends Goal {
    private static final double ATTACK_TRIGGER_RANGE = 2.25D;
    private static final double SLASH_CENTER_OFFSET = 1.0D;
    private static final double DOUBLE_SLASH_HORIZONTAL_RADIUS = 2.4D;
    private static final double DOUBLE_SLASH_TRIGGER_HORIZONTAL_RADIUS = 1.6D;
    private static final double SINGLE_SLASH_HORIZONTAL_RADIUS = 1.9D;
    private static final double SLASH_VERTICAL_RADIUS = 1.6D;
    private static final double DASH_SPEED = 1.2D;
    private static final int WINDUP_TICKS = reducedTickDelay(10);
    private static final int DASH_TICKS = reducedTickDelay(60);
    private static final int RECOVER_TICKS = reducedTickDelay(20);
    private static final int TACKLE_COOLDOWN_TICKS = reducedTickDelay(200);
    private static final int SINGLE_SLASH_POISON_TICKS = reducedTickDelay(100);
    private static final int CHASING_TICKS = reducedTickDelay(200);
    private static final int DOUBLE_SLASH_CHASE_REDUCTION = reducedTickDelay(160);
    private static final int SINGLE_SLASH_CHASE_REDUCTION = reducedTickDelay(60);
    private static final float SINGLE_SLASH_DAMAGE_MULTIPLIER = 0.5F;

    private final ReaperSpiderEntity mob;

    private State state = State.CHASE;
    private LivingEntity target;
    private int windupTicks;
    private int dashTicks;
    private int recoverTicks;
    private int chasingTicksRemaining;
    private int tackleCooldownTicks;
    private Vec3 memorizedTargetPos;
    private Vec3 dashDirection;
    private boolean dashHasAttacked;
    private boolean passedTargetXZ;

    private enum State {
        CHASE,
        ATTACK_WINDUP,
        ATTACK_DASH,
        POST_ATTACK_DELAY
    }

    public ReaperSpiderAttackGoal(ReaperSpiderEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public boolean isDashOrDoubleSlashSequenceActive() {
        return this.state == State.ATTACK_DASH || this.state == State.POST_ATTACK_DELAY;
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null || !livingEntity.isAlive()) {
            return false;
        }

        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            return false;
        }

        this.target = livingEntity;
        return this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.CHASING && this.mob.canAttack(livingEntity);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (this.target instanceof Player player && player.getAbilities().instabuild) {
            return false;
        }

        return this.mob.getBehaviorState() == ReaperSpiderEntity.BehaviorState.CHASING && this.mob.canAttack(this.target);
    }

    @Override
    public void start() {
        this.state = State.CHASE;
        this.windupTicks = 0;
        this.dashTicks = 0;
        this.recoverTicks = 0;
        this.chasingTicksRemaining = CHASING_TICKS;
        this.memorizedTargetPos = null;
        this.dashDirection = null;
        this.dashHasAttacked = false;
        this.passedTargetXZ = false;
        this.tackleCooldownTicks = 0;
        this.mob.queueRevealTackle();
    }

    @Override
    public void stop() {
        this.target = null;
        this.state = State.CHASE;
        this.windupTicks = 0;
        this.dashTicks = 0;
        this.recoverTicks = 0;
        this.chasingTicksRemaining = 0;
        this.memorizedTargetPos = null;
        this.dashDirection = null;
        this.dashHasAttacked = false;
        this.passedTargetXZ = false;
        this.mob.setFastClimb(false);
        this.mob.setLocomotionLocked(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        if (this.tackleCooldownTicks > 0) {
            this.tackleCooldownTicks--;
        }

        this.controlLooking();

        switch (this.state) {
            case CHASE -> this.tickChase();
            case ATTACK_WINDUP -> this.tickWindup();
            case ATTACK_DASH -> this.tickDash();
            case POST_ATTACK_DELAY -> this.tickPostAttackDelay();
        }
    }

    private void tickChase() {
        if (this.target == null) {
            return;
        }

        double distanceToTarget = this.mob.distanceToSqr(this.target);
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        double tackleMinRangeSqr = this.mob.getTackleMinRange() * this.mob.getTackleMinRange();
        boolean tackleAvailable = this.tackleCooldownTicks <= 0 && distanceToTarget >= tackleMinRangeSqr;

        this.mob.getNavigation().moveTo(this.target, this.mob.getChaseSpeedMultiplier());

        if (--this.chasingTicksRemaining <= 0) {
            this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.RECOVERY);
            this.mob.setRevealed(true);
            this.mob.setInvisible(false);
            this.mob.getNavigation().stop();
            return;
        }

        if (this.mob.consumeRevealTackle()
                && hasLineOfSight
                && this.tackleCooldownTicks <= 0
                && !this.mob.isAttackAnimationRunning()) {
            this.startWindup();
            return;
        }

        if (hasLineOfSight
                && !tackleAvailable
                && !this.mob.isAttackAnimationRunning()
                && this.isTargetInSingleSlashRange()) {
            this.mob.playSingleSlashAnimation(this::performSingleSlash);
            return;
        }

        if (hasLineOfSight
                && distanceToTarget <= ATTACK_TRIGGER_RANGE * ATTACK_TRIGGER_RANGE
                && tackleAvailable
                && !this.mob.isAttackAnimationRunning()) {
            this.startWindup();
        }
    }

    private void startWindup() {
        this.state = State.ATTACK_WINDUP;
        this.windupTicks = WINDUP_TICKS;
        this.mob.setLocomotionLocked(true);
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
        this.mob.playWindUpAnimation();
    }

    private void tickWindup() {
        if (this.target != null && this.target.isAlive()) {
            this.faceTargetBody(this.target);
        }
        if (--this.windupTicks <= 0) {
            this.startDash();
        }
    }

    private void startDash() {
        this.state = State.ATTACK_DASH;
        this.dashTicks = DASH_TICKS;
        this.dashHasAttacked = false;
        this.passedTargetXZ = false;
        this.tackleCooldownTicks = TACKLE_COOLDOWN_TICKS;
        this.mob.setFastClimb(true);
        this.mob.setLocomotionLocked(false);

        if (this.target != null && this.target.isAlive()) {
            this.memorizedTargetPos = this.target.position();
        } else {
            this.memorizedTargetPos = this.mob.position().add(this.mob.getForward());
        }

        Vec3 horizontal = this.memorizedTargetPos.subtract(this.mob.position());
        horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            float yawRad = this.mob.getYRot() * (Mth.PI / 180.0F);
            horizontal = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        }

        this.dashDirection = horizontal.normalize();
        this.mob.getNavigation().stop();
    }

    private void tickDash() {
        if (this.dashDirection == null) {
            this.startRecover();
            return;
        }

        float yaw = (float) (Mth.atan2(this.dashDirection.z, this.dashDirection.x) * (180.0F / Math.PI)) - 90.0F;
        this.mob.setYRot(yaw);
        this.mob.setYHeadRot(yaw);
        this.mob.yBodyRot = yaw;

        Vec3 motion = this.dashDirection.scale(DASH_SPEED * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.mob.setDeltaMovement(motion.x, this.mob.getDeltaMovement().y, motion.z);

        if (!this.dashHasAttacked && this.isTargetInDoubleSlashTriggerRange()) {
            this.mob.playDoubleSlashAnimation(() -> this.performFrontalSlash(1.0F));
            this.dashHasAttacked = true;
            this.startRecover();
            return;
        }

        if (!this.dashHasAttacked && this.hasPassedTargetXZ()) {
            this.passedTargetXZ = true;
            this.mob.playDoubleSlashAnimation(() -> this.performFrontalSlash(1.0F));
            this.dashHasAttacked = true;
            this.startRecover();
            return;
        }

        if (--this.dashTicks <= 0) {
            if (!this.dashHasAttacked && this.isTargetInDoubleSlashTriggerRange()) {
                this.mob.playDoubleSlashAnimation(() -> this.performFrontalSlash(1.0F));
                this.dashHasAttacked = true;
            }
            this.startRecover();
        }
    }

    private void startRecover() {
        this.state = State.POST_ATTACK_DELAY;
        this.recoverTicks = RECOVER_TICKS;
        this.mob.setFastClimb(false);
        this.mob.setLocomotionLocked(true);
        this.mob.getNavigation().stop();
        if (this.mob.onGround()) {
            this.mob.setDeltaMovement(0.0D, this.mob.getDeltaMovement().y, 0.0D);
        }
    }

    private void tickPostAttackDelay() {
        if (--this.recoverTicks <= 0) {
            this.mob.setLocomotionLocked(false);
            if (this.chasingTicksRemaining <= 0) {
                this.mob.setBehaviorState(ReaperSpiderEntity.BehaviorState.RECOVERY);
                this.mob.setRevealed(true);
                this.mob.setInvisible(false);
                this.mob.getNavigation().stop();
            } else {
                this.state = State.CHASE;
            }
        }
    }

    private void controlLooking() {
        if (this.target == null || this.state == State.POST_ATTACK_DELAY) {
            return;
        }

        if (this.state == State.ATTACK_DASH && this.memorizedTargetPos != null) {
            this.mob.getLookControl().setLookAt(this.memorizedTargetPos.x, this.memorizedTargetPos.y, this.memorizedTargetPos.z, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        }
    }

    private boolean isTargetInDoubleSlashRange() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        Vec3 slashCenter = this.getSlashCenter();
        Vec3 targetCenter = this.target.position().add(0.0D, this.target.getBbHeight() * 0.5D, 0.0D);
        return this.isInsideSlashVolume(slashCenter, targetCenter, DOUBLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);
    }

    private boolean isTargetInDoubleSlashTriggerRange() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        Vec3 slashCenter = this.getSlashCenter();
        Vec3 targetCenter = this.target.position().add(0.0D, this.target.getBbHeight() * 0.5D, 0.0D);
        return this.isInsideSlashVolume(slashCenter, targetCenter, DOUBLE_SLASH_TRIGGER_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);
    }

    private boolean isTargetInSingleSlashRange() {
        if (this.target == null || !this.target.isAlive()) {
            return false;
        }

        Vec3 slashCenter = this.getSlashCenter();
        Vec3 targetCenter = this.target.position().add(0.0D, this.target.getBbHeight() * 0.5D, 0.0D);
        return this.isInsideSlashVolume(slashCenter, targetCenter, SINGLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);
    }

    private void performFrontalSlash(float damageMultiplier) {
        if (this.target == null || !this.target.isAlive() || !this.isTargetInDoubleSlashRange()) {
            return;
        }

        this.mob.spawnDoubleSlashRangeParticles(SLASH_CENTER_OFFSET, DOUBLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);

        var damageSource = this.mob.damageSources().mobAttack(this.mob);
        float damage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * damageMultiplier;
        boolean blocked = this.isBlockedByShield(this.target, damageSource);
        if (this.target.hurt(damageSource, damage)) {
            this.mob.applyTackleKnockback(this.target);
            this.mob.applyAttackEffects(this.target, reducedTickDelay(300));
            this.mob.destroyBlocksInSlashArea(SLASH_CENTER_OFFSET, DOUBLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);
            this.chasingTicksRemaining -= DOUBLE_SLASH_CHASE_REDUCTION;
        } else if (blocked && this.disableShield(this.target)) {
            this.mob.destroyBlocksInSlashArea(SLASH_CENTER_OFFSET, DOUBLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);
        } else if (blocked) {
            this.disableShield(this.target);
        }
    }

    private void performSingleSlash() {
        if (this.target == null || !this.target.isAlive() || !this.isTargetInSingleSlashRange()) {
            return;
        }

        this.mob.spawnSlashRangeParticles(SLASH_CENTER_OFFSET, SINGLE_SLASH_HORIZONTAL_RADIUS, SLASH_VERTICAL_RADIUS);

        var damageSource = this.mob.damageSources().mobAttack(this.mob);
        float damage = (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE) * SINGLE_SLASH_DAMAGE_MULTIPLIER;
        boolean blocked = this.isBlockedByShield(this.target, damageSource);
        if (this.target.hurt(damageSource, damage)) {
            this.mob.applySingleSlashKnockback(this.target);
            this.mob.applyAttackEffects(this.target, SINGLE_SLASH_POISON_TICKS);
            this.chasingTicksRemaining -= SINGLE_SLASH_CHASE_REDUCTION;
        } else if (blocked) {
            this.disableShield(this.target);
        }
    }

    private boolean isBlockedByShield(LivingEntity target, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (!(target instanceof Player player)) {
            return false;
        }

        return player.isBlocking() && player.isDamageSourceBlocked(damageSource);
    }

    private boolean disableShield(LivingEntity target) {
        if (!(target instanceof Player player)) {
            return false;
        }

        var useStack = player.getUseItem();
        if (useStack.isEmpty() || !(useStack.getItem() instanceof ShieldItem)) {
            return false;
        }

        player.stopUsingItem();
        player.getCooldowns().addCooldown(useStack.getItem(), 120);
        player.level().broadcastEntityEvent(player, (byte) 30);
        return true;
    }

    private Vec3 getSlashCenter() {
        Vec3 forward = this.mob.getForward().normalize();
        Vec3 origin = this.mob.position().add(0.0D, this.mob.getBbHeight() * 0.5D, 0.0D);
        return origin.add(forward.scale(SLASH_CENTER_OFFSET));
    }

    private boolean isInsideSlashVolume(Vec3 center, Vec3 point, double horizontalRadius, double verticalRadius) {
        double dx = point.x - center.x;
        double dy = point.y - center.y;
        double dz = point.z - center.z;
        double horizontal = (dx * dx + dz * dz) / (horizontalRadius * horizontalRadius);
        double vertical = (dy * dy) / (verticalRadius * verticalRadius);
        return horizontal + vertical <= 1.0D;
    }

    private boolean hasPassedTargetXZ() {
        if (this.memorizedTargetPos == null || this.dashDirection == null || this.passedTargetXZ) {
            return false;
        }

        Vec3 toTarget = new Vec3(
                this.memorizedTargetPos.x - this.mob.getX(),
                0.0D,
                this.memorizedTargetPos.z - this.mob.getZ()
        );

        return toTarget.dot(this.dashDirection) <= 0.0D;
    }

    private void faceTargetBody(LivingEntity target) {
        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
        float newYaw = Mth.approachDegrees(this.mob.getYRot(), targetYaw, 30.0F);

        this.mob.setYRot(newYaw);
        this.mob.setYHeadRot(newYaw);
        this.mob.yBodyRot = newYaw;
        this.mob.yBodyRotO = newYaw;
    }
}
