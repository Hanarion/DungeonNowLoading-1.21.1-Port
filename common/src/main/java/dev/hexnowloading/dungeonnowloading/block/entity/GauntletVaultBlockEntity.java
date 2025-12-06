package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// bottom block of the gauntlet. after gauntlet is defeated, the gauntlet block breaks and this bottom block remains to collect the loot out of it.
// so this block holds the actual 'loot' of the gauntlet.
public class GauntletVaultBlockEntity extends BlockEntity {
    public GauntletVaultBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.GAUNTLET_VAULT.get(), pos, state);
    }
}