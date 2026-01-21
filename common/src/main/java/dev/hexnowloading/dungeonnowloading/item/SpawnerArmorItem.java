package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.config.PvpConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerArmorItem extends ArmorItem {

    private int summonTick = 200;
    private final int spawnRange = 4;

    public SpawnerArmorItem(ArmorMaterial armorMaterial, Type slot) {
        super(armorMaterial, slot, new Properties());
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemStack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (!itemStack.is(DNLItems.SPAWNER_HELMET.get())) return;

        // Overworked: soft health cap while wearing helmet with enchant
        int overworkedLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.OVERWORKED.get(), itemStack);
        if (overworkedLevel > 0) {
            // Each level effectively removes 1 heart (2 HP) from the "safe" health cap
            float baseMax = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
            float effectiveMax = baseMax - (2.0F * overworkedLevel);
            if (effectiveMax < 1.0F) {
                effectiveMax = 1.0F; // never hard-cap below 1 HP
            }
            if (player.getHealth() > effectiveMax) {
                player.setHealth(effectiveMax);
            }
        }

        if (!hasFullSuitOfArmorOn(player)) return;

        if (summonTick > 0) {
            summonTick--;
        }

        BlockPos pos = player.getOnPos();
        double r = 5.0;

        // Check for monsters
        boolean hostileMobNearby = !level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(r),
                mob -> mob.isAlive()
        ).isEmpty();

        // Check for opposing players if PvP is enabled
        boolean opposingPlayerNearby = PvpConfig.TOGGLE_PVP_MODE.get() && !level.getEntitiesOfClass(
                Player.class,
                player.getBoundingBox().inflate(r),
                other -> other != player
                        && !player.isAlliedTo(other) // different team
                        && !other.isSpectator()
                        && !other.isCreative()
                        && other.isAlive()
        ).isEmpty();

        if (hostileMobNearby || opposingPlayerNearby) {
            if (summonTick <= 0 && hasCorrectArmorOn(player)) {
                summonMob(level, pos, player);
                summonTick = 200;
            }
        } else if (summonTick <= 0) {
            summonTick = 40;
        }

        // Pack Blessing: 10s Regen, Regen I starts at 2 Whimpers
        int packBlessingLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.PACK_BLESSING.get(), itemStack);
        if (packBlessingLevel > 0) {
            List<WhimperEntity> whimpers = level.getEntitiesOfClass(
                    WhimperEntity.class,
                    player.getBoundingBox().inflate(6.0D),
                    w -> {
                        if (!w.isAlive()) return false;
                        // Only count Whimpers owned by this player; guard against null owner UUID
                        if (w.getOwnerUUID() == null) return false;
                        Player owner = level.getPlayerByUUID(w.getOwnerUUID());
                        return owner != null && owner.equals(player);
                    }
            );

            int count = whimpers.size();
            if (count >= 2) {
                // 2 Whimpers -> Regen I (amp 0), 3 -> Regen II (amp 1), etc.
                int amplifier = count - 2;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, amplifier, true, false));
            }
        }
    }

    private void summonMob(Level level, BlockPos entityPos, Player owner) {
        RandomSource randomSource = level.getRandom();
        EntityType<Zombie> spawningEntity = EntityType.ZOMBIE;
        double x = entityPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
        double y = entityPos.getY() + randomSource.nextInt(3) - 1;
        double z = entityPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
        if (level.noCollision(spawningEntity.getAABB(x, y, z))) {
            ((ServerLevel) level).sendParticles(ParticleTypes.POOF, x + 0.5F, y + 0.5F, z + 0.5F, 20, 0.3D, 0.3D, 0.3D, 0.0D);
            ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, x + 0.5F, y + 0.5F, z + 0.5F, 10, 0.3D, 0.3D, 0.3D, 0.0D);
            WhimperEntity whimper = DNLEntityTypes.WHIMPER.get().create(level);
            if (whimper != null) {
                whimper.moveTo(x, y, z, 0.0F, 0.0F);
                whimper.setOwnerUUID(owner.getUUID());
                // Gigantism: if the helmet has the enchant, double Whimper's size
                ItemStack helmetStack = owner.getInventory().getArmor(3);
                int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.GIGANTISM.get(), helmetStack);
                if (gigantismLevel > 0) {
                    whimper.setGigantic(true);
                }
                level.addFreshEntity(whimper);
            }
        }
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getInventory().getArmor(0);
        ItemStack leggings = player.getInventory().getArmor(1);
        ItemStack chestplate = player.getInventory().getArmor(2);
        ItemStack helmet = player.getInventory().getArmor(3);

        return !helmet.isEmpty() && !chestplate.isEmpty() && !leggings.isEmpty() && !boots.isEmpty();
    }

    private boolean hasCorrectArmorOn(Player player) {
        for (ItemStack armorStack: player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        ArmorItem boots = (ArmorItem) player.getInventory().getArmor(0).getItem();
        ArmorItem leggings = (ArmorItem) player.getInventory().getArmor(1).getItem();
        ArmorItem chestplate = (ArmorItem) player.getInventory().getArmor(2).getItem();
        ArmorItem helmet = (ArmorItem) player.getInventory().getArmor(3).getItem();

        return helmet.getMaterial() == material && chestplate.getMaterial() == material && leggings.getMaterial() == material && boots.getMaterial() == material;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.spawner_armor.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.spawner_armor.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}