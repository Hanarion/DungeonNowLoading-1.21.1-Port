package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.potion.VertexTransmissionEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

public class DNLMobEffects {

    public static final Supplier<MobEffect> VERTEX_TRANSMISSION = registerEffects("vertex_transmission", VertexTransmissionEffect::new);

    public static <T extends MobEffect> Supplier<T> registerEffects(String name, Supplier<T> effectSupplier) {
        return Services.REGISTRY.register(BuiltInRegistries.MOB_EFFECT, name, effectSupplier);
    }

    /** 1.21 APIs key mob effects by Holder; wrap a registered effect for use with addEffect/removeEffect/etc. */
    public static Holder<MobEffect> holder(Supplier<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get());
    }

    public static Holder<MobEffect> vertexTransmission() {
        return holder(VERTEX_TRANSMISSION);
    }

    public static void init() {
    }
}
