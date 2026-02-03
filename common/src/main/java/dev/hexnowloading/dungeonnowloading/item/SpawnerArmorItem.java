package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.config.PvpConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.supporter.DNLSupporters;
import dev.hexnowloading.dungeonnowloading.util.OverworkedPenaltyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerArmorItem extends ArmorItem {

    private int summonTick = 200;
    private final int spawnRange = 4;

    private static final String TAG_WHIMPER_COSMETIC_MODE = "WhimperCosmeticMode";
    private static final String MODE_DEFAULT = "default";
    private static final String MODE_LANTERN = "lantern";
    private static final String MODE_MIX = "mix";

    public SpawnerArmorItem(ArmorMaterial armorMaterial, Type slot) {
        super(armorMaterial, slot, new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (blockState.is(Blocks.LANTERN)) {

            if (!level.isClientSide && player != null) {
                if (!DNLSupporters.hasSkin(player.getUUID(), "whimper_lantern")) return InteractionResult.SUCCESS;

                cycleWhimperCosmeticMode(stack);

                String modeName = getWhimperCosmeticMode(stack);
                String capitalizedMode = modeName.isEmpty()
                        ? ""
                        : modeName.substring(0, 1).toUpperCase() + modeName.substring(1);

                player.displayClientMessage(
                        Component.literal("Whimper Mode: " + capitalizedMode).withStyle(ChatFormatting.YELLOW),
                        true
                );
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemStack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        // Only the equipped spawner helmet in the armor helmet slot should drive the logic
        if (!itemStack.is(DNLItems.SPAWNER_HELMET.get())) return;
        if (player.getInventory().getArmor(3) != itemStack) return;

        // Overworked: HP penalty is now applied on summon in summonMob(), not every tick while wearing the helmet

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
                MobEffectInstance current = player.getEffect(MobEffects.REGENERATION);
                if (current == null || current.getDuration() <= 20 || current.getAmplifier() < 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false));
                }
            }
        }
    }

    private void summonMob(Level level, BlockPos entityPos, Player owner) {
        RandomSource randomSource = level.getRandom();
        double x = entityPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;
        double y = entityPos.getY() + randomSource.nextInt(3) - 1;
        double z = entityPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5;

        WhimperEntity whimper = DNLEntityTypes.WHIMPER.get().create(level);
        if (whimper == null) {
            return;
        }

        whimper.moveTo(x, y, z, 0.0F, 0.0F);
        whimper.setOwnerUUID(owner.getUUID());

        ItemStack helmetStack = owner.getInventory().getArmor(3);

        whimper.setSkin(resolveWhimperSkin(level, helmetStack));

        int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.GIGANTISM.get(), helmetStack);
        if (gigantismLevel > 0) {
            whimper.setGigantic(true);
        }

        // Prevent spawn if there is not enough free space for its (possibly gigantic) bounding box
        if (!level.noCollision(whimper)) {
            return;
        }

        // Particles only for successful spawns
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, x + 0.5F, y + 0.5F, z + 0.5F, 20, 0.3D, 0.3D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.FLAME, x + 0.5F, y + 0.5F, z + 0.5F, 10, 0.3D, 0.3D, 0.3D, 0.0D);
        }

        int overworkedLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.OVERWORKED.get(), helmetStack);
        whimper.setOverworkedLevel(overworkedLevel);
        if (overworkedLevel > 0) {
            whimper.applyOverworkedAttackSpeedBonus();
        }

        level.addFreshEntity(whimper);

        // Refresh owner penalty after the summon is actually alive in the world.
        if (level instanceof ServerLevel serverLevel) {
            OverworkedPenaltyUtil.refreshOwnerPenalty(serverLevel, owner);
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

    private static void cycleWhimperCosmeticMode(ItemStack stack) {
        String current = getWhimperCosmeticMode(stack);
        String next;

        if (current.equals(MODE_DEFAULT)) {
            next = MODE_LANTERN;
        } else if (current.equals(MODE_LANTERN)) {
            next = MODE_MIX; // optional
        } else {
            next = MODE_DEFAULT;
        }

        stack.getOrCreateTag().putString(TAG_WHIMPER_COSMETIC_MODE, next);
    }

    public static String getWhimperCosmeticMode(ItemStack stack) {
        String v = stack.getOrCreateTag().getString(TAG_WHIMPER_COSMETIC_MODE);
        return v.isEmpty() ? MODE_DEFAULT : v;
    }

    private static WhimperEntity.Skin resolveWhimperSkin(Level level, ItemStack helmetStack) {
        String mode = getWhimperCosmeticMode(helmetStack);

        if (MODE_LANTERN.equals(mode)) {
            return WhimperEntity.Skin.LANTERN;
        }

        if (MODE_MIX.equals(mode)) {
            return level.getRandom().nextBoolean()
                    ? WhimperEntity.Skin.LANTERN
                    : WhimperEntity.Skin.DEFAULT;
        }

        return WhimperEntity.Skin.DEFAULT;
    }
}