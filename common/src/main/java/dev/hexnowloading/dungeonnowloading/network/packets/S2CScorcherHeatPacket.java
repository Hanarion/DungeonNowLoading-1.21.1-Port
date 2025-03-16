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

    public S2CScorcherHeatPacket(ItemStack itemStack, UUID playerUUID, int slot, float heat) {
        this.itemStack = itemStack;
        this.playerUUID = playerUUID;
        this.slot = slot;
        this.heat = heat;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(itemStack);
        buffer.writeUUID(playerUUID);
        buffer.writeInt(slot);
        buffer.writeFloat(heat);
    }

    public static S2CScorcherHeatPacket decode(FriendlyByteBuf buffer) {
        return new S2CScorcherHeatPacket(buffer.readItem(), buffer.readUUID(), buffer.readInt(), buffer.readFloat());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = mc.level.getPlayerByUUID(playerUUID);
        if (player == null) return;

        ItemStack stack = player.getInventory().items.get(slot);

        if (stack.is(itemStack.getItem())) {
            ScorcherItem.setHeatLevel(stack, heat);
        }
    }
}
