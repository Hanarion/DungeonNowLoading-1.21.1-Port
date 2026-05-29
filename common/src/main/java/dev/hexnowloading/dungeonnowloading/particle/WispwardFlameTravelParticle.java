package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.Nullable;

public class WispwardFlameTravelParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final int fadeInTicks;
    private final int fadeOutTicks;
    private final float baseQuadSize;

    protected WispwardFlameTravelParticle(ClientLevel level,
                                          double x, double y, double z,
                                          float vx, float vy, float vz,
                                          int fadeInTicks, int fadeOutTicks, int lifetimeTicks,
                                          SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);
        this.spriteSet = spriteSet;
        this.fadeInTicks = Math.max(0, fadeInTicks);
        this.fadeOutTicks = Math.max(0, fadeOutTicks);
        this.lifetime = Math.max(1, lifetimeTicks);
        this.baseQuadSize = this.quadSize * 0.55F;
        this.quadSize = this.baseQuadSize;
        this.hasPhysics = false;
        this.alpha = 0.0F;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.bCol = 0.65F;
        this.gCol = 0.95F;
        this.setSprite(this.spriteSet.get(0, 8));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        int sprite = this.age % 8;
        this.setSprite(this.spriteSet.get(sprite, 8));
        this.alpha = this.computeAlpha();
        this.quadSize = this.baseQuadSize * (0.85F + 0.25F * (float) Math.sin(this.age * 0.55F));
        this.move(this.xd, this.yd, this.zd);

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    private float computeAlpha() {
        float fadeIn = 1.0F;
        if (this.fadeInTicks > 0 && this.age < this.fadeInTicks) {
            fadeIn = (float) this.age / (float) this.fadeInTicks;
        }

        float fadeOut = 1.0F;
        if (this.fadeOutTicks > 0) {
            int fadeStart = this.lifetime - this.fadeOutTicks;
            if (this.age >= fadeStart) {
                fadeOut = 1.0F - (float) (this.age - fadeStart) / (float) this.fadeOutTicks;
            }
        }

        return Math.max(0.0F, Math.min(1.0F, fadeIn * fadeOut));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<MendingFadeParticleType.Data> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(MendingFadeParticleType.Data data, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new WispwardFlameTravelParticle(
                    level, x, y, z,
                    data.vx, data.vy, data.vz,
                    data.fadeInTicks, data.fadeOutTicks, data.lifetimeTicks,
                    this.sprites
            );
        }
    }
}
