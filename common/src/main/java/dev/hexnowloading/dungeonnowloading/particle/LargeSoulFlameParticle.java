package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.Nullable;

public class LargeSoulFlameParticle extends TextureSheetParticle {

    private final float scale;
    private SpriteSet spriteSet;

    protected LargeSoulFlameParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float scale, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize *= 2.9F + level.random.nextFloat() * 0.5F;
        this.hasPhysics = true;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.spriteSet = spriteSet;
        this.friction = 0.95F;
        this.lifetime = 5 + level.random.nextInt(10);
        this.scale = scale;
    }

    @Override
    public void tick() {
        //this.rCol = (float) (this.lifetime - this.age) / this.lifetime;
        this.quadSize -= Math.min(this.quadSize, 0.05F);
        this.gCol = 1.0F - 0.5F * ((float)this.age / this.lifetime);
        this.rCol = 1.0F - 0.5F * ((float)this.age / this.lifetime);
        //this.gCol = (float) this.age / this.lifetime;
        //this.bCol = (float) this.age / this.lifetime;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        int sprite = this.age % 8;
        this.setSprite(spriteSet.get(sprite, 8));
        if (sprite > 0) {
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= (double) this.friction;
            this.yd *= (double) this.friction;
            this.zd *= (double) this.friction;
        }
    }

    @Override
    public float getQuadSize(float quadSize) {
        return this.quadSize * this.scale;
    }

    @Override
    public int getLightColor(float $$0) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    public static class Factory implements ParticleProvider<ScalableParticleType.ScalableParticleData> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(ScalableParticleType.ScalableParticleData scalableParticleData, ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LargeSoulFlameParticle particle = new LargeSoulFlameParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, scalableParticleData.getScale(), this.sprites);
            particle.setSprite(sprites.get(0, 1));
            return particle;
        }
    }
}
