package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.capability.forge.FairkeeperChestPositionsCapability;
import dev.hexnowloading.dungeonnowloading.capability.forge.FairkeeperChestPositionsCapabilityProvider;
import dev.hexnowloading.dungeonnowloading.platform.services.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public class ForgeDataHelper implements DataHelper {

    @Override
    public int getScorcherHeat(Player player) {
        return 0;
    }

    @Override
    public void setScorcherHeat(Player player, int heat) {

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
