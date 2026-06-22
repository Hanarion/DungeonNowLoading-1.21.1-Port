package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.config.PvpConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.supporter.DNLSupporters;
import dev.hexnowloading.dungeonnowloading.util.OverworkedPenaltyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import java.util.UUID;

public class SpawnerArmorItem extends ArmorItem {

    private static final int DEFAULT_SUMMON_TICK = 200;
    private static final int EMPTY_AREA_RECHECK_TICK = 20;
    private static final int CROUCH_TICK_REDUCTION = 3;
    private static final int NORMAL_TICK_REDUCTION = 1;
    private static final int MAX_OWNED_WHIMPERS = 5;

    private static final int SPAWN_RANGE = 4;
    private static final double THREAT_CHECK_RADIUS = 8;
    private static final double PACK_BLESSING_RADIUS = 32.0D;

    private static final String TAG_SUMMON_TICK = "SummonTick";
    private static final String TAG_WHIMPER_COSMETIC_MODE = "WhimperCosmeticMode";

    private static final String MODE_DEFAULT = "default";
    private static final String MODE_LANTERN = "lantern";
    private static final String MODE_MIX = "mix";

    public SpawnerArmorItem(ArmorMaterial armorMaterial, Type slot) {
        super(armorMaterial, slot, new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(DNLItems.SPAWNER_HELMET.get())) {
            return InteractionResultHolder.fail(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (!clickedState.is(Blocks.LANTERN)) {
            return super.useOn(context);
        }

        if (!level.isClientSide && player != null && canUseLanternWhimperMode(player)) {
            cycleWhimperCosmeticMode(stack);
            String modeName = getWhimperCosmeticMode(stack);
            player.displayClientMessage(
                    Component.literal("Whimper Mode: " + capitalize(modeName)).withStyle(ChatFormatting.YELLOW),
                    true
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (!isEquippedSpawnerHelmet(player, stack)) return;
        if (!hasCorrectArmorOn(player)) return;

        tickSummonLogic(level, player, stack);
        applyPackBlessingEffect(level, player, stack);
    }

    private void tickSummonLogic(Level level, Player player, ItemStack helmetStack) {
        int summonTick = getSummonTick(helmetStack);

        if (summonTick > 0) {
            summonTick -= getSummonTickReduction(level, player);
            if (summonTick < 0) {
                summonTick = 0;
            }
        }

        if (summonTick <= 0) {
            boolean forcedByCrouch = player.isCrouching();
            boolean shouldSummon = forcedByCrouch || hasNearbyThreat(level, player);

            if (shouldSummon) {
                boolean summoned = summonMob(level, player.blockPosition(), player, helmetStack, forcedByCrouch);
                summonTick = summoned ? DEFAULT_SUMMON_TICK : EMPTY_AREA_RECHECK_TICK;
            } else {
                summonTick = EMPTY_AREA_RECHECK_TICK;
            }
        }

        setSummonTick(helmetStack, summonTick);
    }

    private int getSummonTickReduction(Level level, Player player) {
        if (player.isCrouching()) {
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        player.getX(), player.getY() + 1.0F, player.getZ(),
                        3,
                        0.7D, 1.0D, 0.7D,
                        0.0D
                );
            }
            return CROUCH_TICK_REDUCTION;
        }

        return NORMAL_TICK_REDUCTION;
    }

    private boolean hasNearbyThreat(Level level, Player player) {
        boolean hostileMobNearby = !level.getEntitiesOfClass(
                Monster.class,
                player.getBoundingBox().inflate(THREAT_CHECK_RADIUS),
                mob -> mob.isAlive()
        ).isEmpty();

        if (hostileMobNearby) {
            return true;
        }

        if (!PvpConfig.TOGGLE_PVP_MODE.get()) {
            return false;
        }

        return !level.getEntitiesOfClass(
                Player.class,
                player.getBoundingBox().inflate(THREAT_CHECK_RADIUS),
                other -> other != player
                        && !player.isAlliedTo(other)
                        && !other.isSpectator()
                        && !other.isCreative()
                        && other.isAlive()
        ).isEmpty();
    }

    private void applyPackBlessingEffect(Level level, Player player, ItemStack helmetStack) {
        int packBlessingLevel = EnchantmentHelper.getItemEnchantmentLevel(
                DNLEnchantments.holder(level, DNLEnchantments.PACK_BLESSING),
                helmetStack
        );

        if (packBlessingLevel <= 0) {
            return;
        }

        List<WhimperEntity> whimpers = level.getEntitiesOfClass(
                WhimperEntity.class,
                player.getBoundingBox().inflate(PACK_BLESSING_RADIUS),
                whimper -> whimper.isAlive() && playerOwnsWhimper(level, player, whimper)
        );

        if (whimpers.size() < 3) {
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.REGENERATION);
        if (current == null || current.getDuration() <= 20 || current.getAmplifier() < 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0, true, false));
        }
    }

    private boolean playerOwnsWhimper(Level level, Player player, WhimperEntity whimper) {
        UUID ownerUuid = whimper.getOwnerUUID();
        if (ownerUuid == null) {
            return false;
        }

        Player owner = level.getPlayerByUUID(ownerUuid);
        return owner != null && owner.equals(player);
    }

