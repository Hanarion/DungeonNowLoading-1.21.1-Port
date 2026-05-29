package dev.hexnowloading.dungeonnowloading.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void injectArmPose(AbstractClientPlayer player, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (player.isUsingItem()) {
            ItemStack itemStack = player.getUseItem();
            if (itemStack.getItem() instanceof ScorcherItem) {
                Services.DATA.setArmPose(player, DNLArmPose.SCORCHER);
            } else if (itemStack.getItem() instanceof WisplightRodItem) {
                Services.DATA.setArmPose(player, DNLArmPose.WISPLIGHT_ROD);
            } else {
                Services.DATA.setArmPose(player, DNLArmPose.EMPTY);
            }
        } else {
            Services.DATA.setArmPose(player, DNLArmPose.EMPTY);
        }
    }
}
