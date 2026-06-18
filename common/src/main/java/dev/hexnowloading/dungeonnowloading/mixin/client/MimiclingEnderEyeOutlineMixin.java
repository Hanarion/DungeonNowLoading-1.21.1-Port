package dev.hexnowloading.dungeonnowloading.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.item.MimiclingFoodEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LevelRenderer.class)
public class MimiclingEnderEyeOutlineMixin {
    @ModifyArgs(
            method = "renderHitOutline",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderShape(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/phys/shapes/VoxelShape;DDDFFFF)V"
            )
    )
    private void dnl$renderEnderEyeBlockOutline(Args args) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !MimiclingFoodEffects.hasEnderEyeReachEffect(player) || !(minecraft.hitResult instanceof BlockHitResult hitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        double vanillaReach = player.isCreative() ? 5.0D : 4.5D;
        Vec3 eyePosition = player.getEyePosition(1.0F);
        if (eyePosition.distanceToSqr(hitResult.getLocation()) <= vanillaReach * vanillaReach) {
            return;
        }

        args.set(6, 0.15F);
        args.set(7, 0.95F);
        args.set(8, 0.45F);
        args.set(9, 0.75F);
    }
}
