package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CopperDetonatorItem extends Item {
    private static final double TRIGGER_RADIUS = 32.0;
    private static final int SUMMON_COOLDOWN = 5;
    private static final int IGNITE_COOLDOWN_PER_CREEP = 100;
    public static final int MODE_SWITCH_TIMING = 10;

    public CopperDetonatorItem(Properties properties) {
        super(properties);
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
            if (consumeCopperBlockIfAvailable(player) && creepsInRange.size() < 3) {
                launchCreep(level, player);
                player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN);
                itemStack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(player1.getUsedItemHand()));
                player.swing(hand);
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

    private void launchCreep(Level level, Player player) {
        CopperCreepEntity creep = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (creep == null) return;

        double offset = 1.0;
        double launchX = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * offset;
        double launchY = player.getY() + player.getEyeHeight() * 0.6;
        double launchZ = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * offset;
        creep.moveTo(launchX, launchY, launchZ, player.getYRot(), player.getXRot());

        double velocity = 1.0;
        double motionX = -Math.sin(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        double motionY = -Math.sin(Math.toRadians(player.getXRot())) * velocity;
        double motionZ = Math.cos(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        creep.setDeltaMovement(motionX, motionY, motionZ);
        creep.setSummonerUUID(player.getUUID());

        level.addFreshEntity(creep);
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
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.right_click").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.copper_detonator.tooltip.right_click.description").withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}