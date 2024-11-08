package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class RedstoneShockwaveParticle extends TextureSheetParticle {

    private final float scale;
    private SpriteSet spriteSet;

    protected RedstoneShockwaveParticle(ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale, SpriteSet spriteSet) {
        super(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize *= 5.9F + level.random.nextFloat() * 0.5F;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.spriteSet = spriteSet;
        this.lifetime = 8;
        this.scale = scale;
    }

    @Override
    public void tick() {
        //this.quadSize -= Math.min(this.quadSize, 0.05F);
        //this.gCol = (float) this.age / this.lifetime;
        //this.bCol = (float) this.age / this.lifetime;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(spriteSet);
        }
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * this.scale;
    }

    @Override
    protected int getLightColor(float $$0) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<ScalableParticleType.ScalableParticleData> {

        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(ScalableParticleType.ScalableParticleData scalableParticleData, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RedstoneShockwaveParticle particle = new RedstoneShockwaveParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, scalableParticleData.getScale(), this.spriteSet);
            particle.setSprite(spriteSet.get(0, 1));
            return particle;
        }
    }
}
