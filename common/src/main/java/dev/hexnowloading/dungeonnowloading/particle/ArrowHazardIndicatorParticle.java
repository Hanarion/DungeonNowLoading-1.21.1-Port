package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;

public class ArrowHazardIndicatorParticle extends TextureSheetParticle {
    private SpriteSet spriteSet;
    private float scale;
    private float lifetimeOverride;

    protected ArrowHazardIndicatorParticle(ClientLevel clientLevel, double x, double y, double z, float scale, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);

        float colOffset = DNLMath.randomRange(0.5f, 1.0f);

        this.rCol = colOffset;
        this.gCol = colOffset;
        this.bCol = colOffset;
        this.alpha = 0;

        this.quadSize = 1.0F;
        this.gravity = 0.0F;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.spriteSet = spriteSet;
        this.lifetime = 20;
        this.scale = scale;
    }

    @Override
    public void tick() {
        float progress = (float) this.age / this.lifetime;

        // Quadratic growth (fast start, slow end)
        this.quadSize = this.scale * (1 - (1 - progress) * (1 - progress));

        // Fade in for the first 10 ticks
        if (this.age < 10) {
            this.alpha = this.age / 10.0F; // Gradually increase alpha from 0 to 1
        } else {
            this.alpha = 1.0F; // Fully visible for the remaining time
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize;
    }



    @Override
    public int getLightColor(float f) {
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

        @Override
        public Particle createParticle(ScalableParticleType.ScalableParticleData scalableParticleData, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ArrowHazardIndicatorParticle particle = new ArrowHazardIndicatorParticle(clientLevel, x, y, z, scalableParticleData.getScale(), this.sprites);
            particle.setSprite(this.sprites.get(0, 1));
            return particle;
        }
    }
}
