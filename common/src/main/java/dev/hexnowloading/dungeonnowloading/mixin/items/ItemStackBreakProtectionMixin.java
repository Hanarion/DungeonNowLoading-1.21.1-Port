package dev.hexnowloading.dungeonnowloading.mixin.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackBreakProtectionMixin {

    // 1.21: the shrink(1) break call lives in different hurtAndBreak overloads per loader:
    //   - NeoForge: hurtAndBreak(int, ServerLevel, LivingEntity, Consumer) holds the real shrink()
    //     (the ServerPlayer overload just delegates to it) -> handled by the NeoForge-only mixin in
    //     neoforge/.../mixin/forge/item/ItemStackNeoForgeBreakProtectionMixin.java.
    //   - Fabric/vanilla: only hurtAndBreak(int, ServerLevel, ServerPlayer, Consumer) exists and
    //     holds the shrink() -> handled by the inject below.
    // The two cannot share one @Inject: the LivingEntity overload doesn't exist on Fabric, and a
    // name+arity match there would descriptor-mismatch against the ServerPlayer method and crash.
    // On NeoForge the inject below finds no shrink() in the delegating ServerPlayer overload, so
    // require = 0 silently skips it and the NeoForge mixin takes over.
    // The shared break logic lives in ItemStackBreakProtectionHandler (kept out of this @Mixin
    // class because Mixin forbids non-private static methods inside a mixin).

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true, require = 0)
    private void dnl$convertToScrapOnBreakServerPlayer(int amount, ServerLevel serverLevel, ServerPlayer player, Consumer<Item> onBroken, CallbackInfo ci) {
        ItemStackBreakProtectionHandler.handleBreak((ItemStack)(Object)this, serverLevel, player, onBroken, ci);
    }
}
