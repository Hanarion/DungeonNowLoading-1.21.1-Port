package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.world.item.Rarity;

public class MendstonePickaxeItem extends PickaxeItem {

    public MendstonePickaxeItem(Properties properties) {
        super(Tiers.IRON, 1, -2.8F, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.is(Blocks.DIAMOND_ORE) && level instanceof ServerLevel serverLevel) {
            int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
            int dropCount = 1 + level.random.nextInt(fortuneLevel + 1);

            for (int i = 0; i < dropCount; i++) {
                Block.popResource(level, pos, new ItemStack(DNLItems.DURITE.get()));
            }

            level.destroyBlock(pos, false, entity);
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(entity.getUsedItemHand()));
            return true;
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        components.add(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_name").withStyle(ChatFormatting.BLUE));
        components.add(Component.translatable("item.dungeonnowloading.mendstone_pickaxe.tooltip.ability_description").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }
}
