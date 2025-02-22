package dev.hexnowloading.dungeonnowloading.particle;

import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class MendingPopParticle extends TextureSheetParticle {
    private SpriteSet spriteSet;

    protected MendingPopParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.quadSize *= 1.5F;
        this.hasPhysics = true;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.bCol = 1.0F;
        this.gCol = 1.0F;
        this.spriteSet = spriteSet;
        this.friction = 0.95F;
        this.lifetime = 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age % 2 == 0) {
            int sprite = (this.age / 2) % 8;
            this.setSprite(spriteSet.get(sprite, 8));
        }

        if (this.age++ >= this.lifetime) {
            level.addParticle(DNLParticleTypes.MENDING_RUNE_PARITCLE.get(), this.x, this.y, this.z, this.xd, this.yd, this.zd);
            this.remove();
        }
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
            MendingPopParticle particle = new MendingPopParticle(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            particle.setSprite(sprites.get(0, 1));
            return particle;
        }
    }
}
