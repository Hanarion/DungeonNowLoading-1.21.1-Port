package dev.hexnowloading.dungeonnowloading.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class VertexSparkParticle extends TextureSheetParticle {

    private SpriteSet spriteSet;
    private float scale;
    private float lifetimeOverride;

    protected VertexSparkParticle(ClientLevel clientLevel, double x, double y, double z, float scale, SpriteSet spriteSet) {
        super(clientLevel, x, y, z);

        float colOffset = DNLMath.randomRange(0.5f, 1.0f);

        this.rCol = colOffset;
        this.gCol = colOffset;
        this.bCol = colOffset;

        this.quadSize = 1.0F;
        this.gravity = 0.0F;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.spriteSet = spriteSet;
        this.lifetime = 3;
        this.scale = scale;
    }
//
    @Override
    public void tick() {
        this.alpha = (this.lifetime - this.age) / this.lifetime;
//        this.alpha = DNLMath.lerpf(this.lifetime)

        if (this.age++ >= this.lifetime) {
            this.remove();
        }


    }
//        this.xo = this.x;
//        this.yo = this.y;
//        this.zo = this.z;
////        int spriteIndex = Mth.clamp(4 * this.age / this.lifetime, 0, 4);
//        this.setSprite(spriteSet.get(0, 1));
////        if (spriteIndex > 0) {
////            this.xd = 0;
////            this.yd = 0;
////            this.zd = 0;
////        }
//        if (this.age++ >= this.lifetime) {
//            this.remove();
//        }
////        } else {
////            this.move(this.xd, this.yd, this.zd);
////        }
//    }
//
//    @Override
//    public void tick() {
//        //this.rCol = (float) (this.lifetime - this.age) / this.lifetime;
//        this.quadSize = Math.min(this.quadSize, 1.0F);
//        this.gCol = 1.0F - 0.9F * ((float)this.age / this.lifetime);
//        this.bCol = 1.0F - 0.9F * ((float)this.age / this.lifetime);
//        //this.gCol = (float) this.age / this.lifetime;
//        //this.bCol = (float) this.age / this.lifetime;
//        this.xo = this.x;
//        this.yo = this.y;
//        this.zo = this.z;
//
//        this.setSprite(spriteSet.get(0, 1));
//        if (this.age++ >= this.lifetime) {
//            this.remove();
//        }
//    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * this.scale;
    }

//    @Override
//    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
//        this.alpha = 1.0F - Mth.clamp((((float)this.age + partialTick) / (float)this.lifetime), 0.0f, 1.0f);
//        this.rCol = 1.0f;
//        this.gCol = 1.0f;
////        this.renderRotatedParticle(vertexConsumer, camera, partialTick, this.axis, this.degree);
//        this.rCol = 0.1f;
//        this.gCol = 0.1f;
////        this.renderRotatedParticle(vertexConsumer, camera, partialTick, this.axis, this.degree + 180F);
//    }

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
            VertexSparkParticle particle = new VertexSparkParticle(clientLevel, x, y, z, scalableParticleData.getScale(), this.sprites);
            particle.setSprite(this.sprites.get(0, 1));
            return particle;
        }
    }
}
