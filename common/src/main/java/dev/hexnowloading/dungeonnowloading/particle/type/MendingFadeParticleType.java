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

public class MendingFadeParticleType extends ParticleType<MendingFadeParticleType.Data> {

    public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type")
                    .forGetter(d -> BuiltInRegistries.PARTICLE_TYPE.getKey(d.particleType).toString()),
            Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
            Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
            Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz),
            Codec.INT.fieldOf("fade_in").forGetter(d -> d.fadeInTicks),
            Codec.INT.fieldOf("fade_out").forGetter(d -> d.fadeOutTicks),
            Codec.INT.fieldOf("lifetime").forGetter(d -> d.lifetimeTicks)
    ).apply(instance, (type, vx, vy, vz, fadeIn, fadeOut, lifetime) ->
            new Data(
                    (ParticleType<Data>) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(type)),
                    vx, vy, vz,
                    fadeIn, fadeOut, lifetime
            )
    ));

    public MendingFadeParticleType(boolean alwaysShow) {
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
                float vx = reader.readFloat();
                reader.expect(' ');
                float vy = reader.readFloat();
                reader.expect(' ');
                float vz = reader.readFloat();
                reader.expect(' ');
                int fadeIn = reader.readInt();
                reader.expect(' ');
                int fadeOut = reader.readInt();
                reader.expect(' ');
                int lifetime = reader.readInt();

                return new Data(type, vx, vy, vz, fadeIn, fadeOut, lifetime);
            }

            @Override
            public Data fromNetwork(ParticleType<Data> type, FriendlyByteBuf buf) {
                return new Data(
                        type,
                        buf.readFloat(), buf.readFloat(), buf.readFloat(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt()
                );
            }
        };

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

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeFloat(vx);
            buf.writeFloat(vy);
            buf.writeFloat(vz);
            buf.writeVarInt(fadeInTicks);
            buf.writeVarInt(fadeOutTicks);
            buf.writeVarInt(lifetimeTicks);
        }

        @Override
        public String writeToString() {
            // command string: <id> vx vy vz fadeIn fadeOut lifetime
            ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(getType());
            return String.format(Locale.ROOT, "%s %.3f %.3f %.3f %d %d %d",
                    id, vx, vy, vz, fadeInTicks, fadeOutTicks, lifetimeTicks);
        }
    }
}
