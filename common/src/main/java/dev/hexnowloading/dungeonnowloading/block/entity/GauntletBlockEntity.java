package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GauntletBlockEntity extends BlockEntity {
    private int wavesTotal = 0;
    private int wavesCurrent = 0;
    private boolean active = false;

    private int relX = 0, relY = 0, relZ = 0;
    private int sizeX = 0, sizeY = 0, sizeZ = 0;
    private int activationRange = 0;
    private ResourceLocation lootTable = null;

    public GauntletBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.GAUNTLET.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        wavesTotal = Mth.clamp(tag.getInt("wavesTotal"), 1, 5);
        wavesCurrent = Mth.clamp(tag.getInt("wavesCurrent"), 0, wavesTotal);
        active = tag.getBoolean("active");
        relX = tag.getInt("relX");
        relY = tag.getInt("relY");
        relZ = tag.getInt("relZ");
        sizeX = tag.getInt("sizeX");
        sizeY = tag.getInt("sizeY");
        sizeZ = tag.getInt("sizeZ");
        activationRange = tag.getInt("activationRange");
        if (tag.contains("lootTable")) lootTable = new ResourceLocation(tag.getString("lootTable"));
        else lootTable = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putInt("wavesTotal", wavesTotal);
        tag.putInt("wavesCurrent", wavesCurrent);
        tag.putBoolean("active", active);
        tag.putInt("relX", relX);
        tag.putInt("relY", relY);
        tag.putInt("relZ", relZ);
        tag.putInt("sizeX", sizeX);
        tag.putInt("sizeY", sizeY);
        tag.putInt("sizeZ", sizeZ);
        tag.putInt("activationRange", activationRange);
        if (lootTable != null) tag.putString("lootTable", lootTable.toString());
    }


    public void setWaves(int total, int current, boolean active) {
        this.wavesTotal = Mth.clamp(total, 1, 5);
        this.wavesCurrent = Mth.clamp(current, 0, this.wavesTotal);
        this.active = active;
        if (level != null && level.isClientSide) return;
        setChanged();
        if (level instanceof ServerLevel sl) sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void setEditorConfig(int relX, int relY, int relZ, int sizeX, int sizeY, int sizeZ, int activationRange, ResourceLocation lootTable, String testWave) {
        this.relX = relX;
        this.relY = relY;
        this.relZ = relZ;
        this.sizeX = Math.max(0, sizeX);
        this.sizeY = Math.max(0, sizeY);
        this.sizeZ = Math.max(0, sizeZ);
        this.activationRange = Math.max(0, activationRange);
        this.lootTable = lootTable;
        if (level != null && level.isClientSide) return;
        setChanged();
        if (level instanceof ServerLevel sl) sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public int getWavesTotal() {
        return wavesTotal;
    }

    public int getWavesCurrent() {
        return wavesCurrent;
    }

    public boolean isActive() {
        return active;
    }

    public int getRelX() {
        return relX;
    }

    public int getRelY() {
        return relY;
    }

    public int getRelZ() {
        return relZ;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }

    public int getActivationRange() {
        return activationRange;
    }

    public ResourceLocation getLootTable() {
        return lootTable;
    }
}
