package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow @Nullable private LivingEntity target;

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void dnl$denyCapturedTargets(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player || target instanceof Villager) {
            Entity vehicle = target.getVehicle();
            if (vehicle instanceof GarholdEntity || vehicle instanceof BrokenGarholdEntity) {
                ci.cancel(); // mob keeps/sets no target
                ((Mob)(Object)this).setLastHurtByMob(null);
                ((Mob)(Object)this).setAggressive(false);
            }
        }
    }
}
