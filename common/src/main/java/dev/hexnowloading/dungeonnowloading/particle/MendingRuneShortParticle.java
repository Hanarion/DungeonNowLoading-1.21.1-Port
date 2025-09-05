package dev.hexnowloading.dungeonnowloading.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

/**
 * Short-lived variant of the Mending Rune particle used for Mendstone Pickaxe highlight.
 */
public class MendingRuneShortParticle extends MendingRuneParticle {

    protected MendingRuneShortParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.lifetime = 40; //override lifetime
    }

    /**
     * I Don't know if it's possible to eliminate this to not have the extra json file, but it's needed to register the particle.
     */
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) { this.sprites = sprites; }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MendingRuneShortParticle p = new MendingRuneShortParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
            p.setSprite(this.sprites.get(0, 1));
            return p;
        }
    }
}

