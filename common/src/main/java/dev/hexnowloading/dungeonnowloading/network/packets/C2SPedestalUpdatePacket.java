package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class C2SPedestalUpdatePacket implements DNLPacket {
    private static final int MAX_LINES = 4;
    private static final int MAX_CHARS_PER_LINE = 16;
    private static final double MAX_EDIT_DISTANCE_SQ = 64.0 * 64.0;

    private final BlockPos pos;
    private final List<Component> lines;
    private final DyeColor color;
    private final boolean glowing;

    // Client ctor
    public C2SPedestalUpdatePacket(BlockPos pos, List<Component> lines, DyeColor color, boolean glowing) {
        this.pos = pos.immutable();
        this.lines = sanitize(lines);
        this.color = (color != null) ? color : DyeColor.BLACK;
        this.glowing = glowing;
    }

    // Decode (server)
    public C2SPedestalUpdatePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();

        int n = Math.min(buf.readVarInt(), MAX_LINES);
        List<Component> tmp = new ArrayList<>(MAX_LINES);
        for (int i = 0; i < n; i++) {
            // FriendlyByteBuf has readComponent/writeComponent in modern versions
            tmp.add(net.minecraft.network.chat.ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buf));
        }
        this.lines = sanitize(tmp);

        int colorId = buf.readVarInt();
        DyeColor c = DyeColor.byId(colorId);
        this.color = (c != null) ? c : DyeColor.BLACK;

        this.glowing = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(lines.size());
        for (Component c : lines) {
            net.minecraft.network.chat.ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buf, c);
        }
        buf.writeVarInt(color.getId());
        buf.writeBoolean(glowing);
    }

    public static C2SPedestalUpdatePacket decode(FriendlyByteBuf buf) {
        return new C2SPedestalUpdatePacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        System.out.println("[C2S Update] recv from " + sender.getGameProfile().getName() + " @ " + pos);

        if (sender == null) return;
        sender.server.execute(() -> {                 // <-- hop to server thread
            var level = sender.level();
            if (!level.isLoaded(pos)) return;

            var be = level.getBlockEntity(pos);
            if (!(be instanceof PlayerStatueBlockEntity statue)) return;

            // If you still want a hard reach check here:
            // if (sender.distanceToSqr(pos.getCenter()) > 64.0D) return;

            statue.applyTextUpdateFromClient(sender, lines, color, glowing);
        });
    }


    private static List<Component> sanitize(List<Component> in) {
        List<Component> out = new ArrayList<>(MAX_LINES);
        int n = Math.min(in != null ? in.size() : 0, MAX_LINES);
        for (int i = 0; i < n; i++) {
            String s = in.get(i) != null ? in.get(i).getString() : "";
            if (s.length() > MAX_CHARS_PER_LINE) s = s.substring(0, MAX_CHARS_PER_LINE);
            out.add(Component.literal(s));
        }
        while (out.size() < MAX_LINES) out.add(Component.empty());
        return out;
    }
}
