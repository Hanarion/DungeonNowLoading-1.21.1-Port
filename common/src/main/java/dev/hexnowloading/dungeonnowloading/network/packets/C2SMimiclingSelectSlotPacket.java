package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class C2SMimiclingSelectSlotPacket implements DNLPacket {
    private final int containerId;
    private final int slotIndex;
    private final int delta;

    public C2SMimiclingSelectSlotPacket(int containerId, int slotIndex, int delta) {
        this.containerId = containerId;
        this.slotIndex = slotIndex;
        this.delta = delta;
    }

    public C2SMimiclingSelectSlotPacket(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.slotIndex = buf.readVarInt();
        this.delta = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slotIndex);
        buf.writeVarInt(delta);
    }

    public static C2SMimiclingSelectSlotPacket decode(FriendlyByteBuf buf) {
        return new C2SMimiclingSelectSlotPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || sender.containerMenu.containerId != containerId || slotIndex < 0 || slotIndex >= sender.containerMenu.slots.size()) {
            return;
        }

        Slot slot = sender.containerMenu.slots.get(slotIndex);
        ItemStack stack = slot.getItem();
        boolean changed = delta == 0
                ? selectHoveredSlot(stack, sender.containerMenu.getCarried())
                : MimiclingItem.tryScrollSelectedSlot(stack, delta);
        if (changed) {
            slot.setChanged();
        }
    }

    private boolean selectHoveredSlot(ItemStack stack, ItemStack carriedStack) {
        if (carriedStack.isEmpty()) {
            return MimiclingItem.trySelectNextOccupiedSlotIfSelectedEmpty(stack);
        }

        return MimiclingItem.trySelectDedicatedSlot(stack, carriedStack);
    }
}
