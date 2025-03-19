package dev.hexnowloading.dungeonnowloading.capability.forge;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import net.minecraft.nbt.CompoundTag;

public class DNLArmPoseCapability {

    private DNLArmPose armPose = DNLArmPose.EMPTY;

    public DNLArmPose getArmPose() {
        return this.armPose;
    }

    public void setArmPose(DNLArmPose dnlArmPose) {
        armPose = dnlArmPose;
    }

    public void copyFrom(DNLArmPoseCapability source) {
        this.armPose = source.armPose;
    }
    public void saveNBTData(CompoundTag compoundTag) {
        compoundTag.putString("DNLArmPose", armPose.getId());
    }

    public void loadNBTData(CompoundTag compoundTag) {
        if (compoundTag.contains("DNLArmPose", CompoundTag.TAG_STRING)) {
            this.armPose = DNLArmPose.fromId(compoundTag.getString("DNLArmPose"));
        }

    }
}
