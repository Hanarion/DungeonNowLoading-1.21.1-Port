package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GauntletBlockEntity extends BlockEntity {
    private int wavesTotal = 3;     // 1..5
    private int wavesCurrent = 0;   // 0..wavesTotal
    private boolean active = false;

    // client-only anim state
    public float glowPulse = 0f;    // 0..1
    public float prevPulse = 0f;

    public GauntletBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.GAUNTLET.get(), pos, state);
    }

    public static void clientTick(Level lvl, BlockPos pos, BlockState st, GauntletBlockEntity be) {
        be.prevPulse = be.glowPulse;
        if (be.active && be.wavesCurrent > 0) {
            be.glowPulse += 0.06f;      // tune speed
            if (be.glowPulse > 1f) be.glowPulse -= 1f;
        } else {
            be.glowPulse *= 0.85f;      // decay when idle
        }
    }

    // getters/setters + save/load NBT (wavesTotal, wavesCurrent, active)
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        wavesTotal   = Mth.clamp(tag.getInt("wavesTotal"), 1, 5);
        wavesCurrent = Mth.clamp(tag.getInt("wavesCurrent"), 0, wavesTotal);
        active = tag.getBoolean("active");
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        tag.putInt("wavesTotal", wavesTotal);
        tag.putInt("wavesCurrent", wavesCurrent);
        tag.putBoolean("active", active);
    }

    // API the game logic will call:
    public void setWaves(int total, int current, boolean active) {
        this.wavesTotal = Mth.clamp(total,1,5);
        this.wavesCurrent = Mth.clamp(current,0,this.wavesTotal);
        this.active = active;
        if (level != null && level.isClientSide) return;
        setChanged();
        if (level instanceof ServerLevel sl) sl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    public int getWavesTotal()   { return wavesTotal; }
    public int getWavesCurrent() { return wavesCurrent; }
    public boolean isActive()    { return active; }
    public float getGlowPulse()  { return glowPulse; }
    public float getPrevPulse()  { return prevPulse; }
}
