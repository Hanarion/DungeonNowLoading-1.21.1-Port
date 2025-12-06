package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PotionBarrelBlockEntity extends BlockEntity {
    private MobEffect effect;

    public PotionBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.POTION_BARREL.get(), pos, state);
    }

    public void setEffect(MobEffect effect) {
        this.effect = effect;
        setChangedAndNotify();
    }

    public MobEffect getEffect() {
        return effect;
    }

    private void setChangedAndNotify() {
        this.setChanged();
        if (this.level != null) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (effect != null) {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            tag.putString("Effect", id.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Effect")) {
            ResourceLocation id = new ResourceLocation(tag.getString("Effect"));
            this.effect = BuiltInRegistries.MOB_EFFECT.get(id);
        } else {
            this.effect = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag); // includes "Effect"
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
