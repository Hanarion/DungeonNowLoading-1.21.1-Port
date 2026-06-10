package dev.hexnowloading.dungeonnowloading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class BurnacleGasParticle extends TextureSheetParticle {

    private static final int FRAME_COUNT = 11;
    private static final int TICKS_PER_FRAME = 2;

    private final SpriteSet spriteSet;

    protected BurnacleGasParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed,
                                  SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.spriteSet = spriteSet;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.hasPhysics = false;
        this.friction = 0.94F;
        this.lifetime = FRAME_COUNT * TICKS_PER_FRAME;
        this.quadSize *= 2.4F + level.random.nextFloat() * 0.6F;
        this.alpha = 0.85F;
        this.setSprite(spriteSet.get(0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        int frame = Math.min(FRAME_COUNT - 1, this.age / TICKS_PER_FRAME);
        this.setSprite(this.spriteSet.get(frame, FRAME_COUNT));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new BurnacleGasParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
