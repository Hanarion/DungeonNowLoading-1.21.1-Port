package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLEnchantments;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.supporter.DNLSupporters;
import dev.hexnowloading.dungeonnowloading.util.OverworkedPenaltyUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CopperDetonatorItem extends Item {
    private static final double TRIGGER_RADIUS = 32.0;
    private static final int SUMMON_COOLDOWN = 5;
    private static final int IGNITE_COOLDOWN_PER_CREEP = 100;
    public static final int MODE_SWITCH_TIMING = 10;

    private static final String TAG_COSMETIC_MODE = "CosmeticMode";
    private static final String MODE_DEFAULT = "default";
    private static final String MODE_BUTLER = "butler";
    private static final String MODE_MIX = "mix";

    public CopperDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (blockState.is(Blocks.COCOA)) {
            if (!level.isClientSide && player != null && DNLSupporters.hasSkin(player.getUUID(), "copper_creep_butler")) {
                cycleCosmeticMode(stack);
                String modeName = getCosmeticMode(stack);
                String capitalizedMode = modeName.substring(0, 1).toUpperCase() + modeName.substring(1);
                player.displayClientMessage(Component.literal("Current Mode: " + capitalizedMode).withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    private void cycleCosmeticMode(ItemStack stack) {
        String current = getCosmeticMode(stack);
        String next;
        if (current.equals(MODE_DEFAULT)) {
            next = MODE_BUTLER;
        } else if (current.equals(MODE_BUTLER)) {
            next = MODE_MIX;
        } else {
            next = MODE_DEFAULT;
        }
        StackNbt.update(stack, t -> t.putString(TAG_COSMETIC_MODE, next));
    }

    public static String getCosmeticMode(ItemStack stack) {
        return StackNbt.getOrCreateTag(stack).getString(TAG_COSMETIC_MODE).isEmpty()
                ? MODE_DEFAULT
                : StackNbt.getOrCreateTag(stack).getString(TAG_COSMETIC_MODE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int remainingUseTick) {
        int usedTime = itemStack.getUseDuration() - remainingUseTick;
        if (usedTime == MODE_SWITCH_TIMING) {
            livingEntity.playSound(DNLSounds.COPPER_DETONATOR_READY.get());
        } else if (usedTime > MODE_SWITCH_TIMING && (usedTime - MODE_SWITCH_TIMING) % 20 == 0) {
            livingEntity.playSound(DNLSounds.COPPER_DETONATOR_BEEP.get());
        }
        super.onUseTick(level, livingEntity, itemStack, remainingUseTick);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack $$0) {
        return super.getUseAnimation($$0);
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int remainingTicks) {
        int usedTime = itemStack.getUseDuration() - remainingTicks;

        if (!(livingEntity instanceof Player player)) return;

        InteractionHand hand = player.getUsedItemHand();

        List<CopperCreepEntity> creepsInRange = findNearbyCreeps(player, TRIGGER_RADIUS);

        if (usedTime <= MODE_SWITCH_TIMING) {
            if (creepsInRange.size() < 3) {
                // First, check whether there is enough space to spawn (including gigantism size) *before* consuming resources.
                if (!canSpawnCreepHere(level, player, itemStack)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("Not enough space for summon").withStyle(ChatFormatting.RED), true);
                    }
                    player.getCooldowns().addCooldown(this, 20);
                    return;
                }

                // Only consume copper block after we know the spawn will succeed.
                if (consumeCopperBlockIfAvailable(player)) {
                    launchCreep(level, player, itemStack);
                    player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN);
                    itemStack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(player1.getUsedItemHand()));
                    player.swing(hand);
                }
            }
        } else {
            if (!creepsInRange.isEmpty()) {
                igniteCreeps(creepsInRange);
                player.getCooldowns().addCooldown(this, creepsInRange.size() * IGNITE_COOLDOWN_PER_CREEP);
                player.swing(hand);
            }
        }

        super.releaseUsing(itemStack, level, livingEntity, remainingTicks);
    }

    @Override
    public int getUseDuration(ItemStack itemStack) {
        return 72000;
    }

    private void launchCreep(Level level, Player player, ItemStack detonatorStack) {
        CopperCreepEntity creep = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (creep == null) return;

        double offset = 1.0;
        double launchX = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * offset;
        double launchY = player.getY() + player.getEyeHeight() * 0.6;
        double launchZ = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * offset;
        creep.moveTo(launchX, launchY, launchZ, player.getYRot(), player.getXRot());

        // Gigantism: if the detonator has the enchant, double the Copper Creep's size
        int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(player.level(), DNLEnchantments.GIGANTISM), detonatorStack);
        if (gigantismLevel > 0) {
            creep.setGigantic(true);
        }

        // Prevent spawn if there is not enough free space for its (possibly gigantic) bounding box
        if (!level.noCollision(creep)) {
            return;
        }

        double velocity = 1.0;
        double motionX = -Math.sin(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        double motionY = -Math.sin(Math.toRadians(player.getXRot())) * velocity;
        double motionZ = Math.cos(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        creep.setDeltaMovement(motionX, motionY, motionZ);
        creep.setSummonerUUID(player.getUUID());
        if (DNLSupporters.hasSkin(player.getUUID(), "copper_creep_butler")) {
            if (getCosmeticMode(detonatorStack).equals(MODE_MIX)) {
                if (Math.random() < 0.5F) {
                    creep.setCosmeticMode(MODE_BUTLER);
                } else {
                    creep.setCosmeticMode(MODE_DEFAULT);
                }
            } else {
                creep.setCosmeticMode(getCosmeticMode(detonatorStack));
            }
        }
        creep.setSkinValidation(true);

        int overworkedLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(player.level(), DNLEnchantments.OVERWORKED), detonatorStack);
        if (overworkedLevel > 0) {
            creep.setOverworkedLevel(overworkedLevel);
        }

        level.addFreshEntity(creep);

        // Apply/refresh owner HP penalty after the summon is actually alive in the world.
        if (overworkedLevel > 0 && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            OverworkedPenaltyUtil.refreshOwnerPenalty(serverLevel, player);
        }
    }

    private List<CopperCreepEntity> findNearbyCreeps(Player player, double radius) {
        return player.level()
                .getEntitiesOfClass(CopperCreepEntity.class, player.getBoundingBox().inflate(radius))
                .stream()
                .filter(creep -> !creep.isDefused() && !creep.isDeadOrDying() && creep.getSummonerUUID().filter(uuid -> uuid.equals(player.getUUID())).isPresent() && player.distanceToSqr(creep) <= radius * radius)
                .toList();
    }

    private boolean consumeCopperBlockIfAvailable(Player player) {
        if (player.isCreative()) return true;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.COPPER_BLOCK && stack.getCount() > 0) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void igniteCreeps(List<CopperCreepEntity> creeps) {
        creeps.forEach(CopperCreepEntity::ignite);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.right_click").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.right_click.description").withStyle(ChatFormatting.DARK_GREEN));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.hold_right_mouse_button").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.hold_right_mouse_button.description").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    private boolean canSpawnCreepHere(Level level, Player player, ItemStack detonatorStack) {
        CopperCreepEntity creep = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (creep == null) return false;

        double offset = 1.0;
        double launchX = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * offset;
        double launchY = player.getY() + player.getEyeHeight() * 0.6;
        double launchZ = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * offset;
        creep.moveTo(launchX, launchY, launchZ, player.getYRot(), player.getXRot());

        int gigantismLevel = EnchantmentHelper.getItemEnchantmentLevel(DNLEnchantments.holder(player.level(), DNLEnchantments.GIGANTISM), detonatorStack);
        if (gigantismLevel > 0) {
            creep.setGigantic(true);
        }

        return level.noCollision(creep);
    }
}