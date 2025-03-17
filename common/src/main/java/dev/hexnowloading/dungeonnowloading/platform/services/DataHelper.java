package dev.hexnowloading.dungeonnowloading.platform.services;

import dev.hexnowloading.dungeonnowloading.item.client.DNLArmPose;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public interface DataHelper {

    DNLArmPose getArmPose(Player player);

    void setArmPose(Player player, DNLArmPose pose);

    int getScorcherHeat(Player player);

    void setScorcherHeat(Player player, int heat);

    Optional<List<BlockPos>> getFairkeeperChestPositionList(Player player);

    void addFairkeeperChestPositionList(Player player, BlockPos blockPos);

    void copyFairkeeperChestPositionList(Player player, List<BlockPos> list);

}
