package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class S2CScorcherHeatPacket implements DNLPacket {

    private ItemStack itemStack;
    private final UUID playerUUID;
    private int slot;
    private float heat;
    private long timeStamp;

    public S2CScorcherHeatPacket(ItemStack itemStack, UUID playerUUID, int slot, float heat, long timeStamp) {
        this.itemStack = itemStack;
        this.playerUUID = playerUUID;
        this.slot = slot;
        this.heat = heat;
        this.timeStamp = timeStamp;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(itemStack);
        buffer.writeUUID(playerUUID);
        buffer.writeInt(slot);
        buffer.writeFloat(heat);
        buffer.writeLong(timeStamp);
    }

    public static S2CScorcherHeatPacket decode(FriendlyByteBuf buffer) {
        return new S2CScorcherHeatPacket(buffer.readItem(), buffer.readUUID(), buffer.readInt(), buffer.readFloat(), buffer.readLong());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Player localPlayer = mc.player; // The client’s player
        Player targetPlayer = mc.level.getPlayerByUUID(playerUUID); // The player whose heat level is being updated

        if (targetPlayer == null) return;

        System.out.println("Heat Packet Received for player: " + targetPlayer.getName().getString());

        ItemStack targetItem;

        if (localPlayer.getUUID().equals(playerUUID)) {
            // If the packet is for the local player, update the inventory slot
            targetItem = targetPlayer.getInventory().items.get(slot);
            System.out.println("Updating local inventory slot " + slot);
        } else {
            // If the packet is about another player, update their mainhand/offhand
            if (slot == 40) { // 40 is offhand slot
                targetItem = targetPlayer.getOffhandItem();
                System.out.println("Updating offhand item of " + targetPlayer.getName().getString());
            } else {
                targetItem = targetPlayer.getMainHandItem();
                System.out.println("Updating mainhand item of " + targetPlayer.getName().getString());
            }
        }

        if (!targetItem.isEmpty()) {
            System.out.println("Item Exists: " + targetItem.getDisplayName().getString());

            if (targetItem.is(itemStack.getItem())) {
                System.out.println("Heat Level Updated: " + heat);
                ScorcherItem.setHeatLevel(targetItem, heat, timeStamp);
            }
        }
    }

}
