package dev.hexnowloading.dungeonnowloading.mixin.inventory;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$temporarilyOpenMimiclingForInventoryFeed(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (clickType != ClickType.PICKUP || button != 1 || slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return;
        }

        Slot slot = menu.slots.get(slotIndex);
        if (MimiclingItem.tryTemporarilyOpenForInventoryFeed(slot.getItem(), slot, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "clicked", at = @At("TAIL"))
    private void dungeonnowloading$temporarilyOpenMimiclingsForFeedableCursor(int slotIndex, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!MimiclingItem.isFeedableTool(menu.getCarried())) {
            return;
        }

        for (Slot slot : menu.slots) {
            MimiclingItem.tryTemporarilyOpenForInventoryFeed(slot.getItem(), slot, player);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void dungeonnowloading$restoreTemporaryMimiclingForm(Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        long gameTime = player.level().getGameTime();

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            ItemStack restored = MimiclingItem.restoreTemporaryInventoryForm(stack, gameTime);
            if (restored != stack) {
                slot.set(restored);
                slot.setChanged();
            }
        }

        ItemStack carried = menu.getCarried();
        ItemStack restoredCarried = MimiclingItem.restoreTemporaryInventoryForm(carried, gameTime);
        if (restoredCarried != carried) {
            menu.setCarried(restoredCarried);
        }
    }
}
