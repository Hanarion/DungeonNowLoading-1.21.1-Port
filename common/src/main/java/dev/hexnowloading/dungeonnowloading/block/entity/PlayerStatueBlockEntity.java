package dev.hexnowloading.dungeonnowloading.block.entity;

import com.mojang.authlib.GameProfile;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerStatueBlockEntity extends BlockEntity {

    private GameProfile owner;           // resolved when item is placed
    private int poseVariant = 0;         // if your Blockbench has multiple poses

    public PlayerStatueBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.PLAYER_STATUE.get(), pos, state); // register this type
    }

    public void setOwner(GameProfile gp) { this.owner = gp; setChanged(); }
    public GameProfile getOwner() { return owner; }

    public int getPoseVariant() { return poseVariant; }
    public void setPoseVariant(int v){ this.poseVariant = v; setChanged(); }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) {
            CompoundTag o = new CompoundTag();
            NbtUtils.writeGameProfile(o, owner);
            tag.put("Owner", o);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner", 10)) {
            this.owner = NbtUtils.readGameProfile(tag.getCompound("Owner"));
        } else {
            this.owner = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
