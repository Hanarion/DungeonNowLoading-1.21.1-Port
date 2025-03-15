package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.animation.ScorcherAnimation;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CScorcherHeatPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
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
        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime())) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            ItemAnimationState.startAndSendPacket(level, player, stack, ScorcherAnimationState.SCORCHER_STALLING.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STALLING.lengthInSeconds() * 20L), false, false);

            System.out.println(level + " : Has Overheated");
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
    public void onUseTick(Level level, LivingEntity player, ItemStack itemStack, int remainingUseDuration) {
        int chargeTime = getUseDuration(itemStack) - remainingUseDuration;
        float maxHeatDuration = ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds() * 20;
        float overHeatedDuration = ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() / ScorcherAnimation.SCORCHER_SHOOT.lengthInSeconds();
        float heatIncreasePerTick = 1.0f / maxHeatDuration;

        boolean animCheck = ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), level.getGameTime());

        if (animCheck) {
            if (!level.isClientSide) {
                shootFlame(level, player);
            }
            float heat = getHeatLevel(itemStack);
            heat = Math.min(1.0F, heat + heatIncreasePerTick);
            setHeatAndSendPacket(level, (Player) player, itemStack, heat);

            if (heat >= 1.0F) {
                ItemAnimationState.startAndSendPacket(level, (Player) player, itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_OVERHEAT.lengthInSeconds() * 20L), false, true);
                setHeatAndSendPacket(level, (Player) player, itemStack, overHeatedDuration);
                player.releaseUsingItem();
                return;
            }
        }

        float activeAnimDuration = ScorcherAnimation.SCORCHER_ACTIVATE.lengthInSeconds() * 20;
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
            setHeatAndSendPacket(level, (Player) player, itemStack, overHeatedDuration);
            player.releaseUsingItem();
        }

        /*if (level.isClientSide) {
            String clientAnim = ItemAnimationState.getCurrentAnimation(itemStack, level.getGameTime());
            float clientHeat = getHeatLevel(itemStack);
            System.out.print("Client: " + clientAnim + " / " + clientHeat);
        }

        if (!level.isClientSide) {
            String serverAnim = ItemAnimationState.getCurrentAnimation(itemStack, level.getGameTime());
            float serverHeat = getHeatLevel(itemStack);
            System.out.println(" | Server: " + serverAnim + " / " + serverHeat);
        }*/
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        long gameTime = level.getGameTime();
        if (!level.isClientSide && stack.getItem() instanceof ScorcherItem && entity instanceof Player player) {
            if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                float heat = getHeatLevel(stack);
                float heatDecayPerTick = 1.0f / (6.0f * 20);
                heat = Math.max(0.0F, heat - heatDecayPerTick);
                setHeatAndSendPacket(level, player, stack, heat);
            }
        }
        if (!isSelected) {
            if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)) {
                if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ItemAnimationState.startAndSendPacket(level, (Player) entity, stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);
                } else if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime)) {
                    ItemAnimationState.stopAll(stack);
                }
            }
        }
    }

    private void shootFlame(Level level, LivingEntity player) {
        Vec3 viewVector = player.getViewVector(1.0F);
        double viewDistance = 2.0F;

        FlameProjectileEntity flame = new FlameProjectileEntity(player, level);
        flame.setOwner(player);
        flame.setPos(
                player.getX() + viewVector.x * viewDistance,
                player.getY() + player.getBoundingBox().getYsize() / 2,
                player.getZ() + viewVector.z * viewDistance
        );
        flame.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.3F, 1.0F);

        level.addFreshEntity(flame);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity player, int timeCharged) {

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        ItemAnimationState.startAndSendPacket(level, (Player) player, stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimation.SCORCHER_STOP.lengthInSeconds() * 20L), false, true);

    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    public static void setHeatLevel(ItemStack stack, float heatLevel) {
        stack.getOrCreateTag().putFloat(HEAT_TAG, heatLevel);
    }

    private static void setHeatAndSendPacket(Level level, Player player, ItemStack itemStack, float heatLevel) {
        ensureUUID(itemStack);
        setHeatLevel(itemStack, heatLevel);
        if (!level.isClientSide) {
            UUID scorcherUUID = itemStack.getTag().getUUID("ScorcherUUID");
            Services.NETWORK.sendToAllPlayers(new S2CScorcherHeatPacket(scorcherUUID, player.getUUID(), heatLevel), player.getServer());
        }
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
