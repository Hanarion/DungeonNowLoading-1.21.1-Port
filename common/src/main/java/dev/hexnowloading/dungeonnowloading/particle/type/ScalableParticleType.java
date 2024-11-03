package dev.hexnowloading.dungeonnowloading.particle.type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class ScalableParticleType extends ParticleType<ScalableParticleType.ScalableParticleData> {

    public static final Codec<ScalableParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type").forGetter(data -> BuiltInRegistries.PARTICLE_TYPE.getKey(data.particleType).toString()),
            Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale)
    ).apply(instance, (type, scale) -> new ScalableParticleData((ParticleType<ScalableParticleData>)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(type)), scale)));

    public ScalableParticleType(boolean alwaysShow) {
        super(alwaysShow, ScalableParticleData.DESERIALIZER);
    }

    @Override
    public Codec<ScalableParticleData> codec() {
        return CODEC;
    }

    public static class ScalableParticleData implements ParticleOptions {

        public static final Deserializer<ScalableParticleData> DESERIALIZER = new Deserializer<>() {

            public ScalableParticleData fromCommand(ParticleType<ScalableParticleData> particleType, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                float scale = reader.readFloat();

                return new ScalableParticleData(particleType, scale);
            }

            public ScalableParticleData fromNetwork(ParticleType<ScalableParticleData> particleType, FriendlyByteBuf buffer) {
                return new ScalableParticleData(particleType, buffer.readFloat());
            }
        };

        private final ParticleType<ScalableParticleData> particleType;
        private final float scale;

        public ScalableParticleData(ParticleType<ScalableParticleData> particleType, float scale) {
            this.particleType = particleType;
            this.scale = scale;
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s", BuiltInRegistries.PARTICLE_TYPE.getKey(getType()));
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeFloat(scale);
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }

        public float getScale() {
            return scale;
        }
    }
}
