package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClientBoundItemAnimationPacket implements DNLPacket {

    private final UUID playerUUID;
    private final String animationName;

    public ClientBoundItemAnimationPacket(UUID playerUUID, String animationName) {
        this.playerUUID = playerUUID;
        this.animationName = animationName;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeUtf(animationName);
    }

    public static ClientBoundItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new ClientBoundItemAnimationPacket(buffer.readUUID(), buffer.readUtf());
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        /*Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = mc.level.getPlayerByUUID(playerUUID);
        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof DNLAnimatedItem<?> animatedItem) {
                System.out.println("🎥 Received animation packet: " + animationName + " for " + player.getName().getString());
                animatedItem.setAnimationState(stack, playerUUID, animationName);
            }
        }*/
    }
}