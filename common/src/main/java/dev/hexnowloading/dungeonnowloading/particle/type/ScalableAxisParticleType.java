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

public class ScalableAxisParticleType extends ParticleType<ScalableAxisParticleType.ScalableAxisParticleData> {
    public static final Codec<ScalableAxisParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("particle_type").forGetter(data -> BuiltInRegistries.PARTICLE_TYPE.getKey(data.particleType).toString()),
            Codec.INT.fieldOf("axis").forGetter(data -> data.axis),
            Codec.FLOAT.fieldOf("degree").forGetter(data -> data.degree),
            Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale)
    ).apply(instance, (type, axis, degree, scale) -> new ScalableAxisParticleData((ParticleType<ScalableAxisParticleData>)BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(type)), axis, degree, scale)));

    public ScalableAxisParticleType(boolean alwaysShow) { super(alwaysShow, ScalableAxisParticleData.DESERIALIZER); }

    @Override
    public Codec<ScalableAxisParticleData> codec() {
        return CODEC;
    }

    public static class ScalableAxisParticleData implements ParticleOptions {
        public static final Deserializer<ScalableAxisParticleData> DESERIALIZER = new Deserializer<>() {

            @Override
            public ScalableAxisParticleData fromCommand(ParticleType<ScalableAxisParticleData> particleType, StringReader reader) throws CommandSyntaxException {
                reader.expect(' ');
                int axis = reader.readInt();
                reader.expect(' ');
                float degree = reader.readFloat();
                reader.expect(' ');
                float scale = reader.readFloat();

                return new ScalableAxisParticleData(particleType, axis, degree, scale);
            }

            @Override
            public ScalableAxisParticleData fromNetwork(ParticleType<ScalableAxisParticleData> particleType, FriendlyByteBuf buffer) {
                return new ScalableAxisParticleData(particleType, buffer.readInt(), buffer.readFloat(), buffer.readFloat());
            }
        };

        private final ParticleType<ScalableAxisParticleData> particleType;
        private final int axis;
        private final float degree;
        private final float scale;

        public ScalableAxisParticleData(ParticleType<ScalableAxisParticleData> particleType, int axis, float degree, float scale) {
            this.particleType = particleType;
            this.axis = axis;
            this.degree = degree;
            this.scale = scale;
        }

        @Override
        public String writeToString() {
            return String.format(Locale.ROOT, "%s", BuiltInRegistries.PARTICLE_TYPE.getKey(getType()));
        }

        @Override
        public void writeToNetwork(FriendlyByteBuf buffer) {
            buffer.writeInt(axis);
            buffer.writeFloat(degree);
            buffer.writeFloat(scale);
        }

        @Override
        public ParticleType<?> getType() {
            return particleType;
        }

        public int getAxis() {
            return axis;
        }

        public float getDegree() {
            return degree;
        }

        public float getScale() { return scale; }
    }
}
