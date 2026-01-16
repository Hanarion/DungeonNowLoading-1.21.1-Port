package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GauntletVaultBlockItem extends BlockItem {

    public GauntletVaultBlockItem() {
        super(DNLBlocks.GAUNTLET_VAULT.get(), new Properties());
    }

    @Override
    public net.minecraft.world.InteractionResult place(BlockPlaceContext ctx) {
        return net.minecraft.world.InteractionResult.FAIL; // cannot place directly
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable net.minecraft.world.level.Level level, List<Component> lines, TooltipFlag flags) {
        lines.add(Component.literal("Placed automatically under the Gauntlet").withStyle(ChatFormatting.DARK_GRAY));
    }
}
