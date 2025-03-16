package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.animation.ScorcherAnimation;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ScorcherItem extends Item implements DNLAnimatedItem<ScorcherItem.ScorcherAnimationState> {

    private static final String HEAT_TAG = "ScorcherHeat";

    public ScorcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime())) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            ItemAnimationState.startAndSendPacket(level, player, stack, ScorcherAnimationState.SCORCHER_STALLING.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STALLING.lengthInSeconds() * 20L), false, false);

            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int remainingUseDuration) {
        if (level.isClientSide) return;

        if (!(entity instanceof Player player)) return;


        int chargeTime = getUseDuration(itemStack) - remainingUseDuration;
        int overHeatedDuration = 160;

        boolean animCheck = ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), level.getGameTime());

        if (animCheck) {
            shootFlame(level, player, itemStack);

            int heat = getPlayerHeat(player);
            setPlayerHeat(player, heat + 1);

            if (heat >= 120) {
                ItemAnimationState.startAndSendPacket(level, (Player) player, itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() * 20L), false, true);
                setPlayerHeat(player, overHeatedDuration);
                player.getCooldowns().addCooldown(DNLItems.SCORCHER.get(), 160);
                player.getCooldowns().addCooldown(DNLItems.SOUL_SCORCHER.get(), 160);
                player.releaseUsingItem();
                return;
            }
        }

        float activeAnimDuration = ScorcherAnimation.SCORCHER_ACTIVATE.lengthInSeconds() * 20 - 1;
        float shootAnimDuration = ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20 + activeAnimDuration;


        if (ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        if (chargeTime == 0) {
            ItemAnimationState.startAndSendPacket(level, (Player) player, itemStack, ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_ACTIVATE.lengthInSeconds() * 20L), false, true);
        }

        if (chargeTime == activeAnimDuration) {
            ItemAnimationState.startAndSendPacket(level, (Player) player, itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20L), false, true);
        }

        if (chargeTime == shootAnimDuration) {
            ItemAnimationState.startAndSendPacket(level, (Player) player, itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() * 20L), false, true);
            setPlayerHeat(player, overHeatedDuration);
            ((Player) player).getCooldowns().addCooldown(this, 160);
            player.releaseUsingItem();
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        long gameTime = level.getGameTime();
        //decreaseHeat(level, entity, stack, gameTime, slot);
        if (level.isClientSide && !isSelected) {
            if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)) {
                if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ItemAnimationState.start(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);
                } else if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime)) {
                    ItemAnimationState.stopAll(stack);
                }
            }
        }
    }

    /*private void decreaseHeat(Level level, Entity entity, ItemStack stack, long gameTime, int slot) {
        if (!level.isClientSide && stack.getItem() instanceof ScorcherItem && entity instanceof Player player) {

            for (int i = 0; i < 9; i++) {
                ItemStack invStack = player.getInventory().getItem(i);

                if (invStack.is(stack.getItem()) && ItemAnimationState.isAnimating(invStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ScorcherItem.setHeatAndSendPacket(level, player, stack, slot, ScorcherItem.getHeatLevel(invStack));
                    return;
                }
            }

            ItemStack offhandStack = player.getOffhandItem();
            if (offhandStack.is(stack.getItem()) && ItemAnimationState.isAnimating(offhandStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                ScorcherItem.setHeatAndSendPacket(level, player, stack, slot, ScorcherItem.getHeatLevel(offhandStack));
                return;
            }

            float heat = getHeatLevel(stack);
            float heatDecayPerTick = 1.0f / (6.0f * 20);
            heat = Math.max(0.0F, heat - heatDecayPerTick);
            setHeatAndSendPacket(level, player, stack, slot, heat);
        }
    }*/

    private void shootFlame(Level level, LivingEntity player, ItemStack itemStack) {
        Vec3 eyePosition = player.getEyePosition(); // ✅ Get eye position like arrows
        Vec3 viewVector = player.getViewVector(1.0F); // ✅ Get forward direction
        Vec3 rightVector = new Vec3(-viewVector.z, 0, viewVector.x).normalize(); // ✅ Get right direction

        double rightOffset = 0.3; // ✅ Offset to the right
        double verticalOffset = -0.2; // ✅ Lowering the spawn position

        // Offset the spawn position to the right and slightly downward
        Vec3 spawnPosition = eyePosition
                .add(viewVector.scale(0.5))  // ✅ Move slightly forward
                .add(rightVector.scale(rightOffset))  // ✅ Move slightly to the right
                .add(0, verticalOffset, 0); // ✅ Drop it lower

        // 🔹 Adjust the target position to ensure it lands at the same spot
        Vec3 targetPosition = eyePosition.add(viewVector.scale(30))  // ✅ Original aim direction
                .add(0, -verticalOffset, 0);  // ✅ Adjust for lowered spawn point

        // Recalculate correct direction to hit the same landing position
        Vec3 correctedDirection = targetPosition.subtract(spawnPosition).normalize();

        // Create the flame projectile entity
        FlameProjectileEntity flame = new FlameProjectileEntity(player, level);
        flame.setOwner(player);
        flame.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);

        if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
            flame.setDamage(5.0F);
        } else {
            flame.setDamage(4.0F);
        }

        // Apply corrected velocity to ensure the projectile lands at the same target
        flame.setDeltaMovement(correctedDirection.scale(0.3)); // Adjust speed

        level.addFreshEntity(flame);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity player, int timeCharged) {

        if (level.isClientSide) return;

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        ItemAnimationState.startAndSendPacket(level, (Player) player, stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);

    }

    public static int getPlayerHeat(Player player) {
        return Services.DATA.getScorcherHeat(player);
    }

    public static void setPlayerHeat(Player player, int heat) {
        Services.DATA.setScorcherHeat(player, heat);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public static void setHeatLevel(ItemStack stack, float heatLevel) {
        stack.getOrCreateTag().putFloat(HEAT_TAG, heatLevel);
    }

    /*private static void setHeatAndSendPacket(Level level, Player player, ItemStack itemStack, int slot, float heatLevel) {
        setHeatLevel(itemStack, heatLevel);
        Services.NETWORK.sendToAllPlayers(new S2CScorcherHeatPacket(itemStack, player.getUUID(), slot, heatLevel), player.getServer());
    }*/

    private static void ensureUUID(ItemStack stack) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag()); // ✅ Create tag if missing
        }
        if (!stack.getTag().contains("ScorcherUUID")) {
            stack.getTag().putUUID("ScorcherUUID", UUID.randomUUID()); // ✅ Assign unique UUID
        }
    }

    public static float getHeatLevel(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getFloat(HEAT_TAG) : 0.0F;
    }

    @Override
    public Class<ScorcherAnimationState> getAnimationEnum() {
        return ScorcherAnimationState.class;
    }

    public enum ScorcherAnimationState implements DNLAnimationState {
        SCORCHER_ACTIVATED("scorcher_activated"),
        SCORCHER_SHOOT("scorcher_shoot"),
        SCORCHER_STOP("scorcher_stop"),
        SCORCHER_STALLING("scorcher_stalling"),
        SCORCHER_OVERHEAT("scorcher_overheat"),
        SCORCHER_BASE("scorcher_base");

        private final String name;

        ScorcherAnimationState(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
