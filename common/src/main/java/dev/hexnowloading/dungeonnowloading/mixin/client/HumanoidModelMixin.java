package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart head;

    @Inject(method = "poseRightArm", at = @At("TAIL"))
    private void modifyRightArmPose(T entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            DNLArmPose pose = Services.DATA.getArmPose(player);

            // Apply transformations based on player's custom arm pose
            switch (pose) {
                case SCORCHER:
                    rightArm.yRot = -0.1F + head.yRot;
                    leftArm.yRot = 0.1F + head.yRot + 0.4F;
                    rightArm.xRot = -1.5707964F + head.xRot;
                    leftArm.xRot = -1.5707964F + head.xRot;
                    break;
                case EMPTY:
                default:
            }
        }
    }

    @Inject(method = "poseLeftArm", at = @At("TAIL"))
    private void modifyLeftArmPose(T entity, CallbackInfo ci) {
        if (entity instanceof Player player) {
            DNLArmPose pose = Services.DATA.getArmPose(player);

            switch (pose) {
                case SCORCHER:
                    this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
                    this.leftArm.yRot = 0.1F + this.head.yRot;
                    this.rightArm.xRot = -1.5707964F + this.head.xRot;
                    this.leftArm.xRot = -1.5707964F + this.head.xRot;
                    break;
                case EMPTY:
                default:
            }
        }
    }
}
