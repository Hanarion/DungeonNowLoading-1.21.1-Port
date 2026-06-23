package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import net.minecraft.core.BlockPos;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.List;

// 1.21 / CCA 6.1.3: ComponentV3 was merged into Component.
public interface IFairkeeperChestPositionsCapability extends Component, AutoSyncedComponent {

    List<BlockPos> getList();

    void addBlock(BlockPos blockPos);

    void copyList(List<BlockPos> blockPos);
}
