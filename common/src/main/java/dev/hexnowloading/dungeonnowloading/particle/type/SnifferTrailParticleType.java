package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SnifferTrailParticleType extends ParticleType<SnifferTrailParticleType.Data> {

    private final MapCodec<Data> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("target_x").forGetter(data -> data.targetX),
            Codec.FLOAT.fieldOf("target_y").forGetter(data -> data.targetY),
            Codec.FLOAT.fieldOf("target_z").forGetter(data -> data.targetZ),
            Codec.INT.fieldOf("travel_lifetime").forGetter(data -> data.travelLifetime),
            Codec.INT.fieldOf("delay").forGetter(data -> data.delay)
    ).apply(instance, (targetX, targetY, targetZ, travelLifetime, delay) ->
            new Data(this, targetX, targetY, targetZ, travelLifetime, delay)));

    private final StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec = StreamCodec.composite(
            ByteBufCodecs.FLOAT, data -> data.targetX,
            ByteBufCodecs.FLOAT, data -> data.targetY,
            ByteBufCodecs.FLOAT, data -> data.targetZ,
            ByteBufCodecs.VAR_INT, data -> data.travelLifetime,
            ByteBufCodecs.VAR_INT, data -> data.delay,
            (targetX, targetY, targetZ, travelLifetime, delay) ->
                    new Data(this, targetX, targetY, targetZ, travelLifetime, delay)
    );

    public SnifferTrailParticleType(boolean alwaysShow) {
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
        private final float targetX;
        private final float targetY;
        private final float targetZ;
        private final int travelLifetime;
        private final int delay;

        public Data(ParticleType<Data> particleType, float targetX, float targetY, float targetZ, int travelLifetime, int delay) {
            this.particleType = particleType;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
            this.travelLifetime = travelLifetime;
            this.delay = delay;
        }

        @Override
        public ParticleType<?> getType() {
            return this.particleType;
        }

        public float getTargetX() {
            return this.targetX;
        }

        public float getTargetY() {
            return this.targetY;
        }

        public float getTargetZ() {
            return this.targetZ;
        }

        public int getTravelLifetime() {
            return this.travelLifetime;
        }

        public int getDelay() {
            return this.delay;
        }
    }
}
