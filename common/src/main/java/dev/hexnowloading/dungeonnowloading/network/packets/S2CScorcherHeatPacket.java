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

    private UUID itemUUID;
    private final UUID playerUUID;
    private float heat;

    public S2CScorcherHeatPacket(UUID itemStack, UUID playerUUID, float heat) {
        this.itemUUID = itemStack;
        this.playerUUID = playerUUID;
        this.heat = heat;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(itemUUID);
        buffer.writeUUID(playerUUID);
        buffer.writeFloat(heat);
    }

    public static S2CScorcherHeatPacket decode(FriendlyByteBuf buffer) {
        return new S2CScorcherHeatPacket(buffer.readUUID(), buffer.readUUID(), buffer.readFloat());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = mc.level.getPlayerByUUID(playerUUID);
        if (player == null) return;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.hasTag() && stack.getTag().contains("ScorcherUUID")) {
                UUID stackUUID = stack.getTag().getUUID("ScorcherUUID");
                if (stackUUID.equals(this.itemUUID)) {
                    ScorcherItem.setHeatLevel(stack, heat);
                    return; // ✅ Stop once the correct Scorcher is found
                }
            }
        }
    }
}
