package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.DNLAnimatedItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class S2CStartItemAnimationPacket implements DNLPacket {
    private final UUID playerUUID;
    private final ItemStack itemStack;
    private final int slot;
    private final String animationName;
    private final long duration;
    private final boolean loop;
    private final boolean resetAnimations;

    public S2CStartItemAnimationPacket(UUID playerId, ItemStack itemStack, int slot, String animationName, long duration, boolean loop, boolean resetAnimations) {
        this.playerUUID = playerId;
        this.itemStack = itemStack;
        this.slot = slot;
        this.animationName = animationName;
        this.duration = duration;
        this.loop = loop;
        this.resetAnimations = resetAnimations;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeItem(itemStack);
        buffer.writeInt(slot);
        buffer.writeUtf(animationName);
        buffer.writeLong(duration);
        buffer.writeBoolean(loop);
        buffer.writeBoolean(resetAnimations);
    }

    public static S2CStartItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStartItemAnimationPacket(buffer.readUUID(), buffer.readItem(), buffer.readInt(), buffer.readUtf(), buffer.readLong(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Player localPlayer = mc.player; // The client’s player
        Player targetPlayer = mc.level.getPlayerByUUID(playerUUID); // The player the packet is about

        if (targetPlayer == null) return;

        System.out.println("Packet Received");

        ItemStack targetItem;

        if (localPlayer.getUUID().equals(playerUUID)) {
            // If the packet is for the local player, get from inventory slot
            targetItem = targetPlayer.getInventory().items.get(slot);
            System.out.println("Checking local inventory slot " + slot);
        } else {
            // If the packet is about another player, get their mainhand/offhand
            if (slot == 40) { // 40 is offhand slot
                targetItem = targetPlayer.getOffhandItem();
                System.out.println("Checking offhand item of " + targetPlayer.getName().getString());
            } else {
                targetItem = targetPlayer.getMainHandItem();
                System.out.println("Checking mainhand item of " + targetPlayer.getName().getString());
            }
        }

        if (!targetItem.isEmpty()) {
            System.out.println("Item Exists");

            if (targetItem.getItem() instanceof DNLAnimatedItem<?> animatedTargetItem &&
                    itemStack.getItem() instanceof DNLAnimatedItem<?> animatedItem) {
                System.out.println("Animated Item");

                UUID uuid1 = animatedTargetItem.getItemUUID(targetItem);
                UUID uuid2 = animatedItem.getItemUUID(itemStack);

                if (uuid1 != null && uuid1.equals(uuid2)) {
                    System.out.println("UUID Matches");
                    ItemAnimationState.start(targetItem, animationName, mc.level.getGameTime(), duration, loop, resetAnimations);
                }
            }
        }
    }
}
