package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.item.animation.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ScorcherItem extends Item implements DNLAnimatedItem<ScorcherItem.ScorcherAnimationState> {

    private static final int CHARGE_TIME = 30;
    private static final long ANIMATION_DURATION = 20 * 1000L / 20L; // 1 second animation

    public ScorcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            setAnimationState(stack, ScorcherAnimationState.SCORCHER_ACTIVATED);
            ItemAnimationState.startAnimation(stack, level.getGameTime(), ANIMATION_DURATION, false);
            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    public void updateAnimation(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) return;

        if (!ItemAnimationState.isAnimating(stack)) {
            setAnimationState(stack, ScorcherAnimationState.SCORCHER_IDLE);
        }
    }

    public float getAnimationProgress(ItemStack stack, Level level) {
        return ItemAnimationState.getProgress(stack, level.getGameTime());
    }

    @Override
    public Class<ScorcherAnimationState> getAnimationEnum() {
        return ScorcherAnimationState.class;
    }

    public enum ScorcherAnimationState implements DNLAnimationState {
        SCORCHER_ACTIVATED("scorcher_activated"),
        SCORCHER_IDLE("scorcher_idle");

        private final String name;

        ScorcherAnimationState(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public static ScorcherAnimationState fromString(String animationName) {
            for (ScorcherAnimationState state : values()) {
                if (state.name.equals(animationName)) {
                    return state;
                }
            }
            throw new IllegalArgumentException("Invalid animation state: " + animationName);
        }
    }
}
