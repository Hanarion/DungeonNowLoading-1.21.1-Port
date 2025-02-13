package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.particle.type.AxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableAxisParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.ScalableParticleType;
import dev.hexnowloading.dungeonnowloading.particle.type.SimpleParticleTypeOverride;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class DNLParticleTypes {

    public static final Supplier<SimpleParticleType> LARGE_FLAME_PARTICLE = register("large_flame", () -> new SimpleParticleTypeOverride(false));
    public static final Supplier<ParticleType<AxisParticleType.AxisParticleData>> FAIRKEEPER_BOUNDARY_PARTICLE = register("fairkeeper_boundary", () -> new AxisParticleType(false));
    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> REDSTONE_SHOCKWAVE_PARTICLE = register("redstone_shockwave", () -> new ScalableParticleType(false));
    public static final Supplier<ParticleType<ScalableAxisParticleType.ScalableAxisParticleData>> REDSTONE_HAZARD_INDICATOR_PARTICLE = register("redstone_hazard_indicator", () -> new ScalableAxisParticleType(false));
    public static final Supplier<ParticleType<ScalableAxisParticleType.ScalableAxisParticleData>> WHITE_SHOCKWAVE_PARTICLE = register("white_shockwave", () -> new ScalableAxisParticleType(false));

    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> VERTEX_SPARK_PARTICLE = register("vertex_spark", () -> new ScalableParticleType(false));

    private static <T extends ParticleType<?>> Supplier<T> register(String name, Supplier<T> particleTypeSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.PARTICLE_TYPE, name, particleTypeSupplier);
    }

    public static void init() {}
}
