package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.util.OverworkedPenaltyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScepterOfSealedChaosItem extends Item {

    public ScepterOfSealedChaosItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        ItemStack itemStack = useOnContext.getItemInHand();
        if (itemStack.getDamageValue() == itemStack.getMaxDamage()) {
            return InteractionResult.FAIL;
        }

        Level level = useOnContext.getLevel();
        Player player = useOnContext.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos playerPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
        level.playSound(player, playerPos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS);

        boolean spawned = false;

        if (!level.isClientSide) {
            BlockPos blockPos = useOnContext.getClickedPos();
            Direction direction = useOnContext.getClickedFace();
            BlockState blockState = level.getBlockState(blockPos);
            BlockPos blockPos1;
            if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
                blockPos1 = blockPos;
            } else {
                blockPos1 = blockPos.relative(direction);
            }

            SealedChaosEntity sealedChaosEntity = DNLEntityTypes.SEALED_CHAOS.get().create(level);
            if (sealedChaosEntity != null) {
                sealedChaosEntity.moveTo(blockPos1, 0.0F, 0.0F);
                sealedChaosEntity.setOwnerUUID(player.getUUID());

                int arcLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.ARC_SHOT.get(), itemStack);
                int pulseLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.PULSE_SHOT.get(), itemStack);
                sealedChaosEntity.setArcShotLevel(Math.max(0, Math.min(2, arcLevel)));
                sealedChaosEntity.setPulseShotLevel(Math.max(0, Math.min(2, pulseLevel)));

                int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.GIGANTISM.get(), itemStack);
                if (gigantismLevel > 0) {
                    sealedChaosEntity.setGigantic(true);
                }

                // Prevent spawn if there is not enough free space for its (possibly gigantic) bounding box
                if (!level.noCollision(sealedChaosEntity)) {
                    player.getCooldowns().addCooldown(this, 20);
                    player.displayClientMessage(Component.literal("Not enough space for summon").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }

                // Visuals only for successful spawns
                ((ServerLevel) level).sendParticles(ParticleTypes.POOF, blockPos1.getX() + 0.5F, blockPos1.getY() + 0.5F, blockPos1.getZ() + 0.5F, 20, 0.3D, 0.3D, 0.3D, 0.0D);
                ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, blockPos1.getX() + 0.5F, blockPos1.getY() + 0.5F, blockPos1.getZ() + 0.5F, 10, 0.3D, 0.3D, 0.3D, 0.0D);

                int overworkedLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.OVERWORKED.get(), itemStack);
                if (overworkedLevel > 0) {
                    sealedChaosEntity.setOverworkedLevel(overworkedLevel);
                    sealedChaosEntity.applyOverworkedAttackSpeedBonus();
                }

                level.addFreshEntity(sealedChaosEntity);
                OverworkedPenaltyUtil.refreshOwnerPenalty((ServerLevel) level, player);

                player.getCooldowns().addCooldown(this, 600);
                spawned = true;
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (spawned && !player.getAbilities().instabuild) {
            itemStack.hurt(1, level.random, null);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return super.use(level, player, interactionHand);
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItemStack) {
        return repairItemStack.is(DNLItems.CHAOTIC_HEXAHEDRON.get()) || super.isValidRepairItem(itemStack, repairItemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_block").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_block.description").withStyle(ChatFormatting.DARK_GREEN));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_sealed_chaos").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_sealed_chaos.description").withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}