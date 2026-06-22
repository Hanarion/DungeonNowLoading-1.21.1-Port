package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class SnifferTrailParticleType extends ParticleType<SnifferTrailParticleType.Data> {
    public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type").forGetter(data -> BuiltInRegistries.PARTICLE_TYPE.getKey(data.particleType).toString()),
            Codec.FLOAT.fieldOf("target_x").forGetter(data -> data.targetX),
            Codec.FLOAT.fieldOf("target_y").forGetter(data -> data.targetY),
            Codec.FLOAT.fieldOf("target_z").forGetter(data -> data.targetZ),
            Codec.INT.fieldOf("travel_lifetime").forGetter(data -> data.travelLifetime),
            Codec.INT.fieldOf("delay").forGetter(data -> data.delay)
    ).apply(instance, (type, targetX, targetY, targetZ, travelLifetime, delay) -> new Data((ParticleType<Data>)BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(type)), targetX, targetY, targetZ, travelLifetime, delay)));

    public SnifferTrailParticleType(boolean alwaysShow) {
        super(alwaysShow, Data.DESERIALIZER);
    }

    @Override
    public Codec<Data> codec() {
        return CODEC;
    }

    public static class Data implements ParticleOptions {
        public static final Deserializer<Data> DESERIALIZER = new Deserializer<>() {
            @Override
            public Data fromCommand(ParticleType<Data> type, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float targetX = reader.readFloat();
                reader.expect(' ');
                float targetY = reader.readFloat();
                reader.expect(' ');
                float targetZ = reader.readFloat();
                reader.expect(' ');
                int travelLifetime = reader.readInt();
                reader.expect(' ');
                int delay = reader.readInt();
                return new Data(type, targetX, targetY, targetZ, travelLifetime, delay);
            }

            @Override
            public Data fromNetwork(ParticleType<Data> type, FriendlyByteBuf buffer) {
                return new Data(type, buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readVarInt(), buffer.readVarInt());
            }
        };

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

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeFloat(this.targetX);
            buffer.writeFloat(this.targetY);
            buffer.writeFloat(this.targetZ);
            buffer.writeVarInt(this.travelLifetime);
            buffer.writeVarInt(this.delay);
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %.3f %.3f %.3f %d %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.targetX, this.targetY, this.targetZ, this.travelLifetime, this.delay);
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
