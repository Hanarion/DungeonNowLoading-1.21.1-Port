package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.screen.ClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class S2CPedestalOpenEditorPacket implements DNLPacket {

    private final BlockPos pos;
    private final List<Component> lines;
    private final int colorId;
    private final boolean glowing;

    public S2CPedestalOpenEditorPacket(BlockPos pos, List<Component> lines, DyeColor color, boolean glowing) {
        this.pos = pos.immutable();
        this.lines = lines;
        this.colorId = color.getId();
        this.glowing = glowing;
    }

    public S2CPedestalOpenEditorPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        int n = buf.readVarInt();
        List<Component> tmp = new ArrayList<>(n);
        for (int i = 0; i < n; i++) tmp.add(buf.readComponent());
        this.lines = tmp;
        this.colorId = buf.readVarInt();
        this.glowing = buf.readBoolean();
    }

    @Override public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(lines.size());
        for (Component c : lines) buf.writeComponent(c);
        buf.writeVarInt(colorId);
        buf.writeBoolean(glowing);
    }
    public static S2CPedestalOpenEditorPacket decode(FriendlyByteBuf buf){ return new S2CPedestalOpenEditorPacket(buf); }

    @Override
    public void handle(@Nullable ServerPlayer sender) { // client side
        var mc = ClientUtil.getClient();
        if (mc == null) return;
        var color = DyeColor.byId(colorId);
        mc.execute(() -> ClientScreens.openPedestalEditor(
                pos, lines, (color == null ? DyeColor.BLACK : color), glowing));
    }
}