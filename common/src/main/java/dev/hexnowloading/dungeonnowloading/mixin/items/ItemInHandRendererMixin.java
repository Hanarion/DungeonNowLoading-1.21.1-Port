package dev.hexnowloading.dungeonnowloading.mixin.items;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
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

import java.util.UUID;

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

        if (stack1.getItem() instanceof DNLAnimatedItem<?> animatedItem1 && stack2.getItem() instanceof DNLAnimatedItem<?> animatedItem2) {
            // Ensure UUID exists before comparison
            UUID uuid1 = animatedItem1.getItemUUID(stack1);
            UUID uuid2 = animatedItem2.getItemUUID(stack2);

            // If either stack is missing a UUID, generate one
            if (uuid1 == null) {
                animatedItem1.ensureItemUUID(stack1);
                uuid1 = animatedItem1.getItemUUID(stack1);
            }
            if (uuid2 == null) {
                animatedItem2.ensureItemUUID(stack2);
                uuid2 = animatedItem2.getItemUUID(stack2);
            }

            // Check if both items have the same UUID
            if (uuid1 != null && uuid2 != null && uuid1.equals(uuid2)) {
                return true;
            }
        }
        return false;
    }
}
