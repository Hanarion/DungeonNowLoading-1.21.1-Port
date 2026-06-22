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

/**
 * Generic particle type that carries a direction/velocity vector (vx, vy, vz).
 * Reusable for any particle that needs deterministic velocity over the network.
 */
public class DirectionalParticleType extends ParticleType<DirectionalParticleType.Data> {

    public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type")
                    .forGetter(d -> BuiltInRegistries.PARTICLE_TYPE.getKey(d.particleType).toString()),
            Codec.FLOAT.fieldOf("vx").forGetter(d -> d.vx),
            Codec.FLOAT.fieldOf("vy").forGetter(d -> d.vy),
            Codec.FLOAT.fieldOf("vz").forGetter(d -> d.vz)
    ).apply(instance, (type, vx, vy, vz) ->
            new Data(
                    (ParticleType<Data>) BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(type)),
                    vx, vy, vz
            )
    ));

    public DirectionalParticleType(boolean alwaysShow) {
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
                // /particle <id> x y z vx vy vz
                reader.expect(' ');
                float vx = reader.readFloat();
                reader.expect(' ');
                float vy = reader.readFloat();
                reader.expect(' ');
                float vz = reader.readFloat();
                return new Data(type, vx, vy, vz);
            }

            @Override
            public Data fromNetwork(ParticleType<Data> type, FriendlyByteBuf buf) {
                return new Data(type, buf.readFloat(), buf.readFloat(), buf.readFloat());
            }
        };

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

        @Override
        public void writeToNetwork(FriendlyByteBuf buf) {
            buf.writeFloat(vx);
            buf.writeFloat(vy);
            buf.writeFloat(vz);
        }

        @Override
        public String writeToString() {
            ResourceLocation id = BuiltInRegistries.PARTICLE_TYPE.getKey(getType());
            return String.format(Locale.ROOT, "%s %.3f %.3f %.3f", id, vx, vy, vz);
        }

        public static Data of(ParticleType<Data> type, float vx, float vy, float vz) {
            return new Data(type, vx, vy, vz);
        }
    }
}
