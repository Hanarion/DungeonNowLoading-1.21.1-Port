package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class MendingFadeParticleType extends ParticleType<MendingFadeParticleType.Data> {

    private final MapCodec<Data> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
            Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
            Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz),
            Codec.INT.fieldOf("fade_in").forGetter(d -> d.fadeInTicks),
            Codec.INT.fieldOf("fade_out").forGetter(d -> d.fadeOutTicks),
            Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetimeTicks)
    ).apply(instance, (vx, vy, vz, fadeIn, fadeOut, lifetime) -> new Data(this, vx, vy, vz, fadeIn, fadeOut, lifetime)));

    private final StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, d -> d.vx,
            ByteBufCodecs.FLOAT, d -> d.vy,
            ByteBufCodecs.FLOAT, d -> d.vz,
            ByteBufCodecs.VAR_INT, d -> d.fadeInTicks,
            ByteBufCodecs.VAR_INT, d -> d.fadeOutTicks,
            ByteBufCodecs.VAR_INT, d -> d.lifetimeTicks,
            (vx, vy, vz, fadeIn, fadeOut, lifetime) -> new Data(this, vx, vy, vz, fadeIn, fadeOut, lifetime)
    );

    public MendingFadeParticleType(boolean alwaysShow) {
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
        public final int fadeInTicks;
        public final int fadeOutTicks;
        public final int lifetimeTicks;

        public Data(ParticleType<Data> particleType,
                    float vx, float vy, float vz,
                    int fadeInTicks, int fadeOutTicks, int lifetimeTicks) {
            this.particleType = particleType;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
            this.fadeInTicks = fadeInTicks;
            this.fadeOutTicks = fadeOutTicks;
            this.lifetimeTicks = lifetimeTicks;
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }
    }
}
