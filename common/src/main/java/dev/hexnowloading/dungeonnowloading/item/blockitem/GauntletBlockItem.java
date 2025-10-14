package dev.hexnowloading.dungeonnowloading.item.blockitem;

import dev.hexnowloading.dungeonnowloading.block.GauntletBlock;
import dev.hexnowloading.dungeonnowloading.block.GauntletVaultBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Places a 2-block structure: vault pedestal (hidden item) + gauntlet top.
 * The item itself represents the gauntlet (top) but handles placing both.
 */
public class GauntletBlockItem extends BlockItem {

    public GauntletBlockItem() {
        super(DNLBlocks.GAUNTLET.get(), new Properties());
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        BlockPos clickedPos = ctx.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        boolean replace = clickedState.canBeReplaced(ctx);
        BlockPos basePos = replace ? clickedPos : clickedPos.relative(ctx.getClickedFace());
        BlockPos topPos = basePos.above();

        if (!level.isInWorldBounds(basePos) || !level.isInWorldBounds(topPos)) return InteractionResult.FAIL;
        if (!level.getBlockState(basePos).canBeReplaced(ctx)) return InteractionResult.FAIL;
        if (!level.getBlockState(topPos).canBeReplaced(ctx)) return InteractionResult.FAIL;

        Direction facing = ctx.getHorizontalDirection().getOpposite();
        BlockState vaultState = DNLBlocks.GAUNTLET_VAULT.get().defaultBlockState();
        if (vaultState.hasProperty(GauntletVaultBlock.FACING))
            vaultState = vaultState.setValue(GauntletVaultBlock.FACING, facing);
        BlockState gauntletState = DNLBlocks.GAUNTLET.get().defaultBlockState();
        if (gauntletState.hasProperty(GauntletBlock.FACING))
            gauntletState = gauntletState.setValue(GauntletBlock.FACING, facing);

        if (!player.mayUseItemAt(basePos, ctx.getClickedFace(), ctx.getItemInHand())) return InteractionResult.FAIL;
        if (!player.mayUseItemAt(topPos, ctx.getClickedFace(), ctx.getItemInHand())) return InteractionResult.FAIL;

        if (!level.setBlock(basePos, vaultState, Block.UPDATE_ALL_IMMEDIATE)) return InteractionResult.FAIL;
        if (!level.setBlock(topPos, gauntletState, Block.UPDATE_ALL_IMMEDIATE)) {
            level.removeBlock(basePos, false);
            return InteractionResult.FAIL;
        }

        this.updateCustomBlockEntityTag(topPos, level, player, ctx.getItemInHand(), gauntletState);

        SoundType sound = gauntletState.getSoundType();
        level.playSound(player, basePos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume()+1F)/2F, sound.getPitch()*0.8F);

        if (!player.getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable net.minecraft.world.entity.player.Player player,
                                                 ItemStack stack, BlockState state) {
        CompoundTag bet = stack.getTagElement("BlockEntityTag");
        if (bet == null) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GauntletBlockEntity)) return false;
        CompoundTag merged = be.saveWithoutMetadata();
        merged.merge(bet);
        be.load(merged);
        be.setChanged();
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            sl.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, level, lines, flags);
        CompoundTag bet = stack.getTagElement("BlockEntityTag");
        if (bet != null) {
            int total = bet.contains("wavesTotal") ? bet.getInt("wavesTotal") : -1;
            int current = bet.contains("wavesCurrent") ? bet.getInt("wavesCurrent") : -1;
            boolean active = bet.getBoolean("active");
            if (total >= 0) lines.add(Component.literal("Waves: " + total).withStyle(ChatFormatting.GRAY));
            if (current >= 0) lines.add(Component.literal("Current Wave: " + current).withStyle(ChatFormatting.DARK_GRAY));
            if (active) lines.add(Component.literal("Active").withStyle(ChatFormatting.GOLD));
        }
    }
}
