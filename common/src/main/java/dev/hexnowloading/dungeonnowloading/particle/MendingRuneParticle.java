package dev.hexnowloading.dungeonnowloading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MendingRuneParticle extends TextureSheetParticle {
    private SpriteSet spriteSet;
    private int runeVarient;
    private double xi;
    private double yi;
    private double zi;
    private final float spinAcceleration;
    private float rotSpeed;
    private final float initialQuadSize;
    private Vec3 initialVelocity;
    private final int REVERSE_DURATION = 40; // Ticks for reversing motion
    private final int GROWTH_DURATION = 3;

    protected MendingRuneParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize *= 0.5F;
        this.hasPhysics = true;
        this.alpha = 0;
        this.spriteSet = spriteSet;
        this.friction = 0.95F;
        this.lifetime = 100;
        this.runeVarient = level.getRandom().nextInt(0, 7);
        this.setSprite(spriteSet.get(this.runeVarient, 7));
        this.xi = xSpeed;
        this.yi = ySpeed;
        this.zi = zSpeed;
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.initialQuadSize = this.quadSize;
        this.quadSize = 0.0F;
        this.initialVelocity = new Vec3(-xSpeed, -ySpeed, -zSpeed);
        this.xd = this.initialVelocity.x * 0.2;
        this.yd = this.initialVelocity.y * 0.2;
        this.zd = this.initialVelocity.z * 0.2;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age < GROWTH_DURATION) {
            float progress = (float) this.age / (float) GROWTH_DURATION; // Progress from 0.0 to 1.0
            this.quadSize = this.initialQuadSize * progress * progress; // Quadratic ease-in growth
        } else {
            float scaleFactor = 1.0f + 0.2f * (float) Math.sin(this.age * 0.2); // Oscillates between 0.8 and 1.2
            this.quadSize = this.initialQuadSize * scaleFactor;
        }
        this.rotSpeed += this.spinAcceleration / 20.0f;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0f;
        this.setSprite(spriteSet.get(this.runeVarient, 7));
        this.setAlpha(1.0F);
        if (this.age++ < this.lifetime) {
            if (this.age < REVERSE_DURATION) {
                float progress = (float) this.age / (float) REVERSE_DURATION; // Transition factor (0 → 1)
                this.xd = lerp(progress, this.initialVelocity.x, 0.0); // Move from reverse to stop
                this.yd = lerp(progress, this.initialVelocity.y, 0.0);
                this.zd = lerp(progress, this.initialVelocity.z, 0.0);
            } else {
                this.xd += xi;
                this.yd += yi;
                this.zd += zi;
            }
            double xt = this.xd;
            double zt = this.zd;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround) {
                this.remove();
                return;
            }
            boolean onNorthWall = (zt != zd && zt < 0.0);
            boolean onSouthWall = (zt != zd && zt > 0.0);
            boolean onWestWall = (xt != xd && xt < 0.0);
            boolean onEastWall = (xt != xd && xt > 0.0);
            if (onNorthWall || onEastWall || onSouthWall || onWestWall) {
                this.remove();
            }
        } else {
            this.remove();
        }
    }

    private double lerp(float t, double start, double end) {
        return start + t * (end - start);
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
            MendingRuneParticle particle = new MendingRuneParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            particle.setSprite(sprites.get(0, 1));
            return particle;
        }
    }
}

