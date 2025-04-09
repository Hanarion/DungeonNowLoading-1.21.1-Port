package dev.hexnowloading.dungeonnowloading.entity.ai;

import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import dev.hexnowloading.dungeonnowloading.util.WeightedRandomBag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FairkeeperOurosShootVertexOrbGoal extends StoppableGoal {

    private final FairkeeperOurosEntity ouros;
    private FairkeeperOurosEntity.FairkeeperOurosState state;
    private FairkeeperOurosPartEntity currentPart;
    private FairkeeperSerpentCallerEntity caller;
    private int attackTicks;
    private int targetCount;
    private int bulletCount;
    private final int bulletPerPlayer;
    private final int radius;
    private final float inaccuracy;

    private final int START_UP_DELAY = 40;
    private final int BULLET_INTERVAL = 10;

    public FairkeeperOurosShootVertexOrbGoal(FairkeeperOurosEntity.FairkeeperOurosState state, FairkeeperOurosEntity ouros, int bulletPerPlayer, float inaccuracy, int radius) {
        this.ouros = ouros;
        this.state = state;
        this.bulletPerPlayer = bulletPerPlayer;
        this.inaccuracy = inaccuracy;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        return this.ouros.getTarget() != null && this.ouros.getTarget().isAlive() && this.ouros.isState(state);
    }

    @Override
    public void start() {
        super.start();
        this.attackTicks = reducedTickDelay(START_UP_DELAY);
        this.targetCount = 1;
        this.caller = (FairkeeperSerpentCallerEntity) this.ouros.getCaller();
        if (caller != null) {
            this.targetCount = this.ouros.getThreatScoreMap().size();
        }
        this.bulletCount = this.targetCount * this.bulletPerPlayer;
    }

    @Override
    public void stop() {
        this.ouros.stopAttacking(20);
    }

    @Override
    public void tick() {

        if (this.bulletCount <= 0) {
            this.stopGoal();
            return;
        }

        if (this.attackTicks > 0) {
            this.attackTicks--;
            return;
        }

        //this.randomCurrentPart();

        this.shootRandomPlayer();

        bulletCount--;
        attackTicks = reducedTickDelay(BULLET_INTERVAL);
    }

    private void randomCurrentPart() {
        RandomSource randomSource = this.ouros.getRandom();
        int randomIndex = randomSource.nextInt(13) + 1;
        this.currentPart = (FairkeeperOurosPartEntity) this.ouros.getChild();
        for (int i = 0; i < randomIndex; i++) {
            this.currentPart = (FairkeeperOurosPartEntity) this.currentPart.getChild();
            if (this.currentPart == null) {
                this.stopGoal();
                return;
            }
        }
    }

    private void shootRandomPlayer() {
        WeightedRandomBag<UUID> bag = new WeightedRandomBag<>();
        this.ouros.getThreatScoreMap().forEach(bag::addEntry);
        UUID randomUUID = bag.getRandom();
        Entity entity = ((ServerLevel) this.ouros.level()).getEntity(randomUUID);

        if (entity != null) {
            for (int attempt = 0; attempt < 3; attempt++) {
                this.randomCurrentPart();
                if (this.currentPart == null) return;

                Vec3 start = new Vec3(this.currentPart.getX(), this.currentPart.getY() - 1, this.currentPart.getZ());
                Vec3 end = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);

                BlockHitResult hitResult = this.ouros.level().clip(new ClipContext(
                        start,
                        end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this.currentPart
                ));

                if (hitResult.getType() == HitResult.Type.MISS) {
                    // Clear path — shoot!
                    VertexOrbProjectileEntity projectile = new VertexOrbProjectileEntity(this.ouros.level(), this.ouros, radius);
                    projectile.shootTowardsTarget(start.x, start.y, start.z, (LivingEntity) entity, 1.0F, this.inaccuracy);
                    this.ouros.level().addFreshEntity(projectile);

                    this.caller.addMinion(projectile.getUUID());

                    this.ouros.level().playSound(null, start.x, start.y, start.z, SoundEvents.WITHER_SHOOT, this.currentPart.getSoundSource(), 3.0F, 1.0F + (this.currentPart.getRandom().nextFloat() - this.currentPart.getRandom().nextFloat()) * 0.2F);
                    break; // Done shooting
                }
            }
        }
    }
}
