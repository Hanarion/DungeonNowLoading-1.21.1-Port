package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class C2SPedestalEditPacket implements DNLPacket {
    private final BlockPos pos;
    private final List<Component> lines;
    private final int colorId;   // -1 to keep existing color
    private final boolean glowing;

    public C2SPedestalEditPacket(BlockPos pos, List<Component> lines, int colorId, boolean glowing) {
        this.pos = pos;
        this.lines = List.copyOf(lines);
        this.colorId = colorId;
        this.glowing = glowing;
    }

    public C2SPedestalEditPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        int n = buf.readVarInt();
        List<Component> tmp = new ArrayList<>(n);
        for (int i = 0; i < n; i++) tmp.add(buf.readComponent());
        this.lines = List.copyOf(tmp);
        this.colorId = buf.readVarInt();
        this.glowing = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(lines.size());
        for (Component c : lines) buf.writeComponent(c);
        buf.writeVarInt(colorId);
        buf.writeBoolean(glowing);
    }

    public static C2SPedestalEditPacket decode(FriendlyByteBuf buf) {
        return new C2SPedestalEditPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null) return;
        var level = sender.level();
        if (!level.isLoaded(pos)) return;

        var be = level.getBlockEntity(pos);
        if (!(be instanceof PlayerStatueBlockEntity statue)) return;

        // simple reach check
        if (sender.distanceToSqr(pos.getCenter()) > 64.0D) return;

        var color = (colorId >= 0 && colorId < DyeColor.values().length)
                ? DyeColor.byId(colorId)
                : statue.getTextColor();

        statue.setAllText(lines, color, glowing);
        statue.setChanged();

        var state = statue.getBlockState();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }
}