package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import dev.hexnowloading.dungeonnowloading.screen.ClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class S2CGauntletOpenEditorPacket implements DNLPacket {
    private final BlockPos pos;
    private final int wavesTotal;
    private final int wavesCurrent;
    private final boolean active;

    // editor fields
    private final int relX, relY, relZ;
    private final int sizeX, sizeY, sizeZ;
    private final int activationRange;
    private final String lootTable;   // empty for null
    private final String testWave;    // may be empty

    public S2CGauntletOpenEditorPacket(BlockPos pos, int wavesTotal, int wavesCurrent, boolean active,
                                       int relX, int relY, int relZ,
                                       int sizeX, int sizeY, int sizeZ,
                                       int activationRange, String lootTable, String testWave) {
        this.pos = pos.immutable();
        this.wavesTotal = wavesTotal;
        this.wavesCurrent = wavesCurrent;
        this.active = active;
        this.relX = relX; this.relY = relY; this.relZ = relZ;
        this.sizeX = sizeX; this.sizeY = sizeY; this.sizeZ = sizeZ;
        this.activationRange = activationRange;
        this.lootTable = lootTable == null ? "" : lootTable;
        this.testWave = testWave == null ? "" : testWave;
    }

    public S2CGauntletOpenEditorPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.wavesTotal = buf.readVarInt();
        this.wavesCurrent = buf.readVarInt();
        this.active = buf.readBoolean();
        this.relX = buf.readVarInt();
        this.relY = buf.readVarInt();
        this.relZ = buf.readVarInt();
        this.sizeX = buf.readVarInt();
        this.sizeY = buf.readVarInt();
        this.sizeZ = buf.readVarInt();
        this.activationRange = buf.readVarInt();
        this.lootTable = buf.readUtf(32767);
        this.testWave = buf.readUtf(32767);
    }

    public static S2CGauntletOpenEditorPacket decode(FriendlyByteBuf buf) { return new S2CGauntletOpenEditorPacket(buf); }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(wavesTotal);
        buf.writeVarInt(wavesCurrent);
        buf.writeBoolean(active);
        buf.writeVarInt(relX);
        buf.writeVarInt(relY);
        buf.writeVarInt(relZ);
        buf.writeVarInt(sizeX);
        buf.writeVarInt(sizeY);
        buf.writeVarInt(sizeZ);
        buf.writeVarInt(activationRange);
        buf.writeUtf(lootTable);
        buf.writeUtf(testWave);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        var mc = ClientUtil.getClient();
        if (mc == null) return;
        mc.execute(() -> ClientScreens.openGauntletEditor(pos, wavesTotal, wavesCurrent, active,
                relX, relY, relZ, sizeX, sizeY, sizeZ, activationRange, lootTable, testWave));
    }
}
