package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class FairkeeperOurosBodyShootVertexOrbGoal extends StoppableGoal{

    private final FairkeeperOurosPartEntity part;
    private final FairkeeperOurosPartEntity.FairkeeperOurosPartState state;

    private FairkeeperOurosEntity ouros;
    private int progress;
    private int attackTicks;

    private final static int ATTACK_INTERVAL = 60;

    public FairkeeperOurosBodyShootVertexOrbGoal(FairkeeperOurosPartEntity part, FairkeeperOurosPartEntity.FairkeeperOurosPartState state) {
        this.part = part;
        this.state = state;
    }
    @Override
    public boolean canUse() {
        return this.part.isState(state) && this.part.getShootingTarget() != null && this.part.getShootingTarget().isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.ouros = (FairkeeperOurosEntity) this.part.getHead();
        this.progress = 0;
        this.attackTicks = ATTACK_INTERVAL;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity livingEntity = (LivingEntity) this.part.getShootingTarget();
        if (livingEntity == null) return false;

        if (!this.ouros.canAttack(livingEntity)) return false;

        Team teamMob = this.ouros.getTeam();
        Team teamOther = livingEntity.getTeam();
        if (teamMob != null && teamOther == teamMob) {
            return false;
        }

        int arenaSize = this.ouros.getArenaSize();
        BlockPos arenaCenter = this.ouros.getArenaCenter();
        boolean withinX = Math.abs(livingEntity.getX() - arenaCenter.getX() + 0.5f) < arenaSize + 0.5F;
        boolean withinY = Math.abs(livingEntity.getY() - arenaCenter.getY() + 0.5f) < arenaSize + 0.5F;
        boolean withinZ = Math.abs(livingEntity.getZ() - arenaCenter.getZ() + 0.5f) < arenaSize + 0.5F;
        if (!withinX || !withinY || !withinZ) {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    public void stop() {
        this.part.setState(FairkeeperOurosPartEntity.FairkeeperOurosPartState.IDLE);
        this.part.setInaccuracy(0.0F);
        this.part.setCancelShooting(false);
        this.part.setShootingTarget(null);
    }

    @Override
    public void tick() {
        if (this.progress == 0) {
            this.progress++;
            this.part.playCannonSetupAnimation(() -> this.progress++);
        } else if (this.progress == 2) {
            this.part.aimCannonAtPlayer(this.part.getShootingTarget());
            if (this.part.isCancelShooting()) {
                this.ouros.level().playSound(null, this.part.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0f, (1.0f + (this.ouros.level().random.nextFloat() - this.ouros.level().random.nextFloat()) * 0.2f) * 0.7f);
                ((ServerLevel) (this.ouros.level())).sendParticles(ParticleTypes.EXPLOSION, this.part.getX(), this.part.getY(), this.part.getZ(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
                this.part.playCannonCancelAnimation(this::stopGoal);
                this.progress += 2;
            } else if (this.attackTicks-- <= 0) {
                shootRandomPlayer();
            }
        } else if (this.progress == 3) {
            this.part.playCannonPackAnimation(this::stopGoal);
            this.progress++;
        }
    }

    private void shootRandomPlayer() {

        Entity entity = this.part.getShootingTarget();

        if (entity == null) {
            this.progress++;
            return;
        }

        Vec3 start = new Vec3(this.part.getX(), this.part.getY() + entity.getBbHeight() * 0.5F, this.part.getZ());
        Vec3 end = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);

        BlockHitResult hitResult = this.ouros.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.part
        ));

        if (hitResult.getType() == HitResult.Type.MISS) {

            VertexOrbProjectileEntity projectile = new VertexOrbProjectileEntity(this.ouros.level(), this.ouros, 2);
            projectile.shootTowardsTarget(start.x, start.y, start.z, (LivingEntity) entity, 1.0F, this.part.getInaccuracy());
            this.ouros.level().addFreshEntity(projectile);

            this.ouros.getCaller().addMinion(projectile.getUUID());

            this.ouros.level().playSound(null, start.x, start.y, start.z, SoundEvents.WITHER_SHOOT, this.part.getSoundSource(), 3.0F, 1.0F + (this.part.getRandom().nextFloat() - this.part.getRandom().nextFloat()) * 0.2F);
            this.progress++;
        }
    }
}
