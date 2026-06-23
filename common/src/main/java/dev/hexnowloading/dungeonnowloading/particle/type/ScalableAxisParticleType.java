package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ScalableAxisParticleType extends ParticleType<ScalableAxisParticleType.ScalableAxisParticleData> {

    private final MapCodec<ScalableAxisParticleData> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("axis").forGetter(data -> data.axis),
            Codec.FLOAT.fieldOf("degree").forGetter(data -> data.degree),
            Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale)
    ).apply(instance, (axis, degree, scale) -> new ScalableAxisParticleData(this, axis, degree, scale)));

    private final StreamCodec<RegistryFriendlyByteBuf, ScalableAxisParticleData> streamCodec = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, data -> data.axis,
            ByteBufCodecs.FLOAT, data -> data.degree,
            ByteBufCodecs.FLOAT, data -> data.scale,
            (axis, degree, scale) -> new ScalableAxisParticleData(this, axis, degree, scale)
    );

    public ScalableAxisParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<ScalableAxisParticleData> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ScalableAxisParticleData> streamCodec() {
        return streamCodec;
    }

    public static class ScalableAxisParticleData implements ParticleOptions {

        private final ParticleType<ScalableAxisParticleData> particleType;
        private final int axis;
        private final float degree;
        private final float scale;

        public ScalableAxisParticleData(ParticleType<ScalableAxisParticleData> particleType, int axis, float degree, float scale) {
            this.particleType = particleType;
            this.axis = axis;
            this.degree = degree;
            this.scale = scale;
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }

        public int getAxis() {
            return axis;
        }

        public float getDegree() {
            return degree;
        }

        public float getScale() {
            return scale;
        }
    }
}
