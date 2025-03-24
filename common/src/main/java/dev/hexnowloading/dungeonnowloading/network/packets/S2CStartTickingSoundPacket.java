package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.sound.DNLClientSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class S2CStartTickingSoundPacket implements DNLPacket {

    private final int entityId;
    private final ResourceLocation soundId;
    private final boolean stopOnRecall;

    public S2CStartTickingSoundPacket(int entityId, ResourceLocation soundId, boolean stopOnRecall) {
        this.entityId = entityId;
        this.soundId = soundId;
        this.stopOnRecall = stopOnRecall;
    }

    public S2CStartTickingSoundPacket(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.soundId = buf.readResourceLocation();
        this.stopOnRecall = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeResourceLocation(soundId);
        buf.writeBoolean(stopOnRecall);
    }

    public static S2CStartTickingSoundPacket decode(FriendlyByteBuf buf) {
        return new S2CStartTickingSoundPacket(buf);
    }


    @Override
    public void handle(@Nullable ServerPlayer sender) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (mc.level == null) return;

        Entity entity = level.getEntity(entityId);

        if (entity instanceof LivingEntity livingEntity) {
            DNLClientSoundHandler.playLoopingSound(soundId, livingEntity, stopOnRecall);
        }
    }
}