    private boolean summonMob(Level level, BlockPos origin, Player owner, ItemStack helmetStack, boolean forcedByCrouch) {
        if (countOwnedWhimpers(level, owner) >= MAX_OWNED_WHIMPERS) {
            return false;
        }

        RandomSource random = level.getRandom();

        double x = origin.getX() + (random.nextDouble() - random.nextDouble()) * SPAWN_RANGE + 0.5D;
        double y = origin.getY() + random.nextInt(3) - 1;
        double z = origin.getZ() + (random.nextDouble() - random.nextDouble()) * SPAWN_RANGE + 0.5D;

        WhimperEntity whimper = DNLEntityTypes.WHIMPER.get().create(level);
        if (whimper == null) {
            return false;
        }

        whimper.moveTo(x, y, z, 0.0F, 0.0F);
        whimper.setOwnerUUID(owner.getUUID());
        whimper.setSkin(resolveWhimperSkin(level, helmetStack));

        int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(level, DNLEnchantments.GIGANTISM), helmetStack);
        if (gigantismLevel > 0) {
            whimper.setGigantic(true);
        }

        if (!level.noCollision(whimper)) {
            return false;
        }

        int overworkedLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(level, DNLEnchantments.OVERWORKED), helmetStack);
        whimper.setOverworkedLevel(overworkedLevel);
        if (overworkedLevel > 0) {
            whimper.applyOverworkedAttackSpeedBonus();
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, x + 0.5D, y + 0.5D, z + 0.5D, 20, 0.3D, 0.3D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.FLAME, x + 0.5D, y + 0.5D, z + 0.5D, 10, 0.3D, 0.3D, 0.3D, 0.0D);
        }

        level.playSound(null, x, y, z, DNLSounds.WHIMPER_AMBIENT.get(), SoundSource.NEUTRAL, 1.0F, 1.5F);
        level.addFreshEntity(whimper);

        if (forcedByCrouch) {
            damageAllArmor(owner, 3);
        }

        if (level instanceof ServerLevel serverLevel) {
            OverworkedPenaltyUtil.refreshOwnerPenalty(serverLevel, owner);
        }

        return true;
    }

    private void damageAllArmor(Player player, int amount) {
        player.getInventory().getArmor(0).hurtAndBreak(amount, player, net.minecraft.world.entity.EquipmentSlot.FEET);
        player.getInventory().getArmor(1).hurtAndBreak(amount, player, net.minecraft.world.entity.EquipmentSlot.LEGS);
        player.getInventory().getArmor(2).hurtAndBreak(amount, player, net.minecraft.world.entity.EquipmentSlot.CHEST);
        player.getInventory().getArmor(3).hurtAndBreak(amount, player, net.minecraft.world.entity.EquipmentSlot.HEAD);
    }

    private boolean isEquippedSpawnerHelmet(Player player, ItemStack stack) {
        return stack.is(DNLItems.SPAWNER_HELMET.get()) && player.getInventory().getArmor(3) == stack;
    }

    private boolean canUseLanternWhimperMode(Player player) {
        UUID uuid = player.getUUID();
        return DNLSupporters.hasSkin(uuid, "whimper_lantern") || DNLSupporters.isSupporter(uuid);
    }

    private boolean hasCorrectArmorOn(Player player) {
        for (ItemStack armorStack : player.getInventory().armor) {
            if (!(armorStack.getItem() instanceof ArmorItem)) {
                return false;
            }
        }

        ArmorItem boots = (ArmorItem) player.getInventory().getArmor(0).getItem();
        ArmorItem leggings = (ArmorItem) player.getInventory().getArmor(1).getItem();
        ArmorItem chestplate = (ArmorItem) player.getInventory().getArmor(2).getItem();
        ArmorItem helmet = (ArmorItem) player.getInventory().getArmor(3).getItem();

        return helmet.getMaterial() == material
                && chestplate.getMaterial() == material
                && leggings.getMaterial() == material
                && boots.getMaterial() == material;
    }

    private int countOwnedWhimpers(Level level, Player player) {
        return level.getEntitiesOfClass(
                WhimperEntity.class,
                player.getBoundingBox().inflate(128.0D),
                whimper -> whimper.isAlive() && playerOwnsWhimper(level, player, whimper)
        ).size();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);

        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.spawner_armor.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.spawner_armor.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void cycleWhimperCosmeticMode(ItemStack stack) {
        String current = getWhimperCosmeticMode(stack);
        String next;

        if (MODE_DEFAULT.equals(current)) {
            next = MODE_LANTERN;
        } else if (MODE_LANTERN.equals(current)) {
            next = MODE_MIX;
        } else {
            next = MODE_DEFAULT;
        }

        stack.getOrCreateTag().putString(TAG_WHIMPER_COSMETIC_MODE, next);
    }

    public static String getWhimperCosmeticMode(ItemStack stack) {
        String value = stack.getOrCreateTag().getString(TAG_WHIMPER_COSMETIC_MODE);
        return value.isEmpty() ? MODE_DEFAULT : value;
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

    private static int getSummonTick(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_SUMMON_TICK)) {
            tag.putInt(TAG_SUMMON_TICK, DEFAULT_SUMMON_TICK);
        }
        return tag.getInt(TAG_SUMMON_TICK);
    }

    private static void setSummonTick(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(TAG_SUMMON_TICK, value);
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}