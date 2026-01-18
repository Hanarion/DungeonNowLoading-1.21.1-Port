package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.GarholdEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow
    public abstract EntityModel<?> getModel();

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;prepareMobModel(Lnet/minecraft/world/entity/Entity;FFF)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void dnl$noSittingWhenRidingGarhold(
            LivingEntity entity, float yaw, float partialTicks,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (entity instanceof AbstractClientPlayer player && (player.getVehicle() instanceof GarholdEntity || player.getVehicle() instanceof BrokenGarholdEntity)) {
            EntityModel<?> m = this.getModel();
            if (m instanceof PlayerModel<?> pm) {
                pm.riding = false;
            }
        }
    }
}
