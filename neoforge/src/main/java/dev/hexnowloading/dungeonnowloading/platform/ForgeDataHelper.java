package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.capability.forge.DNLArmPoseCapability;
import dev.hexnowloading.dungeonnowloading.capability.forge.DNLAttachments;
import dev.hexnowloading.dungeonnowloading.capability.forge.FairkeeperChestPositionsCapability;
import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.services.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * 1.21 NeoForge: capabilities replaced by data attachments (getData/setData). The
 * attachment's default value is created lazily on first access.
 */
public class ForgeDataHelper implements DataHelper {

    @Override
    public DNLArmPose getArmPose(Player player) {
        return player.getData(DNLAttachments.DNL_ARM_POSE.get()).getArmPose();
    }

    @Override
    public void setArmPose(Player player, DNLArmPose pose) {
        DNLArmPoseCapability data = player.getData(DNLAttachments.DNL_ARM_POSE.get());
        data.setArmPose(pose);
        player.setData(DNLAttachments.DNL_ARM_POSE.get(), data);
    }

    @Override
    public Optional<List<BlockPos>> getFairkeeperChestPositionList(Player player) {
        return Optional.of(player.getData(DNLAttachments.FAIRKEEPER_CHEST_POSITIONS.get()).getList());
    }

    @Override
    public void addFairkeeperChestPositionList(Player player, BlockPos blockPos) {
        FairkeeperChestPositionsCapability data = player.getData(DNLAttachments.FAIRKEEPER_CHEST_POSITIONS.get());
        data.addBlockPos(blockPos);
        player.setData(DNLAttachments.FAIRKEEPER_CHEST_POSITIONS.get(), data);
    }

    @Override
    public void copyFairkeeperChestPositionList(Player player, List<BlockPos> list) {
        FairkeeperChestPositionsCapability data = player.getData(DNLAttachments.FAIRKEEPER_CHEST_POSITIONS.get());
        data.copyList(list);
        player.setData(DNLAttachments.FAIRKEEPER_CHEST_POSITIONS.get(), data);
    }
}
