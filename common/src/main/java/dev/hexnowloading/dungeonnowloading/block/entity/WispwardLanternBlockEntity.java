package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WispwardLanternBlockEntity extends BlockEntity {
    private static final int MIN_TIMER_SECONDS = 1;
    private static final int MAX_TIMER_SECONDS = 15;

    private int timerSeconds = MIN_TIMER_SECONDS;
    private long litUntilGameTime = 0L;
    private boolean lockedLit = false;

    public WispwardLanternBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.WISPWARD_LANTERN.get(), pos, state);
    }

    public int getTimerSeconds() {
        return this.timerSeconds;
    }

    public int cycleTimerSeconds() {
        this.timerSeconds = this.timerSeconds >= MAX_TIMER_SECONDS ? MIN_TIMER_SECONDS : this.timerSeconds + 1;
        this.setChanged();
        this.syncToClients();
        return this.timerSeconds;
    }

    public void markLit(long gameTime) {
        this.litUntilGameTime = gameTime + this.timerSeconds * 20L;
        this.setChanged();
        this.syncToClients();
    }

    public boolean isLockedLit() {
        return this.lockedLit;
    }

    public void lockLit() {
        if (this.lockedLit) {
            return;
        }

        this.lockedLit = true;
        this.litUntilGameTime = Long.MAX_VALUE;
        this.setChanged();
        this.syncToClients();
    }

    public boolean shouldTurnOff(long gameTime) {
        if (this.lockedLit) {
            return false;
        }

        return this.litUntilGameTime <= 0L || gameTime >= this.litUntilGameTime;
    }

    public int getRemainingLitTicks(long gameTime) {
        return Math.max(1, (int) (this.litUntilGameTime - gameTime));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TimerSeconds", this.timerSeconds);
        tag.putLong("LitUntilGameTime", this.litUntilGameTime);
        tag.putBoolean("LockedLit", this.lockedLit);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.timerSeconds = Math.max(MIN_TIMER_SECONDS, Math.min(MAX_TIMER_SECONDS, tag.getInt("TimerSeconds")));
        this.litUntilGameTime = tag.getLong("LitUntilGameTime");
        this.lockedLit = tag.getBoolean("LockedLit");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("TimerSeconds", this.timerSeconds);
        tag.putLong("LitUntilGameTime", this.litUntilGameTime);
        tag.putBoolean("LockedLit", this.lockedLit);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClients() {
        if (this.level == null) {
            return;
        }

        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }
}
