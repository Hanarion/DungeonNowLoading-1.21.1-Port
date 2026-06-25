package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
import dev.hexnowloading.dungeonnowloading.item.MimiclingFormItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow @Final private Minecraft minecraft;

    @Shadow private ItemStack mainHandItem;

    @Shadow private ItemStack offHandItem;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ignoreAnimationNBT(CallbackInfo ci) {

        LocalPlayer localPlayer = this.minecraft.player;

        ItemStack currentMainHand = localPlayer.getMainHandItem();
        ItemStack currentOffHand = localPlayer.getOffhandItem();

        if (itemMatches(mainHandItem, currentMainHand)) {
            mainHandItem = currentMainHand;
        }

        if (itemMatches(offHandItem, currentOffHand)) {
            offHandItem = currentOffHand;
        }
    }

    private boolean itemMatches(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() instanceof WisplightRodItem && stack2.getItem() instanceof WisplightRodItem && ItemStack.isSameItem(stack1, stack2)) {
            return true;
        }

        if (stack1.getItem() instanceof MimiclingFormItem && stack2.getItem() instanceof MimiclingFormItem) {
            return stack1.is(stack2.getItem());
        }

        // DNLAnimatedItems (e.g. the Scorcher) rewrite their CUSTOM_DATA every tick (animation
        // StartTime, heat, fuel). Vanilla ItemInHandRenderer.tick() decides the first-person re-equip
        // ("lowering") animation from a full ItemStack comparison, so that per-tick NBT churn would
        // re-trigger the bob every tick and the model jumps/drops. Treat the same animated item type
        // as "unchanged" so only an actual item swap re-equips. (This renderer only tracks the
        // main/off-hand stacks, so item-type matching is safe — it can't conflate hotbar slots.)
        if (stack1.getItem() instanceof DNLAnimatedItem<?> && stack1.is(stack2.getItem())) {
            return true;
        }
        return false;
    }
}
