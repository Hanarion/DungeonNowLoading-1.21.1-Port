package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class MimiclingAxeItem extends AxeItem implements MimiclingFormItem {
    public MimiclingAxeItem(Properties properties) {
        this(Tiers.DIAMOND, properties);
    }

    public MimiclingAxeItem(Tier tier, Properties properties) {
        super(tier, getAttackDamageBonus(tier), getAttackSpeed(tier), properties);
    }

    private static float getAttackDamageBonus(Tier tier) {
        if (tier == Tiers.STONE) {
            return 7.0F;
        }
        if (tier == Tiers.IRON || tier == Tiers.GOLD) {
            return 6.0F;
        }
        if (tier == Tiers.DIAMOND || tier == Tiers.NETHERITE) {
            return 5.0F;
        }
        return 6.0F;
    }

    private static float getAttackSpeed(Tier tier) {
        if (tier == Tiers.IRON) {
            return -3.1F;
        }
        if (tier == Tiers.WOOD || tier == Tiers.STONE) {
            return -3.2F;
        }
        return -3.0F;
    }

    @Override
    public String getMimiclingForm() {
        return MimiclingItem.getAxeForm();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack carriedStack, Slot slot, ClickAction clickAction, Player player, net.minecraft.world.entity.SlotAccess carriedSlot) {
        return MimiclingItem.tryTemporarilyOpenForInventoryFeed(stack, carriedStack, slot, clickAction, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MimiclingItem.tickMimiclingInventory(stack, level, entity);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        MimiclingItem.onMimiclingMineBlock(stack, level, state, pos, entity);
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        MimiclingItem.onMimiclingHurtEnemy(stack, target, attacker);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return MimiclingItem.isMimiclingFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        MimiclingItem.appendActiveFoodTooltip(stack, components);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return MimiclingItem.getActiveFoodTooltipImage(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        MimiclingItem.onMimiclingDestroyed(itemEntity);
    }
}
