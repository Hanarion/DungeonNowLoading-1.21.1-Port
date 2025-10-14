package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.GauntletBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class C2SGauntletUpdatePacket implements DNLPacket {
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

    public C2SGauntletUpdatePacket(BlockPos pos, int wavesTotal, int wavesCurrent, boolean active,
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

    public C2SGauntletUpdatePacket(FriendlyByteBuf buf) {
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

    public static C2SGauntletUpdatePacket decode(FriendlyByteBuf buf) { return new C2SGauntletUpdatePacket(buf); }

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
        if (sender == null) return;
        var level = sender.level();
        if (!level.isLoaded(pos)) return;
        if (!sender.getAbilities().instabuild) return; // creative only
        var be = level.getBlockEntity(pos);
        if (!(be instanceof GauntletBlockEntity gauntlet)) return;
        gauntlet.setWaves(wavesTotal, wavesCurrent, active);
        ResourceLocation rl = null;
        if (!lootTable.isEmpty()) {
            try { rl = new ResourceLocation(lootTable); } catch (Exception ignored) { rl = null; }
        }
        gauntlet.setEditorConfig(relX, relY, relZ, sizeX, sizeY, sizeZ, activationRange, rl, testWave);
        BlockState state = gauntlet.getBlockState();
        if (state.hasProperty(GauntletBlock.ACTIVE) && state.getValue(GauntletBlock.ACTIVE) != active) {
            level.setBlock(pos, state.setValue(GauntletBlock.ACTIVE, active), 3);
        } else {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}
