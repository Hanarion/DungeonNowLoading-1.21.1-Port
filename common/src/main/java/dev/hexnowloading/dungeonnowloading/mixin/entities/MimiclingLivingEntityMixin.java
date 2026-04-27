package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MimiclingLivingEntityMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void dnl$spawnMimiclingDeathEffectCloud(DamageSource damageSource, CallbackInfo ci) {
        MimiclingFoodEffects.onMobDeath((LivingEntity)(Object)this, damageSource.getEntity());
    }
}
