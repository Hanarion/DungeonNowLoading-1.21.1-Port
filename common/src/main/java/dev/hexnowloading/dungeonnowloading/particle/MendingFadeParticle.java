package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.Nullable;

public class MendingFadeParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final int runeVariant;

    private final int fadeInTicks;
    private final int fadeOutTicks;

    protected MendingFadeParticle(ClientLevel level,
                                  double x, double y, double z,
                                  float vx, float vy, float vz,
                                  int fadeInTicks, int fadeOutTicks, int lifetimeTicks,
                                  SpriteSet spriteSet) {
        super(level, x, y, z, 0, 0, 0);

        this.spriteSet = spriteSet;
        this.runeVariant = level.getRandom().nextInt(0, 7);
        this.setSprite(spriteSet.get(this.runeVariant, 7));

        this.fadeInTicks = Math.max(0, fadeInTicks);
        this.fadeOutTicks = Math.max(0, fadeOutTicks);
        this.lifetime = Math.max(1, lifetimeTicks);

        this.alpha = 0.0F;
        this.quadSize *= 0.5F;

        this.hasPhysics = false;
        this.friction = 0.95F;

        // Velocity from data
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        // keep the same rune sprite (or animate if you want)
        this.setSprite(spriteSet.get(this.runeVariant, 7));

        // Fade
        this.alpha = computeAlpha();

        // Move
        this.move(this.xd, this.yd, this.zd);

        // Optional: kill on ground/hit
        if (this.onGround) {
            this.remove();
            return;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    private float computeAlpha() {
        float aIn = 1.0F;
        if (fadeInTicks > 0 && this.age < fadeInTicks) {
            aIn = (float) this.age / (float) fadeInTicks;
        }

        float aOut = 1.0F;
        if (fadeOutTicks > 0) {
            int fadeStart = this.lifetime - this.fadeOutTicks;
            if (this.age >= fadeStart) {
                int t = this.age - fadeStart; // 0..fadeOut
                aOut = 1.0F - ((float) t / (float) this.fadeOutTicks);
            }
        }

        float a = aIn * aOut;
        if (a < 0) a = 0;
        if (a > 1) a = 1;
        return a;
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

            // NOTE: xSpeed/ySpeed/zSpeed are ignored; we use data.vx/vy/vz
            return new MendingFadeParticle(
                    level, x, y, z,
                    data.vx, data.vy, data.vz,
                    data.fadeInTicks, data.fadeOutTicks, data.lifetimeTicks,
                    sprites
            );
        }
    }
}
