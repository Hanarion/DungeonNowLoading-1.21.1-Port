package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.CompoundTag;

public class DNLArmPoseCapabilityHandler implements DNLArmPoseComponent, PlayerComponent<DNLArmPoseCapabilityHandler> {

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
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("DNLArmPose", CompoundTag.TAG_STRING)) {
            this.armPose = DNLArmPose.fromId(tag.getString("DNLArmPose"));
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putString("DNLArmPose", armPose.getId());
    }
}
