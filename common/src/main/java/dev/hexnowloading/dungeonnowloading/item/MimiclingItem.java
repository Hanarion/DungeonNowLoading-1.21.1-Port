package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MimiclingItem extends Item {
    private static final String FORM_TAG = "MimiclingForm";
    private static final String TRANSITION_START_TAG = "MimiclingTransitionStart";
    private static final String TRANSITION_FROM_TAG = "MimiclingTransitionFrom";
    private static final String TRANSITION_TO_TAG = "MimiclingTransitionTo";
    private static final String FORM_BASE = "base";
    private static final String FORM_PICKAXE = "pickaxe";
    private static final String FORM_AXE = "axe";
    private static final String FORM_SHOVEL = "shovel";
    private static final String FORM_HOE = "hoe";
    private static final String FORM_SWORD = "sword";
    private static final int TRANSITION_DURATION = 20;
    private static final int FIRST_PHASE_TICKS = 10;

    public MimiclingItem(Properties properties) {
        super(properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        String form = getStoredForm(stack);
        if (FORM_SWORD.equals(form)) {
            if (state.is(Blocks.COBWEB)) {
                return 15.0F;
            }
            return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
        }

        return isMineableByForm(state, form) ? Tiers.DIAMOND.getSpeed() : 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(Blocks.COBWEB)
                || state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || state.is(BlockTags.MINEABLE_WITH_HOE);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            int damage = FORM_SWORD.equals(getStoredForm(stack)) ? 2 : 1;
            stack.hurtAndBreak(damage, entity, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(FORM_SWORD.equals(getStoredForm(stack)) ? 1 : 2, attacker, livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        String form = getStoredForm(context.getItemInHand());
        if (FORM_AXE.equals(form)) {
            return Items.DIAMOND_AXE.useOn(context);
        }
        if (FORM_SHOVEL.equals(form)) {
            return Items.DIAMOND_SHOVEL.useOn(context);
        }
        if (FORM_HOE.equals(form)) {
            return Items.DIAMOND_HOE.useOn(context);
        }

        return super.useOn(context);
    }

    public static boolean tryTransformToForm(ItemStack stack, Player player, InteractionHand hand, String targetForm) {
        Level level = player.level();
        if (!(stack.getItem() instanceof MimiclingItem) || !isValidForm(targetForm) || isTransitioning(stack, level.getGameTime())) {
            return false;
        }

        String currentForm = getCurrentForm(stack, level.getGameTime());
        if (targetForm.equals(currentForm)) {
            return false;
        }

        startTransition(stack, currentForm, targetForm, level.getGameTime());
        playTransformSound(level, player);
        player.swing(hand);
        return true;
    }

    public static String getBestFormFor(BlockState state) {
        if (state.is(Blocks.COBWEB)) {
            return FORM_SWORD;
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return FORM_PICKAXE;
        }
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return FORM_AXE;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return FORM_SHOVEL;
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return FORM_HOE;
        }
        return FORM_BASE;
    }

    public static String getSwordForm() {
        return FORM_SWORD;
    }

    public static boolean isValidForm(String form) {
        return FORM_BASE.equals(form) || FORM_PICKAXE.equals(form) || FORM_AXE.equals(form) || FORM_SHOVEL.equals(form) || FORM_HOE.equals(form) || FORM_SWORD.equals(form);
    }

    private static void startTransition(ItemStack stack, String fromForm, String toForm, long gameTime) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(FORM_TAG, toForm);
        tag.putString(TRANSITION_FROM_TAG, fromForm);
        tag.putString(TRANSITION_TO_TAG, toForm);
        tag.putLong(TRANSITION_START_TAG, gameTime);
        applyAttributeModifiers(stack, toForm);
    }

    private static void playTransformSound(Level level, Player player) {
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 1.1F);
        }
    }

    public static boolean isTransitionFrame(ItemStack stack, long gameTime, String form, int frame, int frameCount) {
        if (!isTransitioning(stack, gameTime)) {
            return false;
        }

        long elapsed = gameTime - stack.getTag().getLong(TRANSITION_START_TAG);
        String currentVisualForm;
        int currentFrame;

        if (elapsed < FIRST_PHASE_TICKS) {
            currentVisualForm = getTransitionFrom(stack);
            currentFrame = scaleFrame(elapsed, FIRST_PHASE_TICKS, frameCount);
        } else if (elapsed == FIRST_PHASE_TICKS) {
            return false;
        } else {
            currentVisualForm = getTransitionTo(stack);
            currentFrame = (frameCount - 1) - scaleFrame(elapsed - FIRST_PHASE_TICKS - 1, TRANSITION_DURATION - FIRST_PHASE_TICKS - 1, frameCount);
        }

        return currentVisualForm.equals(form) && currentFrame == frame;
    }

    public static boolean isMucus(ItemStack stack, long gameTime) {
        return isTransitioning(stack, gameTime) && gameTime - stack.getTag().getLong(TRANSITION_START_TAG) == FIRST_PHASE_TICKS;
    }

    public static boolean isForm(ItemStack stack, long gameTime, String form) {
        return !isTransitioning(stack, gameTime) && getCurrentForm(stack, gameTime).equals(form);
    }

    private static int scaleFrame(long elapsed, int durationTicks, int frameCount) {
        if (frameCount <= 1 || durationTicks <= 1) {
            return 0;
        }

        return (int) Math.min((elapsed * frameCount) / durationTicks, frameCount - 1);
    }

    private static boolean isTransitioning(ItemStack stack, long gameTime) {
        if (!hasTransitionData(stack)) {
            return false;
        }

        long elapsed = gameTime - stack.getTag().getLong(TRANSITION_START_TAG);
        if (elapsed >= 0 && elapsed < TRANSITION_DURATION) {
            return true;
        }

        return false;
    }

    private static String getCurrentForm(ItemStack stack, long gameTime) {
        if (hasTransitionData(stack)) {
            long elapsed = gameTime - stack.getTag().getLong(TRANSITION_START_TAG);
            return elapsed >= TRANSITION_DURATION ? getTransitionTo(stack) : getTransitionFrom(stack);
        }

        if (!stack.hasTag()) {
            return FORM_BASE;
        }

        String form = stack.getTag().getString(FORM_TAG);
        return form.isEmpty() ? FORM_BASE : form;
    }

    private static boolean hasTransitionData(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TRANSITION_START_TAG) && stack.getTag().contains(TRANSITION_TO_TAG);
    }

    private static String getTransitionFrom(ItemStack stack) {
        String form = stack.getOrCreateTag().getString(TRANSITION_FROM_TAG);
        return form.isEmpty() ? FORM_BASE : form;
    }

    private static String getTransitionTo(ItemStack stack) {
        String form = stack.getOrCreateTag().getString(TRANSITION_TO_TAG);
        return form.isEmpty() ? FORM_BASE : form;
    }

    private static String getStoredForm(ItemStack stack) {
        if (!stack.hasTag()) {
            return FORM_BASE;
        }

        String form = stack.getTag().getString(FORM_TAG);
        return form.isEmpty() ? FORM_BASE : form;
    }

    private static boolean isMineableByForm(BlockState state, String form) {
        return FORM_PICKAXE.equals(form) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || FORM_AXE.equals(form) && state.is(BlockTags.MINEABLE_WITH_AXE)
                || FORM_SHOVEL.equals(form) && state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || FORM_HOE.equals(form) && state.is(BlockTags.MINEABLE_WITH_HOE);
    }

    private static void applyAttributeModifiers(ItemStack stack, String form) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("AttributeModifiers");

        if (FORM_BASE.equals(form)) {
            return;
        }

        double attackDamage;
        double attackSpeed;
        if (FORM_SWORD.equals(form)) {
            attackDamage = 6.0D;
            attackSpeed = -2.4D;
        } else if (FORM_PICKAXE.equals(form)) {
            attackDamage = 4.0D;
            attackSpeed = -2.8D;
        } else if (FORM_AXE.equals(form)) {
            attackDamage = 8.0D;
            attackSpeed = -3.0D;
        } else if (FORM_SHOVEL.equals(form)) {
            attackDamage = 4.5D;
            attackSpeed = -3.0D;
        } else if (FORM_HOE.equals(form)) {
            attackDamage = 0.0D;
            attackSpeed = 0.0D;
        } else {
            return;
        }

        ListTag modifiers = new ListTag();
        modifiers.add(createModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", attackDamage, AttributeModifier.Operation.ADDITION)));
        modifiers.add(createModifier(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION)));
        tag.put("AttributeModifiers", modifiers);
    }

    private static CompoundTag createModifier(Attribute attribute, AttributeModifier modifier) {
        CompoundTag tag = modifier.save();
        tag.putString("AttributeName", net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString());
        tag.putString("Slot", EquipmentSlot.MAINHAND.getName());
        return tag;
    }
}
