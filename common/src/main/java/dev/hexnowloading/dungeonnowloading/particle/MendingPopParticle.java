package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.Nullable;

public class MendingPopParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final float baseQuadSize;

    protected MendingPopParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 float scale,
                                 SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.spriteSet = spriteSet;

        this.baseQuadSize = this.quadSize * 1.5F;
        this.quadSize = this.baseQuadSize * scale;

        this.hasPhysics = true;
        this.friction = 0.95F;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.alpha = 1.0F;
        this.lifetime = 16;

        this.setSprite(spriteSet.get(0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) return;

        if (this.age % 2 == 0) {
            int sprite = Math.min(7, this.age / 2);
            this.setSprite(spriteSet.get(sprite, 8));
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<ScalableParticleType.ScalableParticleData> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(ScalableParticleType.ScalableParticleData data, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {

            return new MendingPopParticle(
                    level,
                    x, y, z,
                    xSpeed, ySpeed, zSpeed,
                    data.getScale(),
                    this.sprites
            );
        }
    }
}
