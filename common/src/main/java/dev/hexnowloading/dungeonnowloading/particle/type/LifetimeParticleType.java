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

public class LifetimeParticleType extends ParticleType<LifetimeParticleType.Data> {
    public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type").forGetter(data -> BuiltInRegistries.PARTICLE_TYPE.getKey(data.particleType).toString()),
            Codec.INT.fieldOf("lifetime").forGetter(data -> data.lifetime)
    ).apply(instance, (type, lifetime) -> new Data((ParticleType<Data>)BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(type)), lifetime)));

    public LifetimeParticleType(boolean alwaysShow) {
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
                return new Data(type, reader.readInt());
            }

            @Override
            public Data fromNetwork(ParticleType<Data> type, FriendlyByteBuf buffer) {
                return new Data(type, buffer.readVarInt());
            }
        };

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

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeVarInt(this.lifetime);
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s %d", BuiltInRegistries.PARTICLE_TYPE.getKey(this.getType()), this.lifetime);
        }

        public int getLifetime() {
            return this.lifetime;
        }
    }
}
