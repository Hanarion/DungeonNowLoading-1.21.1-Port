package dev.hexnowloading.dungeonnowloading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class ScorcherFlameParticle extends TextureSheetParticle {

    private final float targetQuadSize; // ✅ Store target size to scale up to
    private SpriteSet spriteSet;

    protected ScorcherFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.targetQuadSize = Math.min(level.random.nextFloat() * 0.5F + 0.5F, 0.5F); // ✅ Store the max size
        this.quadSize = 0.0F; // ✅ Start from 0
        this.hasPhysics = true;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.bCol = 1.0F;
        this.gCol = 1.0F;
        this.spriteSet = spriteSet;
        this.friction = 0.95F;
        this.lifetime = 5 + level.random.nextInt(10);
    }

    @Override
    public void tick() {
        this.age++;
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        // ✅ Lerp quadSize from 0 to targetQuadSize
        float progress = (float) this.age / this.lifetime; // ✅ Normalized progress (0 to 1)
        this.quadSize = targetQuadSize * progress; // ✅ Smooth scaling

        // ✅ Color fade effect (yellow to red)
        this.gCol = 1.0F - 0.9F * progress;
        this.bCol = 1.0F - 0.9F * progress;

        // ✅ Set sprite animation
        int sprite = this.age % 8;
        this.setSprite(spriteSet.get(sprite, 8));

        // ✅ Apply movement and friction
        this.move(this.xd, this.yd, this.zd);
        this.xd *= (double) this.friction;
        this.yd *= (double) this.friction;
        this.zd *= (double) this.friction;
    }
    @Override
    public int getLightColor(float $$0) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ScorcherFlameParticle particle = new ScorcherFlameParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            particle.setSprite(sprites.get(0, 1));
            return particle;
        }
    }
}
