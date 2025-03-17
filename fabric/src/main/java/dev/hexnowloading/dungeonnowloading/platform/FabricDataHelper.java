package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.capabilities.CapabilityList;
import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import dev.hexnowloading.dungeonnowloading.platform.services.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public class FabricDataHelper implements DataHelper {

    @Override
    public DNLArmPose getArmPose(Player player) {
        if (CapabilityList.DNL_ARM_POSE.isProvidedBy(player)) {
            return CapabilityList.DNL_ARM_POSE.get(player).getArmPose();
        }
        return DNLArmPose.EMPTY; // Default pose
    }

    @Override
    public void setArmPose(Player player, DNLArmPose pose) {
        if (CapabilityList.DNL_ARM_POSE.isProvidedBy(player)) {
            CapabilityList.DNL_ARM_POSE.get(player).setArmPose(pose);
            CapabilityList.DNL_ARM_POSE.sync(player);
        }
    }

    @Override
    public int getScorcherHeat(Player player) {
        if (CapabilityList.SCORCHER_HEAT.isProvidedBy(player)) {
            return CapabilityList.SCORCHER_HEAT.get(player).getValue();
        }
        return 0;
    }

    @Override
    public void setScorcherHeat(Player player, int heat) {
        if (CapabilityList.SCORCHER_HEAT.isProvidedBy(player)) {
            CapabilityList.SCORCHER_HEAT.get(player).setValue(heat);
            CapabilityList.SCORCHER_HEAT.sync(player);
        }
    }

    @Override
    public Optional<List<BlockPos>> getFairkeeperChestPositionList(Player player) {
        if (CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.isProvidedBy(player)) {
            List<BlockPos> blockPosList = CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.get(player).getList();
            return Optional.ofNullable(blockPosList);
        }
        return Optional.empty();
        //return Optional.of(FairkeeperChestPositionsData.getList((IPlayerDataSaver) player));
    }

    @Override
    public void addFairkeeperChestPositionList(Player player, BlockPos blockPos) {
        if (CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.isProvidedBy(player)) {
            CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.get(player).addBlock(blockPos);
            CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.sync(player);
        }
        //FairkeeperChestPositionsData.addBlockPos((IPlayerDataSaver) player, blockPos);
    }

    @Override
    public void copyFairkeeperChestPositionList(Player player, List<BlockPos> list) {
        if (CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.isProvidedBy(player)) {
            CapabilityList.FAIRKEEPER_CHEST_POSITIONS_CAP.get(player).copyList(list);
        }
        //FairkeeperChestPositionsData.copyList((IPlayerDataSaver) player, list);
    }
}
