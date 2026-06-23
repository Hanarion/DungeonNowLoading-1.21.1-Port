package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LifetimeParticleType extends ParticleType<LifetimeParticleType.Data> {

    private final MapCodec<Data> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("lifetime").forGetter(data -> data.lifetime)
    ).apply(instance, lifetime -> new Data(this, lifetime)));

    private final StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, data -> data.lifetime,
            lifetime -> new Data(this, lifetime)
    );

    public LifetimeParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    @Override
    public MapCodec<Data> codec() {
        return codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, Data> streamCodec() {
        return streamCodec;
    }

    public static class Data implements ParticleOptions {

        private final ParticleType<Data> particleType;
        private final int lifetime;

        public Data(ParticleType<Data> particleType, int lifetime) {
            this.particleType = particleType;
            this.lifetime = lifetime;
        }

        @Override
        public ParticleType<?> getType() {
            return this.particleType;
        }

        public int getLifetime() {
            return this.lifetime;
        }
    }
}
