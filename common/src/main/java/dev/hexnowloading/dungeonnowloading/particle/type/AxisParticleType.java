package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class AxisParticleType extends ParticleType<AxisParticleType.AxisParticleData> {

    private final MapCodec<AxisParticleData> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("axis").forGetter(data -> data.axis),
            Codec.FLOAT.fieldOf("degree").forGetter(data -> data.degree)
    ).apply(instance, (axis, degree) -> new AxisParticleData(this, axis, degree)));

    private final StreamCodec<RegistryFriendlyByteBuf, AxisParticleData> streamCodec = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, data -> data.axis,
            ByteBufCodecs.FLOAT, data -> data.degree,
            (axis, degree) -> new AxisParticleData(this, axis, degree)
    );

    public AxisParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<AxisParticleData> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, AxisParticleData> streamCodec() {
        return streamCodec;
    }

    public static class AxisParticleData implements ParticleOptions {

        private final ParticleType<AxisParticleData> particleType;
        private final int axis;
        private final float degree;

        public AxisParticleData(ParticleType<AxisParticleData> particleType, int axis, float degree) {
            this.particleType = particleType;
            this.axis = axis;
            this.degree = degree;
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
    }
}
