package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ScalableParticleType extends ParticleType<ScalableParticleType.ScalableParticleData> {

    private final MapCodec<ScalableParticleData> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale)
    ).apply(instance, scale -> new ScalableParticleData(this, scale)));

    private final StreamCodec<RegistryFriendlyByteBuf, ScalableParticleData> streamCodec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, data -> data.scale,
            scale -> new ScalableParticleData(this, scale)
    );

    public ScalableParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<ScalableParticleData> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ScalableParticleData> streamCodec() {
        return streamCodec;
    }

    public static class ScalableParticleData implements ParticleOptions {

        private final ParticleType<ScalableParticleData> particleType;
        private final float scale;

        public ScalableParticleData(ParticleType<ScalableParticleData> particleType, float scale) {
            this.particleType = particleType;
            this.scale = scale;
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }

        public float getScale() {
            return this.scale;
        }
    }
}
