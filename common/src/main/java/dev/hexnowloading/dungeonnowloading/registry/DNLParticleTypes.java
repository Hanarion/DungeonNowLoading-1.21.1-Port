package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.particle.type.*;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class DNLParticleTypes {

    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> LARGE_FLAME_PARTICLE = register("large_flame", () -> new ScalableParticleType(false));
    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> LARGE_SOUL_FLAME_PARTICLE = register("large_soul_flame", () -> new ScalableParticleType(false));
    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> MENDING_POP_PARTICLE = register("mending_pop", () -> new ScalableParticleType(false));
    public static final Supplier<SimpleParticleType> SCORCHER_FLAME_PARTICLE = register("scorcher_flame", () -> new SimpleParticleTypeOverride(false));
    public static final Supplier<ParticleType<AxisParticleType.AxisParticleData>> FAIRKEEPER_BOUNDARY_PARTICLE = register("fairkeeper_boundary", () -> new AxisParticleType(false));
    public static final Supplier<ParticleType<AxisParticleType.AxisParticleData>> VERTEX_BOUNDARY_PARTICLE = register("vertex_boundary", () -> new AxisParticleType(false));
    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> REDSTONE_SHOCKWAVE_PARTICLE = register("redstone_shockwave", () -> new ScalableParticleType(false));
    public static final Supplier<ParticleType<ScalableAxisParticleType.ScalableAxisParticleData>> REDSTONE_HAZARD_INDICATOR_PARTICLE = register("redstone_hazard_indicator", () -> new ScalableAxisParticleType(false));
    public static final Supplier<ParticleType<ScalableAxisParticleType.ScalableAxisParticleData>> WHITE_SHOCKWAVE_PARTICLE = register("white_shockwave", () -> new ScalableAxisParticleType(true));
    public static final Supplier<ParticleType<ScalableAxisParticleType.ScalableAxisParticleData>> WHITE_SHOCKWAVE_MEDIUM_PARTICLE = register("white_shockwave_medium", () -> new ScalableAxisParticleType(true));
    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> ARROW_HAZARD_INDICATOR = register("arrow_hazard_indicator", () -> new ScalableParticleType(false));
    public static final Supplier<SimpleParticleType> MENDING_POP_AND_RUNE_PARTICLE = register("mending_pop_and_rune", () -> new SimpleParticleTypeOverride(false));
    public static final Supplier<SimpleParticleType> MENDING_RUNE_PARTICLE = register("mending_rune", () -> new SimpleParticleTypeOverride(false));
    public static final Supplier<SimpleParticleType> MENDING_RUNE_SHORT_PARTICLE = register("mending_rune_short", () -> new SimpleParticleTypeOverride(false));
    public static final Supplier<MendingFadeParticleType> MENDING_FADE_PARTICLE = register("mending_fade", () -> new MendingFadeParticleType(false));
    public static final Supplier<ParticleType<SnifferTrailParticleType.Data>> SNIFFER_TRAIL_PARTICLE = register("sniffer_trail", () -> new SnifferTrailParticleType(false));
    public static final Supplier<ParticleType<BlockParticleOption>> MIMICLING_IMPACT_BLOCK_PARTICLE = register("mimicling_impact_block", () -> new ParticleType<BlockParticleOption>(false, BlockParticleOption.DESERIALIZER) {
        @Override
        public com.mojang.serialization.Codec<BlockParticleOption> codec() {
            return BlockParticleOption.codec(this);
        }
    });


    public static final Supplier<ParticleType<ScalableParticleType.ScalableParticleData>> VERTEX_SPARK_PARTICLE = register("vertex_spark", () -> new ScalableParticleType(false));

    private static <T extends ParticleType<?>> Supplier<T> register(String name, Supplier<T> particleTypeSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.PARTICLE_TYPE, name, particleTypeSupplier);
    }

    public static void init() {}
}
