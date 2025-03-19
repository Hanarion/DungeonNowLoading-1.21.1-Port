package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.capability.forge.DNLArmPoseCapabilityProvider;
import dev.hexnowloading.dungeonnowloading.capability.forge.FairkeeperChestPositionsCapability;
import dev.hexnowloading.dungeonnowloading.capability.forge.FairkeeperChestPositionsCapabilityProvider;
import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.services.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public class ForgeDataHelper implements DataHelper {

    @Override
    public DNLArmPose getArmPose(Player player) {
        return player.getCapability(DNLArmPoseCapabilityProvider.DNL_ARM_POSE)
                .map(cap -> cap.getArmPose()) // ✅ Get the actual value
                .orElse(DNLArmPose.EMPTY); // ✅ Provide a default if missing
    }

    @Override
    public void setArmPose(Player player, DNLArmPose pose) {
        player.getCapability(DNLArmPoseCapabilityProvider.DNL_ARM_POSE).ifPresent(cap -> {
            cap.setArmPose(pose); // ✅ Actually sets the pose
        });
    }

    @Override
    public Optional<List<BlockPos>> getFairkeeperChestPositionList(Player player) {
        return player.getCapability(FairkeeperChestPositionsCapabilityProvider.FAIRKEEPER_CHEST_POSITIONS).map(FairkeeperChestPositionsCapability::getList);
    }

    @Override
    public void addFairkeeperChestPositionList(Player player, BlockPos blockPos) {
        player.getCapability(FairkeeperChestPositionsCapabilityProvider.FAIRKEEPER_CHEST_POSITIONS).ifPresent(cap -> {
            cap.addBlockPos(blockPos);
        });
    }

    @Override
    public void copyFairkeeperChestPositionList(Player player, List<BlockPos> list) {
        player.getCapability(FairkeeperChestPositionsCapabilityProvider.FAIRKEEPER_CHEST_POSITIONS).ifPresent(cap -> {
            cap.copyList(list);
        });
    }
}
