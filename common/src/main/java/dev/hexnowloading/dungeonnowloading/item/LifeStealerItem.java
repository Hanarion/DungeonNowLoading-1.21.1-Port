package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LifeStealerItem extends SwordItem {

    public LifeStealerItem(Tier $$0, int $$1, float $$2, Properties $$3) {
        super($$0, $$1, $$2, $$3);
    }

    /*@Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity hurtedEntity, LivingEntity userEntity) {
        if (super.hurtEnemy(itemStack, hurtedEntity, userEntity)) {
            RandomSource randomSource = userEntity.getRandom();
            if (randomSource.nextFloat() < 1.0F) {
                float healAmount = 1.0F;
                userEntity.heal(healAmount);
                Level level = userEntity.level();
                if (!level.isClientSide) {
                    ((ServerLevel) level).sendParticles(ParticleTypes.ANGRY_VILLAGER, hurtedEntity.getX(), hurtedEntity.getY() + 2.0, hurtedEntity.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.0D);
                }
            }
            return true;
        } else {
            return false;
        }
    }*/

    public static float onLivingDamage(LivingEntity attacker, LivingEntity target, float damage) {
        // Sacrifice enchantment: heal when hitting allies/summons
        int sacrificeLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.SACRIFICE.get(), attacker.getMainHandItem());
        boolean sacrificeTriggered = sacrificeLevel > 0 && damage > 0.0F && isAllyOrSummon(attacker, target);
        if (sacrificeTriggered) {
            float healFactor = 0.2F + 0.1F * sacrificeLevel; // 20% + 10% per level
            float healAmount = damage * healFactor;
            if (healAmount > 0.0F) {
                attacker.heal(healAmount);
            }
        } else {
            // Base Life Stealer lifesteal (default 20%)
            healthDrain(attacker, damage);
        }
        return damage;
    }

    private static boolean isAllyOrSummon(LivingEntity attacker, LivingEntity target) {
        if (attacker == target) {
            return false; // don't treat self-hit as Sacrifice trigger
        }

        // Vanilla team/ally logic (covers same team and tamed pets)
        if (attacker.isAlliedTo(target)) {
            return true;
        }

        // Explicit ownership check for tamable/ownable entities
        if (target instanceof OwnableEntity ownable && ownable.getOwner() == attacker) {
            return true;
        }

        return false;
    }

    public static void healthDrain(LivingEntity hurtingEntity, float damage) {
        float healAmount = damage * 0.2F;
        if (healAmount > 0.0F) {
            hurtingEntity.heal(healAmount);
            Level level = hurtingEntity.level();
            level.playSound(null, hurtingEntity.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 2.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HEART, hurtingEntity.getX(), hurtingEntity.getY() + 2.0, hurtingEntity.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItem) {
        return repairItem.is(DNLItems.SPAWNER_BLADE.get()) || super.isValidRepairItem(itemStack, repairItem);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.life_stealer.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.life_stealer.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
