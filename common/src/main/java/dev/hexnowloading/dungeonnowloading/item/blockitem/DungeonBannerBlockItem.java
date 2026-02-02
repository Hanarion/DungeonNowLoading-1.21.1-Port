package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonBannerBlockItem extends BlockItem {

    private final DungeonBannerBlock.DungeonBannerVariant variant;

    public DungeonBannerBlockItem(Block block, DungeonBannerBlock.DungeonBannerVariant variant, Properties props) {
        super(block, props);
        this.variant = variant;
    }

    public DungeonBannerBlock.DungeonBannerVariant getVariant() {
        return variant;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext ctx) {
        BlockState state = super.getPlacementState(ctx);
        if (state == null) return null;
        return state.setValue(DungeonBannerBlock.VARIANT, this.variant);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item." + DungeonNowLoading.MOD_ID + ".dungeon_banner_" + this.variant.getSerializedName();
    }
}