package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.item.MimiclingFormItem;
import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.network.packets.C2SMimiclingTransformPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MimiclingPickBlockHandler {
    public static boolean handlePickBlock(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) {
            return false;
        }

        if (!isHoldingMimicling(minecraft.player)) {
            return false;
        }

        ItemStack heldMimicling = getHeldMimicling(minecraft.player);
        String targetForm = getTargetForm(minecraft, heldMimicling);
        if (targetForm != null && canHeldMimiclingTransformTo(minecraft.player, targetForm)) {
            Services.NETWORK.sendToServer(new C2SMimiclingTransformPacket(targetForm));
        }

        return true;
    }

    private static boolean isHoldingMimicling(Player player) {
        return player.getMainHandItem().getItem() instanceof MimiclingFormItem || player.getOffhandItem().getItem() instanceof MimiclingFormItem;
    }

    private static ItemStack getHeldMimicling(Player player) {
        if (player.getMainHandItem().getItem() instanceof MimiclingFormItem) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof MimiclingFormItem) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean canHeldMimiclingTransformTo(Player player, String targetForm) {
        return MimiclingItem.canTransformToForm(player.getMainHandItem(), targetForm) || MimiclingItem.canTransformToForm(player.getOffhandItem(), targetForm);
    }

    private static String getTargetForm(Minecraft minecraft, ItemStack heldMimicling) {
        if (isLookingAtLivingEntity(minecraft)) {
            return MimiclingItem.getBestCombatForm(heldMimicling);
        }

        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
            return MimiclingItem.getBestCombatForm(heldMimicling);
        }

        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            return MimiclingItem.getBestFormFor(heldMimicling, minecraft.level.getBlockState(blockHitResult.getBlockPos()));
        }

        return MimiclingItem.getBaseForm();
    }

    private static boolean isLookingAtLivingEntity(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return false;
        }

        Vec3 eyePosition = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 target = eyePosition.add(look.scale(32.0));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(32.0)).inflate(1.0);

        for (Entity entity : minecraft.level.getEntities(player, searchBox, entity -> entity instanceof LivingEntity && !(entity instanceof Player) && entity.isAlive() && !entity.isSpectator())) {
            AABB hitbox = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> hit = hitbox.clip(eyePosition, target);
            if (hit.isPresent() || hitbox.contains(eyePosition)) {
                return true;
            }
        }

        return false;
    }
}
