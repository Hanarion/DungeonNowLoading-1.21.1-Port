package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
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
    private static final float WISPLIGHT_ROD_ARM_X_ROT = -1.2217305F;
    private static final float WISPLIGHT_ROD_OFF_ARM_X_ROT = -0.87266463F;
    private static final float CENTERED_ROD_ARM_Y_ROT = 0.52F;
    private static final float CENTERED_ROD_ARM_Z_ROT = 0.0F;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void applyDNLArmPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player)) {
            return;
        }

        DNLArmPose pose = Services.DATA.getArmPose(player);
        if (pose == DNLArmPose.WISPLIGHT_ROD) {
            this.applyWisplightRodPose(player);
        }
    }

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

    private void applyWisplightRodPose(Player player) {
        HumanoidArm usedArm = this.getUsedArm(player);
        if (usedArm == HumanoidArm.RIGHT) {
            this.rightArm.yRot = -CENTERED_ROD_ARM_Y_ROT + this.head.yRot;
            this.rightArm.xRot = WISPLIGHT_ROD_ARM_X_ROT + this.head.xRot;
            this.rightArm.zRot = CENTERED_ROD_ARM_Z_ROT;

            this.leftArm.xRot = WISPLIGHT_ROD_OFF_ARM_X_ROT + this.head.xRot;
            this.leftArm.yRot = CENTERED_ROD_ARM_Y_ROT + this.head.yRot;
            this.leftArm.zRot = -CENTERED_ROD_ARM_Z_ROT;
        } else {
            this.leftArm.yRot = CENTERED_ROD_ARM_Y_ROT + this.head.yRot;
            this.leftArm.xRot = WISPLIGHT_ROD_ARM_X_ROT + this.head.xRot;
            this.leftArm.zRot = -CENTERED_ROD_ARM_Z_ROT;

            this.rightArm.xRot = WISPLIGHT_ROD_OFF_ARM_X_ROT + this.head.xRot;
            this.rightArm.yRot = -CENTERED_ROD_ARM_Y_ROT + this.head.yRot;
            this.rightArm.zRot = CENTERED_ROD_ARM_Z_ROT;
        }
    }

    private HumanoidArm getUsedArm(Player player) {
        HumanoidArm mainArm = player.getMainArm();
        return player.getUsedItemHand() == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
    }
}
