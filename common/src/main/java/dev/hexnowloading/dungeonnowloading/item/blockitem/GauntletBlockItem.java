package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * BlockItem for the Gauntlet block.
 * - Preserves BlockEntityTag on place (wavesTotal, wavesCurrent, active, etc.)
 * - Adds a small tooltip summarizing saved data (optional)
 */
public class GauntletBlockItem extends BlockItem {

    public GauntletBlockItem() {
        super(DNLBlocks.GAUNTLET.get(), new Properties());
    }

    /**
     * Keep default placement behavior; no auto-placing of any other blocks.
     */
    @Override
    public net.minecraft.world.InteractionResult place(BlockPlaceContext ctx) {
        return super.place(ctx);
    }

    /**
     * Apply the item's BlockEntityTag to the newly placed Gauntlet BE.
     * Return true if anything was applied.
     */
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable net.minecraft.world.entity.player.Player player,
                                                 ItemStack stack, BlockState state) {
        CompoundTag bet = stack.getTagElement("BlockEntityTag");
        if (bet == null) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GauntletBlockEntity gauntletBE)) return false;

        // Merge the item's BlockEntityTag into the BE.
        // (Alternative: call a dedicated read method on your BE.)
        CompoundTag merged = be.saveWithoutMetadata();
        merged.merge(bet);
        be.load(merged);
        be.setChanged();

        if (!level.isClientSide && level instanceof ServerLevel sl) {
            sl.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
        return true;
    }

    /**
     * Optional: show quick summary when the item carries BE data.
     * Remove if you want a silent item.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, level, lines, flags);

        CompoundTag bet = stack.getTagElement("BlockEntityTag");
        if (bet != null) {
            int total   = bet.contains("wavesTotal")   ? bet.getInt("wavesTotal")   : -1;
            int current = bet.contains("wavesCurrent") ? bet.getInt("wavesCurrent") : -1;
            boolean active = bet.getBoolean("active");

            if (total >= 0) {
                lines.add(Component.literal("Waves: " + total).withStyle(ChatFormatting.GRAY));
            }
            if (current >= 0) {
                lines.add(Component.literal("Current Wave: " + current).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (active) {
                lines.add(Component.literal("Active").withStyle(ChatFormatting.GOLD));
            }
        }
    }
}
