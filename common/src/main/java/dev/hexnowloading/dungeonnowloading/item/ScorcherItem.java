package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.animation.ScorcherAnimation;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CScorcherHeatPacket;
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
    private static final String HEAT_TIME_STAMP = "ScorcherTimeStamp";

    public ScorcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime())) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            if (!level.isClientSide) {
                int slot = player.getInventory().selected;
                ItemAnimationState.startAndSendPacket(player, stack, slot, ScorcherAnimationState.SCORCHER_STALLING.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STALLING.lengthInSeconds() * 20L), false, false);
            }

            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack $$0) {
        return super.getUseAnimation($$0);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int remainingUseDuration) {

        if (level.isClientSide) return;

        if (!(entity instanceof Player player)) return;

        int slot = -1;

        if (player.getMainHandItem() == itemStack) {
            slot = player.getInventory().selected;
        } else if (player.getOffhandItem() == itemStack) {
            slot = 40;
        }

        int chargeTime = getUseDuration(itemStack) - remainingUseDuration;
        long gameTime = player.level().getGameTime();
        float overHeatedDuration = ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() / ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds();

        float activeAnimDuration = ScorcherAnimation.SCORCHER_ACTIVATE.lengthInSeconds() * 20 - 1;
        float shootAnimDuration = ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20 + activeAnimDuration;


        if (ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        if (chargeTime == 0) {
            ItemAnimationState.startAndSendPacket(player, itemStack, slot, ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime, (long) (ScorcherAnimation.SCORCHER_ACTIVATE.lengthInSeconds() * 20L), false, true);
        }

        if (chargeTime == activeAnimDuration) {
            ItemAnimationState.startAndSendPacket(player, itemStack, slot, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime, (long) (ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20L), false, true);
        }

        if (chargeTime == shootAnimDuration) {
            ItemAnimationState.startAndSendPacket(player, itemStack, slot, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime, (long) (ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() * 20L), false, true);
            setHeatAndSendPacket(level, (Player) player, itemStack, slot, overHeatedDuration, gameTime);
            //((Player) player).getCooldowns().addCooldown(this, 160);
            player.releaseUsingItem();
        }

        float maxHeatDuration = ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20;
        float heatIncreasePerTick = 1.0f / maxHeatDuration;

        if (ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
            System.out.println("SHOOTING : " + gameTime);

            shootFlame(level, player, itemStack);

            float heat = getHeatLevel(itemStack);
            heat = Math.min(1.0F, heat + heatIncreasePerTick);
            setHeatAndSendPacket(level, (Player) player, itemStack, slot, heat, gameTime);

            if (heat >= 1.0F) {
                ItemAnimationState.startAndSendPacket(player, itemStack, slot, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime, (long) (ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() * 20L), false, true);
                setHeatAndSendPacket(level, (Player) player, itemStack, slot, overHeatedDuration, gameTime);
                //((Player) player).getCooldowns().addCooldown(this, 160);
                player.releaseUsingItem();
                return;
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        long gameTime = level.getGameTime();

        if (!level.isClientSide && stack.getItem() instanceof ScorcherItem && entity instanceof Player player) {
            long storedGameTime = getTimeStamp(stack);


            // If timestamp is outdated, recalculate heat decay before updating
            if (storedGameTime != gameTime) {
                float heat = getHeatLevel(stack);
                long timeElapsed = gameTime - storedGameTime;
                float heatDecayPerTick = 1.0f / (6.0f * 20); // Same decay rate as in inventory

                // Apply heat decay over the time elapsed
                heat = Math.max(0.0F, heat - (timeElapsed * heatDecayPerTick));

                // Update the heat value and timestamp in NBT

                if (ItemAnimationState.isAnimatingOrHanging(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    System.out.println("STOPPED");
                    ItemAnimationState.stopAllAndSendPacket(player, stack);
                }
                setHeatAndSendPacket(level, player, stack, slot, heat, gameTime);
            }

        }
        if (level.isClientSide && !isSelected) {
            if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)) {
                if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ItemAnimationState.start(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime, (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);
                } else if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime)) {
                    ItemAnimationState.stopAll(stack);
                }
            }
        }
    }

    private void decreaseHeat(Level level, Entity entity, ItemStack stack, long gameTime, int slot) {
        if (!level.isClientSide && stack.getItem() instanceof ScorcherItem && entity instanceof Player player) {

            for (int i = 0; i < 9; i++) {
                ItemStack invStack = player.getInventory().getItem(i);

                if (invStack.is(stack.getItem()) && ItemAnimationState.isAnimating(invStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ScorcherItem.setHeatAndSendPacket(level, player, stack, slot, ScorcherItem.getHeatLevel(invStack), gameTime);
                    return;
                }
            }

            ItemStack offhandStack = player.getOffhandItem();
            if (offhandStack.is(stack.getItem()) && ItemAnimationState.isAnimating(offhandStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                ScorcherItem.setHeatAndSendPacket(level, player, stack, slot, ScorcherItem.getHeatLevel(offhandStack), gameTime);
                return;
            }

            float heat = getHeatLevel(stack);
            float heatDecayPerTick = 1.0f / (6.0f * 20);
            heat = Math.max(0.0F, heat - heatDecayPerTick);
            setHeatAndSendPacket(level, player, stack, slot, heat, gameTime);
        }
    }

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
            flame.setSoul(true);
        } else {
            flame.setDamage(4.0F);
        }

        // Apply corrected velocity to ensure the projectile lands at the same target
        flame.setDeltaMovement(correctedDirection.scale(0.3)); // Adjust speed

        level.addFreshEntity(flame);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {

        if (level.isClientSide) return;

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        if (!(entity instanceof Player player)) return;

        int slot = player.getInventory().selected;

        ItemAnimationState.startAndSendPacket(player, stack, slot, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);

    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public static void setHeatLevel(ItemStack stack, float heatLevel, long timeStamp) {
        stack.getOrCreateTag().putFloat(HEAT_TAG, heatLevel);
        stack.getOrCreateTag().putLong(HEAT_TIME_STAMP, timeStamp);
    }

    private static void setHeatAndSendPacket(Level level, Player player, ItemStack itemStack, int slot, float heatLevel, long timeStamp) {
        setHeatLevel(itemStack, heatLevel, timeStamp);
        Services.NETWORK.sendToAllPlayers(new S2CScorcherHeatPacket(itemStack, player.getUUID(), slot, heatLevel, timeStamp), player.getServer());
    }

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

    public static long getTimeStamp(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getLong(HEAT_TIME_STAMP) : 0L;
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
