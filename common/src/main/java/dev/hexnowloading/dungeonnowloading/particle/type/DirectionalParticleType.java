package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Generic particle type that carries a direction/velocity vector (vx, vy, vz).
 * Reusable for any particle that needs deterministic velocity over the network.
 */
public class DirectionalParticleType extends ParticleType<DirectionalParticleType.Data> {

    private final MapCodec<Data> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
            Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
            Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz)
    ).apply(instance, (vx, vy, vz) -> new Data(this, vx, vy, vz)));

    private final StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, d -> d.vx,
            ByteBufCodecs.FLOAT, d -> d.vy,
            ByteBufCodecs.FLOAT, d -> d.vz,
            (vx, vy, vz) -> new Data(this, vx, vy, vz)
    );

    public DirectionalParticleType(boolean alwaysShow) {
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

        public final float vx, vy, vz;

        public Data(ParticleType<Data> particleType, float vx, float vy, float vz) {
            this.particleType = particleType;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }

        public static Data of(ParticleType<Data> type, float vx, float vy, float vz) {
            return new Data(type, vx, vy, vz);
        }
    }
}
