package dev.hexnowloading.dungeonnowloading.mixin.forge.item;

import dev.hexnowloading.dungeonnowloading.mixin.items.ItemStackBreakProtectionHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

/**
 * NeoForge-only: target the hurtAndBreak(int, ServerLevel, LivingEntity, Consumer) overload, which
 * is the one that actually holds the shrink() break call on NeoForge (the ServerPlayer overload just
 * delegates here). This overload does NOT exist on Fabric/vanilla, so this mixin is registered only
 * in the NeoForge mixin config. The shared break logic lives in ItemStackBreakProtectionMixin.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackNeoForgeBreakProtectionMixin {

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true, require = 1)
    private void dnl$convertToScrapOnBreakLivingEntity(int amount, ServerLevel serverLevel, LivingEntity entity, Consumer<Item> onBroken, CallbackInfo ci) {
        ItemStackBreakProtectionHandler.handleBreak((ItemStack)(Object)this, serverLevel, entity instanceof ServerPlayer sp ? sp : null, onBroken, ci);
    }
}
