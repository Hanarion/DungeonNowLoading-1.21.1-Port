package dev.hexnowloading.dungeonnowloading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MimiclingImpactBlockParticle extends TerrainParticle {
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double maxDistanceSqr;
    private boolean reachedRange;

    protected MimiclingImpactBlockParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, BlockState state) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, state);
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.maxDistanceSqr = targetX * targetX + targetY * targetY + targetZ * targetZ;
        this.xd = targetX * 0.15D;
        this.yd = targetY * 0.15D;
        this.zd = targetZ * 0.15D;
        this.gravity = 0.55F;
        this.friction = 0.86F;
        this.hasPhysics = true;
        this.lifetime = 14 + this.random.nextInt(6);
        this.quadSize *= 0.85F + this.random.nextFloat() * 0.35F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04D * this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.65D;
            this.zd *= 0.65D;
        }

        double dx = this.x - this.startX;
        double dy = this.y - this.startY;
        double dz = this.z - this.startZ;
        if (!this.reachedRange && dx * dx + dy * dy + dz * dz >= this.maxDistanceSqr) {
            this.reachedRange = true;
            this.lifetime = Math.min(this.lifetime, this.age + 4);
        }
        if (this.reachedRange) {
            int remaining = Math.max(0, this.lifetime - this.age);
            this.setAlpha(Mth.clamp(remaining / 4.0F, 0.0F, 1.0F));
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = ((float)this.age + partialTick) / (float)this.lifetime;
        return super.getQuadSize(partialTick) * Mth.clamp(1.0F - progress * 0.45F, 0.45F, 1.0F);
    }

    public static class Factory implements ParticleProvider<BlockParticleOption> {
        @Nullable
        @Override
        public Particle createParticle(BlockParticleOption data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new MimiclingImpactBlockParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, data.getState());
        }
    }
}
