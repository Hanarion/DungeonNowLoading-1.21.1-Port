package dev.hexnowloading.dungeonnowloading.mixin.fabric.block;

import dev.hexnowloading.dungeonnowloading.block.entity.FairkeeperChestBlockEntity;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Tracks Fairkeeper-chest positions when a bucket pickup/place affects the area. 1.21 changed
// BucketItem.use's local-variable table, breaking the old LocalCapture.CAPTURE_FAILHARD injects;
// recompute the affected block via a raycast at TAIL instead (mirror neoforge BucketItemMixin).
@Mixin(BucketItem.class)
public abstract class BucketItemMixin {
    @Inject(method = "Lnet/minecraft/world/item/BucketItem;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
            at = @At("TAIL"))
    private void dnl$trackFairkeeperOnBucketUse(Level level, Player player, InteractionHand hand,
                                                CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide) return;
        if (player.isCreative() || player.isSpectator()) return;

        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return;
        // Empty bucket picks up at the hit block; full bucket places at the relative face.
        BlockPos pickedPos = hit.getBlockPos();
        BlockPos placedPos = hit.getBlockPos().relative(hit.getDirection());

        Optional<List<BlockPos>> blockPosList = Services.DATA.getFairkeeperChestPositionList(player);
        blockPosList.ifPresent(pos -> Services.DATA.copyFairkeeperChestPositionList(player,
                pos.stream()
                        .filter(blockPos -> FairkeeperChestBlockEntity.scanFairkeeperChestPositions(level, blockPos, pickedPos)
                                || FairkeeperChestBlockEntity.scanFairkeeperChestPositions(level, blockPos, placedPos))
                        .collect(Collectors.toList())));
    }
}
