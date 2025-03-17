package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class S2CStopItemAnimationPacket implements DNLPacket {

    private final UUID playerUUID;

    public S2CStopItemAnimationPacket(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
    }

    public static S2CStopItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CStopItemAnimationPacket(buffer.readUUID());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = mc.level.getPlayerByUUID(playerUUID);
        if (player == null) return;

        ItemStack itemStack = player.getMainHandItem();
        if (!itemStack.isEmpty()) {
            ItemAnimationState.stopAll(itemStack);
        }
    }
}
