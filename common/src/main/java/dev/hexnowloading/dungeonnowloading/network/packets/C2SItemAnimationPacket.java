package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class C2SItemAnimationPacket implements DNLPacket {
    private final UUID playerUUID;
    private final String animationName;
    private final long duration;
    private final boolean loop;
    private final boolean resetAnimations;

    public C2SItemAnimationPacket(UUID playerUUID, String animationName, long duration, boolean loop, boolean resetAnimations) {
        this.playerUUID = playerUUID;
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

    public static C2SItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new C2SItemAnimationPacket(buffer.readUUID(), buffer.readUtf(), buffer.readLong(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (sender == null) return; // Safety check

        ServerLevel level = sender.serverLevel();
        if (level == null) return; // Make sure the server level is not null

        Player player = level.getPlayerByUUID(playerUUID);
        if (player == null) return; // Ensure player exists

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) return; // No item, no animation

        //ItemAnimationState.start(stack, animationName, level.getGameTime(), duration, loop, resetAnimations);

        // ✅ Send update to all players NEARBY in the same dimension
        Services.NETWORK.sendToAllPlayers(new S2CItemAnimationPacket(player.getUUID(), animationName, duration, loop, resetAnimations), sender.getServer());
    }
}
