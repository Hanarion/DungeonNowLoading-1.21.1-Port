package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // 1.21 removed LivingEntity.updatePose(); pose is now updated within aiStep(), so inject there
    // at TAIL to force STANDING while riding a Garhold.
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void dnl_forceStandingWhenRidingGarhold(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self.isPassenger() && self.getVehicle() instanceof GarholdEntity) {
            // Prevent the automatic "sitting" pose while mounted
            self.setPose(Pose.STANDING);
        }
    }
}
