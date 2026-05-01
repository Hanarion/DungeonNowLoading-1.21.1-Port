package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.SnifferTrailParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class SnifferTrailParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final double startX;
    private final double startY;
    private final double startZ;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int travelLifetime;
    private int delay;
    private int travelAge;

    protected SnifferTrailParticle(ClientLevel level, double x, double y, double z, double targetX, double targetY, double targetZ, int travelLifetime, int delay, SpriteSet spriteSet) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.spriteSet = spriteSet;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.targetX = x + targetX;
        this.targetY = y + targetY;
        this.targetZ = z + targetZ;
        this.delay = Math.max(0, delay);
        this.travelLifetime = Math.max(1, travelLifetime);
        this.lifetime = this.delay + this.travelLifetime;
        this.alpha = 0.0F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.125F;
        if (this.random.nextFloat() < 0.22F) {
            this.rCol = 0.38F + this.random.nextFloat() * 0.08F;
            this.gCol = 0.58F + this.random.nextFloat() * 0.14F;
            this.bCol = 0.22F + this.random.nextFloat() * 0.08F;
        } else {
            this.rCol = 0.52F + this.random.nextFloat() * 0.13F;
            this.gCol = 0.34F + this.random.nextFloat() * 0.09F;
            this.bCol = 0.18F + this.random.nextFloat() * 0.07F;
        }
        this.setSprite(spriteSet.get(this.random.nextInt(8), 8));
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
        if (this.delay > 0) {
            this.delay--;
            this.alpha = 0.0F;
            return;
        }

        this.travelAge++;
        float progress = Mth.clamp((float)this.travelAge / (float)this.travelLifetime, 0.0F, 1.0F);
        this.alpha = getAlpha(progress);
        this.setPos(
                Mth.lerp(progress, this.startX, this.targetX),
                Mth.lerp(progress, this.startY, this.targetY),
                Mth.lerp(progress, this.startZ, this.targetZ)
        );
    }

    private float getAlpha(float progress) {
        float fadeIn = Mth.clamp(progress / 0.08F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((1.0F - progress) / 0.25F, 0.0F, 1.0F);
        return 0.86F * fadeIn * fadeOut;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    public static class Factory implements ParticleProvider<SnifferTrailParticleType.Data> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SnifferTrailParticleType.Data data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SnifferTrailParticle(level, x, y, z, data.getTargetX(), data.getTargetY(), data.getTargetZ(), data.getTravelLifetime(), data.getDelay(), this.sprites);
        }
    }
}
