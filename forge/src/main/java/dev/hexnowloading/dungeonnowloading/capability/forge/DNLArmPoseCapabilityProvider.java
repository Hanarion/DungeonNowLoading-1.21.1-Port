package dev.hexnowloading.dungeonnowloading.capability.forge;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DNLArmPoseCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<DNLArmPoseCapability> DNL_ARM_POSE = CapabilityManager.get(new CapabilityToken<DNLArmPoseCapability>() { });

    private DNLArmPoseCapability armPose = null;
    private final LazyOptional<DNLArmPoseCapability> optional = LazyOptional.of(this::createArmPose);

    private DNLArmPoseCapability createArmPose() {
        if (this.armPose == null) {
            this.armPose = new DNLArmPoseCapability();
        }
        return this.armPose;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
        if (capability == DNL_ARM_POSE) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        createArmPose().saveNBTData(compoundTag);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag compoundTag) {
        createArmPose().loadNBTData(compoundTag);
    }
}
