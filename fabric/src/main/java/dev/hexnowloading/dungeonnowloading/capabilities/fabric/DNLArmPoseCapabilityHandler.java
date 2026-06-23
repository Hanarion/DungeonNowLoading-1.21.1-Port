package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

// 1.21 / CCA 6.1.3: PlayerComponent -> RespawnableComponent; NBT methods take HolderLookup.Provider.
public class DNLArmPoseCapabilityHandler implements DNLArmPoseComponent, RespawnableComponent<DNLArmPoseCapabilityHandler> {

    private DNLArmPose armPose = DNLArmPose.EMPTY; // Default Pose

    @Override
    public void setArmPose(DNLArmPose pose) {
        armPose = pose;
    }

    @Override
    public DNLArmPose getArmPose() {
        return armPose;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("DNLArmPose", CompoundTag.TAG_STRING)) {
            this.armPose = DNLArmPose.fromId(tag.getString("DNLArmPose"));
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("DNLArmPose", armPose.getId());
    }

    @Override
    public void copyFrom(DNLArmPoseCapabilityHandler original, HolderLookup.Provider registries) {
        this.armPose = original.armPose;
    }
}
