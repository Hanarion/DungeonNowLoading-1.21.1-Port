package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.item.MimiclingItem;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class C2SMimiclingTransformPacket implements DNLPacket {
    private final String targetForm;

    public C2SMimiclingTransformPacket(String targetForm) {
        this.targetForm = targetForm;
    }

    public C2SMimiclingTransformPacket(FriendlyByteBuf buf) {
        this.targetForm = buf.readUtf(32);
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetForm, 32);
    }

    public static C2SMimiclingTransformPacket decode(FriendlyByteBuf buf) {
        return new C2SMimiclingTransformPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || !MimiclingItem.isValidForm(targetForm)) {
            return;
        }

        ItemStack mainHand = sender.getMainHandItem();
        if (MimiclingItem.tryTransformToForm(mainHand, sender, InteractionHand.MAIN_HAND, targetForm)) {
            return;
        }

        MimiclingItem.tryTransformToForm(sender.getOffhandItem(), sender, InteractionHand.OFF_HAND, targetForm);
    }
}
