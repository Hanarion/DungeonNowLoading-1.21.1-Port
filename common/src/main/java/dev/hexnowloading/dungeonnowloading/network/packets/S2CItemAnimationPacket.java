package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class S2CItemAnimationPacket implements DNLPacket {
    private final UUID playerUUID;
    private final String animationName;
    private final long duration;
    private final boolean loop;
    private final boolean resetAnimations;

    public S2CItemAnimationPacket(UUID playerId, String animationName, long duration, boolean loop, boolean resetAnimations) {
        this.playerUUID = playerId;
        this.animationName = animationName;
        this.duration = duration;
        this.loop = loop;
        this.resetAnimations = resetAnimations;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeUtf(animationName);
        buffer.writeLong(duration);
        buffer.writeBoolean(loop);
        buffer.writeBoolean(resetAnimations);
    }

    public static S2CItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new S2CItemAnimationPacket(buffer.readUUID(), buffer.readUtf(), buffer.readLong(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player player = (Player) mc.level.getPlayerByUUID(playerUUID);
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty()) {
            ItemAnimationState.start(stack, animationName, mc.level.getGameTime(), duration, loop, resetAnimations);
        }
    }
}
