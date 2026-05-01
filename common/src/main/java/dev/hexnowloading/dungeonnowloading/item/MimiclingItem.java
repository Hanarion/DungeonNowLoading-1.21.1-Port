package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MimiclingItem extends Item implements MimiclingFormItem {
    private static final String FORM_TAG = "MimiclingForm";
    private static final String TRANSITION_START_TAG = "MimiclingTransitionStart";
    private static final String TRANSITION_FROM_TAG = "MimiclingTransitionFrom";
    private static final String TRANSITION_TO_TAG = "MimiclingTransitionTo";
    private static final String STORED_ITEMS_TAG = "MimiclingItems";
    private static final String SELECTED_SLOT_TAG = "MimiclingSelectedSlot";
    private static final String SELECTED_FOOD_SLOT_TAG = "MimiclingSelectedFoodSlot";
    private static final String CHEWING_START_TAG = "MimiclingChewingStart";
    private static final String CAPACITY_TAG = "MimiclingCapacity";
    private static final String MUCUS_FED_TAG = "MimiclingMucusFed";
    private static final String TEMPORARY_INVENTORY_FORM_TAG = "MimiclingTemporaryInventoryForm";
    private static final String FORM_BASE = "base";
    private static final String FORM_PICKAXE = "pickaxe";
    private static final String FORM_AXE = "axe";
    private static final String FORM_SHOVEL = "shovel";
    private static final String FORM_HOE = "hoe";
    private static final String FORM_SWORD = "sword";
    private static final int MAX_STORED_ITEMS = 5;
    private static final int INITIAL_CAPACITY = 2;
    private static final int MUCUS_PER_CAPACITY_UPGRADE = 2;
    private static final int MUCUS_LOCKED_HEAL_AMOUNT = 10;
    private static final int MUCUS_UNLOCKED_HEAL_AMOUNT = 100;
    private static final int TRANSITION_DURATION = 20;
    private static final int FIRST_PHASE_TICKS = 10;
    private static final int CHEWING_FRAME_COUNT = 15;
    private static final int CHEWING_TICKS_PER_FRAME = 2;
    private static final String[] BLOCK_TIE_BREAKER_FORMS = {FORM_SWORD, FORM_HOE, FORM_SHOVEL, FORM_AXE, FORM_PICKAXE};
    private static final String[] COMBAT_FALLBACK_FORMS = {FORM_AXE, FORM_PICKAXE, FORM_SHOVEL, FORM_HOE};
    private final String form;

    public MimiclingItem(Properties properties) {
        this(properties, FORM_BASE);
    }

    public MimiclingItem(Properties properties, String form) {
        super(properties);
        this.form = isValidForm(form) ? form : FORM_BASE;
    }

    @Override
    public String getMimiclingForm() {
        return form;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction != ClickAction.SECONDARY || !canUseStorage(stack)) {
            return false;
        }

        ItemStack slotStack = slot.getItem();
        if (slotStack.isEmpty()) {
            removeSelected(stack).ifPresent(removed -> {
                playRemoveOneSound(player);
                ItemStack remainder = slot.safeInsert(removed);
                if (!remainder.isEmpty()) {
                    storeInDedicatedSlot(stack, remainder);
                }
            });
            return true;
        }

        if (isFeedableTool(slotStack) && slot.allowModification(player) && canAcceptFeed(stack, slotStack)) {
            ItemStack taken = slot.safeTake(1, 1, player);
            if (!taken.isEmpty()) {
                List<ItemStack> removed = feedOne(stack, taken, getSelectedFoodReplacementSlot(stack, taken));
                startChewing(stack, player.level().getGameTime());
                giveReturnedFeedItems(player, slot, removed);
                playInsertSound(player);
            }
            return true;
        }

        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack carriedStack, Slot slot, ClickAction clickAction, Player player, SlotAccess carriedSlot) {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player) || !canUseStorage(stack)) {
            return false;
        }

        if (carriedStack.isEmpty()) {
            removeSelected(stack).ifPresent(removed -> {
                playRemoveOneSound(player);
                carriedSlot.set(removed);
            });
            return true;
        }

        if (!isFeedableTool(carriedStack)) {
            return true;
        }

        if (!canAcceptFeed(stack, carriedStack)) {
            return true;
        }

        ItemStack inserted = carriedStack.copyWithCount(1);
        List<ItemStack> removed = feedOne(stack, inserted, getSelectedFoodReplacementSlot(stack, inserted));
        startChewing(stack, player.level().getGameTime());
        carriedStack.shrink(1);
        carriedSlot.set(carriedStack);
        giveReturnedFeedItems(player, removed);
        playInsertSound(player);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !canUseStorage(stack)) {
            return super.use(level, player, hand);
        }

        ItemStack foodStack = player.getOffhandItem();
        if (!isFeedableFood(foodStack) || !canAcceptFeed(stack, foodStack)) {
            return super.use(level, player, hand);
        }

        if (!level.isClientSide) {
            ItemStack inserted = foodStack.copyWithCount(1);
            List<ItemStack> returnedItems = feedOne(stack, inserted, getSelectedFoodReplacementSlot(stack, inserted));
            startChewing(stack, level.getGameTime());
            foodStack.shrink(1);
            giveReturnedFeedItems(player, returnedItems);
            playInsertSound(player);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        ItemStack storedTool = getStoredToolForCurrentForm(stack);
        return storedTool.isEmpty() ? super.getDestroySpeed(stack, state) : storedTool.getDestroySpeed(state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && !state.isAir()) {
            int damage = FORM_SWORD.equals(getStoredForm(stack)) ? 2 : 1;
            MimiclingFoodEffects.onBreak(stack, level, pos, state, entity);
            if (state.getDestroySpeed(level, pos) != 0.0F) {
                applyDurabilityDamage(stack, entity, damage);
            }
            MimiclingFoods.consumeUsage(stack);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyDurabilityDamage(stack, attacker, FORM_SWORD.equals(getStoredForm(stack)) ? 1 : 2);
        if (!attacker.level().isClientSide) {
            MimiclingFoodEffects.onAttack(stack, target, attacker);
            MimiclingFoods.consumeUsage(stack);
        }
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack storedTool = getStoredToolForCurrentForm(context.getItemInHand());
        if (!storedTool.isEmpty() && (storedTool.getItem() instanceof AxeItem || storedTool.getItem() instanceof ShovelItem || storedTool.getItem() instanceof HoeItem)) {
            return storedTool.getItem().useOn(context);
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            removeStaleAttributeModifierTags(stack);
            syncActiveEnchantmentTags(stack, getStoredForm(stack));
            MimiclingFoodEffects.tickHeld(stack, level, entity);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ItemStack storedTool = getStoredToolForCurrentForm(stack);
        return !stack.getEnchantmentTags().isEmpty() || !storedTool.isEmpty() && storedTool.hasFoil();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return MimiclingFormItem.getMimiclingBarColor(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, components, tooltipFlag);
        if (canUseStorage(stack)) {
            components.add(Component.translatable("item.dungeonnowloading.mimicling.tooltip.capacity", getStoredItemCount(stack), getStorageCapacity(stack)).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!canUseStorage(stack)) {
            return Optional.empty();
        }

        return getTooltipImage(stack, -1);
    }

    public static Optional<TooltipComponent> getTooltipImageForCarried(ItemStack stack, ItemStack carriedStack) {
        if (!canUseStorage(stack)) {
            return Optional.empty();
        }

        ItemStack previewFood = isFeedableFood(carriedStack) ? carriedStack.copyWithCount(1) : ItemStack.EMPTY;
        return getTooltipImage(
                stack,
                previewFood,
                MimiclingFoods.getPreviewDescription(previewFood),
                getCarriedFoodHighlightSlot(stack, carriedStack),
                !isFeedableFood(carriedStack)
        );
    }

    private static Optional<TooltipComponent> getTooltipImage(ItemStack stack, int selectedFoodSlot) {
        return getTooltipImage(stack, selectedFoodSlot, true);
    }

    private static Optional<TooltipComponent> getTooltipImage(ItemStack stack, int selectedFoodSlot, boolean showSelectedToolSlot) {
        NonNullList<ItemStack> contents = getTooltipContents(stack);
        return Optional.of(new MimiclingTooltip(contents, getTooltipActiveFoods(stack), showSelectedToolSlot ? getSelectedSlot(stack) : -1, selectedFoodSlot, getStorageCapacity(stack)));
    }

    private static Optional<TooltipComponent> getTooltipImage(ItemStack stack, ItemStack previewFood, List<String> previewFoodLines, int selectedFoodSlot, boolean showSelectedToolSlot) {
        NonNullList<ItemStack> contents = getTooltipContents(stack);
        return Optional.of(new MimiclingTooltip(contents, getTooltipActiveFoods(stack), previewFood, previewFoodLines, showSelectedToolSlot ? getSelectedSlot(stack) : -1, selectedFoodSlot, getStorageCapacity(stack)));
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getStoredItems(itemEntity.getItem()));
    }

    public static boolean tryTransformToForm(ItemStack stack, Player player, InteractionHand hand, String targetForm) {
        Level level = player.level();
        if (!canTransformToForm(stack, targetForm) || isTransitioning(stack, level.getGameTime())) {
            return false;
        }

        String currentForm = getCurrentForm(stack, level.getGameTime());
        if (targetForm.equals(currentForm)) {
            return false;
        }

        ItemStack transformed = copyWithForm(stack, targetForm);
        startTransition(transformed, currentForm, targetForm, level.getGameTime());
        player.setItemInHand(hand, transformed);
        playTransformSound(level, player);
        player.swing(hand);
        return true;
    }

    public static boolean tryTransformBrokenToolFormToBase(ItemStack stack, LivingEntity entity) {
        String currentForm = getStoredForm(stack);
        if (!(stack.getItem() instanceof MimiclingFormItem) || FORM_BASE.equals(currentForm)) {
            return false;
        }

        ItemStack transformed = copyWithForm(stack, FORM_BASE);
        transformed.setDamageValue(transformed.getMaxDamage());
        startTransition(transformed, currentForm, FORM_BASE, entity.level().getGameTime());
        if (!replaceHeldOrEquippedStack(stack, transformed, entity)) {
            return false;
        }

        playTransformSound(entity.level(), entity);
        return true;
    }

    public static boolean tryTemporarilyOpenForInventoryFeed(ItemStack stack, ItemStack carriedStack, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction != ClickAction.SECONDARY) {
            return false;
        }

        return tryTemporarilyOpenForInventoryFeed(stack, carriedStack, slot, player);
    }

    public static boolean tryTemporarilyOpenForInventoryFeed(ItemStack stack, ItemStack carriedStack, Slot slot, Player player) {
        if (carriedStack.isEmpty() || !isFeedableTool(carriedStack) || !canAcceptFeed(stack, carriedStack)) {
            return false;
        }

        return tryTemporarilyOpenForInventoryFeed(stack, slot, player);
    }

    public static boolean tryTemporarilyOpenForInventoryFeed(ItemStack stack, Slot slot, Player player) {
        if (!slot.allowModification(player)) {
            return false;
        }

        String currentForm = getStoredForm(stack);
        if (!(stack.getItem() instanceof MimiclingFormItem) || FORM_BASE.equals(currentForm)) {
            return false;
        }

        ItemStack opened = copyWithForm(stack, FORM_BASE);
        CompoundTag tag = opened.getOrCreateTag();
        tag.putString(TEMPORARY_INVENTORY_FORM_TAG, currentForm);
        startTransition(opened, currentForm, FORM_BASE, player.level().getGameTime());
        slot.set(opened);
        slot.setChanged();
        playTransformSound(player.level(), player);
        return true;
    }

    public static ItemStack restoreTemporaryInventoryForm(ItemStack stack) {
        return restoreTemporaryInventoryForm(stack, 0L, false);
    }

    public static ItemStack restoreTemporaryInventoryForm(ItemStack stack, long gameTime) {
        return restoreTemporaryInventoryForm(stack, gameTime, true);
    }

    private static ItemStack restoreTemporaryInventoryForm(ItemStack stack, long gameTime, boolean animate) {
        if (!isBaseStorageForm(stack) || !stack.hasTag() || !stack.getTag().contains(TEMPORARY_INVENTORY_FORM_TAG)) {
            return stack;
        }

        String originalForm = stack.getTag().getString(TEMPORARY_INVENTORY_FORM_TAG);
        if (!canTransformToForm(stack, originalForm)) {
            stack.getTag().remove(TEMPORARY_INVENTORY_FORM_TAG);
            stack.getTag().putString(FORM_TAG, FORM_BASE);
            syncActiveEnchantmentTags(stack, FORM_BASE);
            return stack;
        }

        ItemStack restored = copyWithForm(stack, originalForm);
        CompoundTag tag = restored.getOrCreateTag();
        tag.remove(TEMPORARY_INVENTORY_FORM_TAG);
        if (animate) {
            startTransition(restored, FORM_BASE, originalForm, gameTime);
        } else {
            tag.putString(FORM_TAG, originalForm);
            clearTransitionData(restored);
            removeStaleAttributeModifierTags(restored);
            syncActiveEnchantmentTags(restored, originalForm);
        }
        return restored;
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

    public static String getBestFormFor(ItemStack stack, BlockState state) {
        if (isHoeHarvestCropBlock(state) && !getStoredToolForForm(stack, FORM_HOE).isEmpty()) {
            return FORM_HOE;
        }

        String cycledUseForm = getCycledUseFormFor(stack, state);
        if (cycledUseForm != null) {
            return cycledUseForm;
        }

        String bestForm = FORM_BASE;
        int bestTierLevel = Integer.MIN_VALUE;
        int bestTieBreaker = Integer.MAX_VALUE;

        for (String form : BLOCK_TIE_BREAKER_FORMS) {
            ItemStack storedTool = getStoredToolForForm(stack, form);
            if (storedTool.isEmpty() || !isFormEffectiveForBlock(form, state)) {
                continue;
            }

            int tierLevel = getToolTierLevel(storedTool);
            int tieBreaker = getBlockTieBreaker(form);
            if (tierLevel > bestTierLevel || tierLevel == bestTierLevel && tieBreaker < bestTieBreaker) {
                bestForm = form;
                bestTierLevel = tierLevel;
                bestTieBreaker = tieBreaker;
            }
        }

        return bestForm;
    }

    public static String getCycledUseFormFor(ItemStack stack, BlockState state) {
        if (state.is(DNLTags.MIMICLING_CAMPFIRE_AXE_SHOVEL_CYCLE)) {
            return getCycledUseForm(stack, FORM_AXE, FORM_SHOVEL);
        }
        if (state.is(DNLTags.MIMICLING_WAXED_PICKAXE_AXE_CYCLE)) {
            return getCycledUseForm(stack, FORM_PICKAXE, FORM_AXE);
        }
        if (state.is(DNLTags.MIMICLING_SHOVEL_HOE_CYCLE)) {
            return getCycledUseForm(stack, FORM_SHOVEL, FORM_HOE);
        }

        return null;
    }

    private static String getCycledUseForm(ItemStack stack, String priorityForm, String alternateForm) {
        boolean hasPriority = !getStoredToolForForm(stack, priorityForm).isEmpty();
        boolean hasAlternate = !getStoredToolForForm(stack, alternateForm).isEmpty();
        if (!hasPriority && !hasAlternate) {
            return FORM_BASE;
        }

        String currentForm = getStoredForm(stack);
        if (priorityForm.equals(currentForm) && hasAlternate) {
            return alternateForm;
        }
        if (alternateForm.equals(currentForm) && hasPriority) {
            return priorityForm;
        }
        if (hasPriority) {
            return priorityForm;
        }
        return alternateForm;
    }

    public static String getBestCombatForm(ItemStack stack) {
        boolean hasSword = !getStoredToolForForm(stack, FORM_SWORD).isEmpty();
        boolean hasAxe = !getStoredToolForForm(stack, FORM_AXE).isEmpty();
        if (hasSword && hasAxe) {
            return getCycledUseForm(stack, FORM_SWORD, FORM_AXE);
        }
        if (hasSword) {
            return FORM_SWORD;
        }

        String bestForm = FORM_BASE;
        double bestAttackDamage = Double.NEGATIVE_INFINITY;
        for (String form : COMBAT_FALLBACK_FORMS) {
            ItemStack storedTool = getStoredToolForForm(stack, form);
            if (storedTool.isEmpty()) {
                continue;
            }

            double attackDamage = getToolAttackDamage(storedTool);
            if (attackDamage > bestAttackDamage) {
                bestForm = form;
                bestAttackDamage = attackDamage;
            }
        }

        return bestForm;
    }

    public static String getWorstFormFor(ItemStack stack, BlockState state) {
        String worstForm = FORM_BASE;
        int worstTierLevel = Integer.MAX_VALUE;
        int worstTieBreaker = Integer.MIN_VALUE;

        for (String form : BLOCK_TIE_BREAKER_FORMS) {
            ItemStack storedTool = getStoredToolForForm(stack, form);
            if (storedTool.isEmpty() || isFormEffectiveForBlock(form, state)) {
                continue;
            }

            int tierLevel = getToolTierLevel(storedTool);
            int tieBreaker = getBlockTieBreaker(form);
            if (tierLevel < worstTierLevel || tierLevel == worstTierLevel && tieBreaker > worstTieBreaker) {
                worstForm = form;
                worstTierLevel = tierLevel;
                worstTieBreaker = tieBreaker;
            }
        }

        return FORM_BASE.equals(worstForm) ? getWorstCombatForm(stack) : worstForm;
    }

    public static String getWorstCombatForm(ItemStack stack) {
        String worstForm = FORM_BASE;
        double worstAttackDamage = Double.POSITIVE_INFINITY;
        for (String form : BLOCK_TIE_BREAKER_FORMS) {
            ItemStack storedTool = getStoredToolForForm(stack, form);
            if (storedTool.isEmpty()) {
                continue;
            }

            double attackDamage = getToolAttackDamage(storedTool);
            if (attackDamage < worstAttackDamage) {
                worstForm = form;
                worstAttackDamage = attackDamage;
            }
        }

        return worstForm;
    }

    public static boolean tryTransformHeldOrEquippedToForm(ItemStack stack, LivingEntity entity, String targetForm) {
        return tryTransformHeldOrEquippedToForm(stack, entity, targetForm, 0, false);
    }

    public static boolean tryTransformHeldOrEquippedToForm(ItemStack stack, LivingEntity entity, String targetForm, int durabilityDamage, boolean consumeUsage) {
        if (!canTransformToForm(stack, targetForm) || targetForm.equals(getStoredForm(stack))) {
            return false;
        }

        ItemStack transformed = copyWithForm(stack, targetForm);
        if (consumeUsage) {
            MimiclingFoods.consumeUsage(transformed);
        }
        applyDurabilityDamage(transformed, entity, durabilityDamage);
        startTransition(transformed, getStoredForm(stack), targetForm, entity.level().getGameTime());
        if (!replaceHeldOrEquippedStack(stack, transformed, entity)) {
            return false;
        }

        playTransformSound(entity.level(), entity);
        return true;
    }

    public static int getBlockUseDurabilityCost(ItemStack stack) {
        return FORM_SWORD.equals(getStoredForm(stack)) ? 2 : 1;
    }

    public static int getAttackUseDurabilityCost(ItemStack stack) {
        return FORM_SWORD.equals(getStoredForm(stack)) ? 1 : 2;
    }

    public static String getSwordForm() {
        return FORM_SWORD;
    }

    public static String getBaseForm() {
        return FORM_BASE;
    }

    public static String getPickaxeForm() {
        return FORM_PICKAXE;
    }

    public static String getAxeForm() {
        return FORM_AXE;
    }

    public static String getShovelForm() {
        return FORM_SHOVEL;
    }

    public static String getHoeForm() {
        return FORM_HOE;
    }

    public static boolean isValidForm(String form) {
        return FORM_BASE.equals(form) || FORM_PICKAXE.equals(form) || FORM_AXE.equals(form) || FORM_SHOVEL.equals(form) || FORM_HOE.equals(form) || FORM_SWORD.equals(form);
    }

    public static boolean canTransformToForm(ItemStack stack, String targetForm) {
        if (!(stack.getItem() instanceof MimiclingFormItem) || !isValidForm(targetForm)) {
            return false;
        }

        if (FORM_BASE.equals(targetForm)) {
            return true;
        }

        if (isDurabilityExhausted(stack)) {
            return false;
        }

        int slot = getDedicatedSlot(targetForm);
        return slot >= 0 && !getStoredItem(stack, slot).isEmpty();
    }

    public static void tickMimiclingInventory(ItemStack stack, Level level) {
        if (!level.isClientSide) {
            removeStaleAttributeModifierTags(stack);
            syncActiveEnchantmentTags(stack, getStoredForm(stack));
        }
    }

    public static void tickMimiclingInventory(ItemStack stack, Level level, Entity entity) {
        tickMimiclingInventory(stack, level);
        if (!level.isClientSide) {
            MimiclingFoodEffects.tickHeld(stack, level, entity);
        }
    }

    public static void onMimiclingMineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && !state.isAir()) {
            MimiclingFoodEffects.onBreak(stack, level, pos, state, entity);
            MimiclingFoods.consumeUsage(stack);
        }
    }

    public static void onMimiclingHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            MimiclingFoodEffects.onAttack(stack, target, attacker);
            MimiclingFoods.consumeUsage(stack);
        }
    }

    public static boolean isMimiclingFoil(ItemStack stack) {
        ItemStack storedTool = getStoredToolForCurrentForm(stack);
        return !stack.getEnchantmentTags().isEmpty() || !storedTool.isEmpty() && storedTool.hasFoil();
    }

    public static void onMimiclingDestroyed(ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getStoredItems(itemEntity.getItem()));
    }

    public static boolean tryScrollSelectedSlot(ItemStack stack, int delta) {
        if (!(stack.getItem() instanceof MimiclingFormItem) || !canUseStorage(stack)) {
            return false;
        }

        if (getStoredItemCount(stack) == 0) {
            return false;
        }

        int currentSelected = getSelectedSlot(stack);
        int selected = currentSelected;
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            selected = Math.floorMod(selected + delta, MAX_STORED_ITEMS);
            if (!getStoredItem(stack, selected).isEmpty()) {
                if (selected == currentSelected) {
                    return false;
                }

                stack.getOrCreateTag().putInt(SELECTED_SLOT_TAG, selected);
                return true;
            }
        }

        return false;
    }

    public static boolean tryScrollSelectedFoodSlot(ItemStack stack, ItemStack foodStack, int delta) {
        if (!canSelectFoodReplacement(stack, foodStack)) {
            return false;
        }

        int activeFoodCount = MimiclingFoods.getActiveFoodEntries(stack).size();
        int currentSelected = getSelectedFoodSlot(stack);
        int selected = Math.floorMod(currentSelected + delta, activeFoodCount);
        if (selected == currentSelected) {
            return false;
        }

        stack.getOrCreateTag().putInt(SELECTED_FOOD_SLOT_TAG, selected);
        return true;
    }

    public static boolean tryScrollSelectedActiveFoodSlot(ItemStack stack, int delta) {
        if (!(stack.getItem() instanceof MimiclingFormItem)) {
            return false;
        }

        int activeFoodCount = MimiclingFoods.getActiveFoodEntries(stack).size();
        if (activeFoodCount <= 1) {
            return false;
        }

        int currentSelected = getSelectedFoodSlot(stack);
        int selected = Math.floorMod(currentSelected + delta, activeFoodCount);
        if (selected == currentSelected) {
            return false;
        }

        stack.getOrCreateTag().putInt(SELECTED_FOOD_SLOT_TAG, selected);
        return true;
    }

    public static boolean canSelectFoodReplacement(ItemStack stack, ItemStack foodStack) {
        if (!(stack.getItem() instanceof MimiclingFormItem) || !canUseStorage(stack)) {
            return false;
        }

        MimiclingFoods.FoodDefinition food = MimiclingFoods.getFood(foodStack);
        if (food == null || !MimiclingFoods.shouldRemember(food)) {
            return false;
        }

        return !MimiclingFoods.hasActiveFood(stack, food.id())
                && MimiclingFoods.getActiveFoodEntries(stack).size() >= 2;
    }

    private static int getCarriedFoodHighlightSlot(ItemStack stack, ItemStack foodStack) {
        MimiclingFoods.FoodDefinition food = MimiclingFoods.getFood(foodStack);
        if (food == null || !MimiclingFoods.shouldRemember(food)) {
            return -1;
        }

        List<MimiclingFoods.ActiveFood> activeFoods = MimiclingFoods.getActiveFoodEntries(stack);
        for (int i = 0; i < activeFoods.size(); i++) {
            if (food.id().equals(activeFoods.get(i).food().id())) {
                return i;
            }
        }

        return canSelectFoodReplacement(stack, foodStack) ? getSelectedFoodSlot(stack) : -1;
    }

    public static boolean trySelectDedicatedSlot(ItemStack stack, ItemStack toolStack) {
        int slot = getDedicatedSlot(toolStack);
        if (!(stack.getItem() instanceof MimiclingFormItem) || !canUseStorage(stack) || slot < 0) {
            return false;
        }

        if (getSelectedSlot(stack) == slot) {
            return false;
        }

        stack.getOrCreateTag().putInt(SELECTED_SLOT_TAG, slot);
        return true;
    }

    public static boolean trySelectNextOccupiedSlotIfSelectedEmpty(ItemStack stack) {
        if (!(stack.getItem() instanceof MimiclingFormItem) || !canUseStorage(stack) || getStoredItemCount(stack) == 0) {
            return false;
        }

        int selected = getSelectedSlot(stack);
        if (!getStoredItem(stack, selected).isEmpty()) {
            return false;
        }

        int nextSelected = getSelectedOccupiedSlotOrNext(getStoredItemsList(stack), selected);
        if (nextSelected == selected) {
            return false;
        }

        stack.getOrCreateTag().putInt(SELECTED_SLOT_TAG, nextSelected);
        return true;
    }

    private static void startTransition(ItemStack stack, String fromForm, String toForm, long gameTime) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(FORM_TAG, toForm);
        tag.putString(TRANSITION_FROM_TAG, fromForm);
        tag.putString(TRANSITION_TO_TAG, toForm);
        tag.putLong(TRANSITION_START_TAG, gameTime);
        removeStaleAttributeModifierTags(stack);
        syncActiveEnchantmentTags(stack, toForm);
    }

    private static void clearTransitionData(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(TRANSITION_START_TAG);
        tag.remove(TRANSITION_FROM_TAG);
        tag.remove(TRANSITION_TO_TAG);
    }

    private static ItemStack copyWithForm(ItemStack stack, String targetForm) {
        ItemStack transformed = new ItemStack(getItemForForm(stack, targetForm), stack.getCount());
        if (stack.hasTag()) {
            transformed.setTag(stack.getTag().copy());
        }
        transformed.setDamageValue(Math.min(stack.getDamageValue(), transformed.getMaxDamage()));
        return transformed;
    }

    private static boolean replaceHeldOrEquippedStack(ItemStack original, ItemStack replacement, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.getMainHandItem() == original) {
                player.setItemInHand(InteractionHand.MAIN_HAND, replacement);
                return true;
            }
            if (player.getOffhandItem() == original) {
                player.setItemInHand(InteractionHand.OFF_HAND, replacement);
                return true;
            }
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                if (player.getInventory().items.get(i) == original) {
                    player.getInventory().items.set(i, replacement);
                    return true;
                }
            }
            for (int i = 0; i < player.getInventory().offhand.size(); i++) {
                if (player.getInventory().offhand.get(i) == original) {
                    player.getInventory().offhand.set(i, replacement);
                    return true;
                }
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (entity.getItemBySlot(slot) == original) {
                entity.setItemSlot(slot, replacement);
                return true;
            }
        }

        return false;
    }

    private static Item getItemForForm(ItemStack stack, String form) {
        Tiers tier = getClosestVanillaTier(getStoredToolForForm(stack, form));
        if (FORM_PICKAXE.equals(form)) {
            return getPickaxeItemForTier(tier);
        }
        if (FORM_AXE.equals(form)) {
            return getAxeItemForTier(tier);
        }
        if (FORM_SHOVEL.equals(form)) {
            return getShovelItemForTier(tier);
        }
        if (FORM_HOE.equals(form)) {
            return getHoeItemForTier(tier);
        }
        if (FORM_SWORD.equals(form)) {
            return getSwordItemForTier(tier);
        }
        return DNLItems.MIMICLING.get();
    }

    private static Item getPickaxeItemForTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> DNLItems.MIMICLING_WOODEN_PICKAXE.get();
            case STONE -> DNLItems.MIMICLING_STONE_PICKAXE.get();
            case IRON -> DNLItems.MIMICLING_IRON_PICKAXE.get();
            case GOLD -> DNLItems.MIMICLING_GOLDEN_PICKAXE.get();
            case NETHERITE -> DNLItems.MIMICLING_NETHERITE_PICKAXE.get();
            default -> DNLItems.MIMICLING_DIAMOND_PICKAXE.get();
        };
    }

    private static Item getAxeItemForTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> DNLItems.MIMICLING_WOODEN_AXE.get();
            case STONE -> DNLItems.MIMICLING_STONE_AXE.get();
            case IRON -> DNLItems.MIMICLING_IRON_AXE.get();
            case GOLD -> DNLItems.MIMICLING_GOLDEN_AXE.get();
            case NETHERITE -> DNLItems.MIMICLING_NETHERITE_AXE.get();
            default -> DNLItems.MIMICLING_DIAMOND_AXE.get();
        };
    }

    private static Item getShovelItemForTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> DNLItems.MIMICLING_WOODEN_SHOVEL.get();
            case STONE -> DNLItems.MIMICLING_STONE_SHOVEL.get();
            case IRON -> DNLItems.MIMICLING_IRON_SHOVEL.get();
            case GOLD -> DNLItems.MIMICLING_GOLDEN_SHOVEL.get();
            case NETHERITE -> DNLItems.MIMICLING_NETHERITE_SHOVEL.get();
            default -> DNLItems.MIMICLING_DIAMOND_SHOVEL.get();
        };
    }

    private static Item getHoeItemForTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> DNLItems.MIMICLING_WOODEN_HOE.get();
            case STONE -> DNLItems.MIMICLING_STONE_HOE.get();
            case IRON -> DNLItems.MIMICLING_IRON_HOE.get();
            case GOLD -> DNLItems.MIMICLING_GOLDEN_HOE.get();
            case NETHERITE -> DNLItems.MIMICLING_NETHERITE_HOE.get();
            default -> DNLItems.MIMICLING_DIAMOND_HOE.get();
        };
    }

    private static Item getSwordItemForTier(Tiers tier) {
        return switch (tier) {
            case WOOD -> DNLItems.MIMICLING_WOODEN_SWORD.get();
            case STONE -> DNLItems.MIMICLING_STONE_SWORD.get();
            case IRON -> DNLItems.MIMICLING_IRON_SWORD.get();
            case GOLD -> DNLItems.MIMICLING_GOLDEN_SWORD.get();
            case NETHERITE -> DNLItems.MIMICLING_NETHERITE_SWORD.get();
            default -> DNLItems.MIMICLING_DIAMOND_SWORD.get();
        };
    }

    private static Tiers getClosestVanillaTier(ItemStack storedTool) {
        if (storedTool.getItem() instanceof TieredItem tieredItem) {
            Tier tier = tieredItem.getTier();
            if (tier instanceof Tiers vanillaTier) {
                return vanillaTier;
            }
            return getClosestVanillaTier(tier);
        }
        return Tiers.DIAMOND;
    }

    private static Tiers getClosestVanillaTier(Tier tier) {
        Tiers closest = Tiers.DIAMOND;
        double closestScore = Double.MAX_VALUE;
        for (Tiers candidate : Tiers.values()) {
            double score = Math.abs(candidate.getLevel() - tier.getLevel()) * 100.0D
                    + Math.abs(candidate.getSpeed() - tier.getSpeed())
                    + Math.abs(candidate.getAttackDamageBonus() - tier.getAttackDamageBonus())
                    + Math.abs(candidate.getUses() - tier.getUses()) / 1000.0D
                    + Math.abs(candidate.getEnchantmentValue() - tier.getEnchantmentValue()) / 10.0D;
            if (score < closestScore) {
                closest = candidate;
                closestScore = score;
            }
        }
        return closest;
    }

    private static void playTransformSound(Level level, Player player) {
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 1.1F);
        }
    }

    private static void playTransformSound(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 1.1F);
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

    public static boolean isChewingFrame(ItemStack stack, long gameTime, int frame) {
        if (!isBaseStorageForm(stack) || !stack.hasTag() || !stack.getTag().contains(CHEWING_START_TAG)) {
            return false;
        }

        long elapsed = gameTime - stack.getTag().getLong(CHEWING_START_TAG);
        return elapsed >= 0 && elapsed < CHEWING_FRAME_COUNT * CHEWING_TICKS_PER_FRAME && elapsed / CHEWING_TICKS_PER_FRAME == frame;
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

        return getStoredForm(stack);
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
        if (stack.getItem() instanceof MimiclingFormItem mimiclingFormItem && !FORM_BASE.equals(mimiclingFormItem.getMimiclingForm())) {
            return mimiclingFormItem.getMimiclingForm();
        }

        if (stack.hasTag()) {
            String legacyForm = stack.getTag().getString(FORM_TAG);
            if (!legacyForm.isEmpty()) {
                return legacyForm;
            }
        }

        return FORM_BASE;
    }

    private static boolean canUseStorage(ItemStack stack) {
        return FORM_BASE.equals(getStoredForm(stack));
    }

    private static void startChewing(ItemStack stack, long gameTime) {
        stack.getOrCreateTag().putLong(CHEWING_START_TAG, gameTime);
    }

    public static boolean isBaseStorageForm(ItemStack stack) {
        return stack.getItem() instanceof MimiclingFormItem && canUseStorage(stack);
    }

    public static boolean isFeedableTool(ItemStack stack) {
        return isMimicMucus(stack) || MimiclingFoods.getFood(stack) != null || getDedicatedSlot(stack) >= 0;
    }

    private static boolean isFeedableFood(ItemStack stack) {
        return isMimicMucus(stack) || MimiclingFoods.getFood(stack) != null;
    }

    private static boolean canAcceptFeed(ItemStack stack, ItemStack itemToFeed) {
        if (isMimicMucus(itemToFeed)) {
            return getStorageCapacity(stack) < MAX_STORED_ITEMS || stack.getDamageValue() > 0;
        }

        MimiclingFoods.FoodDefinition food = MimiclingFoods.getFood(itemToFeed);
        if (food != null) {
            return MimiclingFoods.canAcceptFood(stack, food, stack.getDamageValue() > 0 && food.durability() > 0);
        }

        int slot = getDedicatedSlot(itemToFeed);
        if (slot < 0) {
            return false;
        }

        ItemStack existing = getStoredItem(stack, slot);
        return !existing.isEmpty() || getStoredItemCount(stack) < getStorageCapacity(stack);
    }

    private static boolean isMimicMucus(ItemStack stack) {
        return stack.is(DNLItems.MIMIC_MUCUS.get());
    }

    private static int getDedicatedSlot(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MimiclingFormItem) {
            return -1;
        }
        if (item instanceof SwordItem) {
            return 0;
        }
        if (item instanceof PickaxeItem) {
            return 1;
        }
        if (item instanceof AxeItem) {
            return 2;
        }
        if (item instanceof ShovelItem) {
            return 3;
        }
        if (item instanceof HoeItem) {
            return 4;
        }
        return -1;
    }

    private static int getDedicatedSlot(String form) {
        if (FORM_SWORD.equals(form)) {
            return 0;
        }
        if (FORM_PICKAXE.equals(form)) {
            return 1;
        }
        if (FORM_AXE.equals(form)) {
            return 2;
        }
        if (FORM_SHOVEL.equals(form)) {
            return 3;
        }
        if (FORM_HOE.equals(form)) {
            return 4;
        }
        return -1;
    }

    private static int getStoredItemCount(ItemStack stack) {
        int count = 0;
        ListTag items = getStoredItemsList(stack);
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            if (i < items.size() && !ItemStack.of(items.getCompound(i)).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static ListTag getStoredItemsList(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new ListTag() : tag.getList(STORED_ITEMS_TAG, 10);
    }

    private static Stream<ItemStack> getStoredItems(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            ItemStack stored = getStoredItem(stack, i);
            if (!stored.isEmpty()) {
                items.add(stored);
            }
        }
        return items.stream();
    }

    private static NonNullList<ItemStack> getTooltipContents(ItemStack stack) {
        NonNullList<ItemStack> contents = NonNullList.create();
        for (int i = 0; i < MAX_STORED_ITEMS; i++) {
            contents.add(getStoredItem(stack, i));
        }
        return contents;
    }

    private static List<ItemStack> feedOne(ItemStack stack, ItemStack itemToFeed) {
        return feedOne(stack, itemToFeed, -1);
    }

    private static List<ItemStack> feedOne(ItemStack stack, ItemStack itemToFeed, int preferredFoodReplacementIndex) {
        if (isMimicMucus(itemToFeed)) {
            feedMimicMucus(stack);
            return List.of();
        }

        MimiclingFoods.FoodDefinition food = MimiclingFoods.getFood(itemToFeed);
        if (food != null) {
            repairDurability(stack, food.durability());
            List<ItemStack> returnedItems = new java.util.ArrayList<>(MimiclingFoods.rememberFood(stack, food, itemToFeed, preferredFoodReplacementIndex));
            ItemStack onFedReturn = MimiclingFoods.getOnFedReturnStack(food);
            if (!onFedReturn.isEmpty()) {
                returnedItems.add(onFedReturn);
            }
            return returnedItems;
        }

        ItemStack removed = storeInDedicatedSlot(stack, itemToFeed);
        return removed.isEmpty() ? List.of() : List.of(removed);
    }

    private static void giveReturnedFeedItems(Player player, Slot preferredSlot, List<ItemStack> returnedItems) {
        for (ItemStack returnedItem : returnedItems) {
            ItemStack remainder = preferredSlot.safeInsert(returnedItem);
            giveReturnedFeedItem(player, remainder);
        }
    }

    private static void giveReturnedFeedItems(Player player, List<ItemStack> returnedItems) {
        for (ItemStack returnedItem : returnedItems) {
            giveReturnedFeedItem(player, returnedItem);
        }
    }

    private static void giveReturnedFeedItem(Player player, ItemStack returnedItem) {
        if (returnedItem.isEmpty()) {
            return;
        }

        if (!player.getInventory().add(returnedItem)) {
            player.drop(returnedItem, false);
        }
    }

    private static void applyDurabilityDamage(ItemStack stack, LivingEntity entity, int amount) {
        if (amount <= 0 || !stack.isDamageableItem()) {
            return;
        }

        stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
    }

    public static Optional<TooltipComponent> getActiveFoodTooltipImage(ItemStack stack) {
        List<MimiclingTooltip.ActiveFood> activeFoods = getTooltipActiveFoods(stack);
        return activeFoods.isEmpty()
                ? Optional.empty()
                : Optional.of(new MimiclingTooltip(NonNullList.create(), activeFoods, -1, -1, 0));
    }

    public static Optional<TooltipComponent> getTooltipImageForSelectedFood(ItemStack stack) {
        if (!(stack.getItem() instanceof MimiclingFormItem)) {
            return Optional.empty();
        }

        List<MimiclingFoods.ActiveFood> activeFoods = MimiclingFoods.getActiveFoodEntries(stack);
        if (activeFoods.isEmpty()) {
            return Optional.empty();
        }

        int selected = getSelectedFoodSlot(stack);
        if (selected < 0 || selected >= activeFoods.size()) {
            return Optional.empty();
        }

        MimiclingFoods.ActiveFood selectedFood = activeFoods.get(selected);
        return getTooltipImage(
                stack,
                selectedFood.displayStack(),
                selectedFood.food().description(),
                selected,
                !isFeedableFood(selectedFood.displayStack())
        );
    }

    public static void appendActiveFoodTooltip(ItemStack stack, List<Component> components) {
    }

    private static List<MimiclingTooltip.ActiveFood> getTooltipActiveFoods(ItemStack stack) {
        List<MimiclingFoods.ActiveFood> activeFoods = MimiclingFoods.getActiveFoodEntries(stack);
        if (activeFoods.isEmpty()) {
            return List.of();
        }

        List<MimiclingTooltip.ActiveFood> tooltipFoods = new java.util.ArrayList<>();
        for (MimiclingFoods.ActiveFood activeFood : activeFoods) {
            tooltipFoods.add(new MimiclingTooltip.ActiveFood(
                    activeFood.displayStack(),
                    activeFood.uses(),
                    activeFood.maxUses(),
                    activeFood.food().infiniteUsage() || activeFood.uses() < 0
            ));
        }
        return tooltipFoods;
    }

    private static void feedMimicMucus(ItemStack stack) {
        int capacity = getStorageCapacity(stack);
        int healAmount = capacity >= MAX_STORED_ITEMS ? MUCUS_UNLOCKED_HEAL_AMOUNT : MUCUS_LOCKED_HEAL_AMOUNT;
        repairDurability(stack, healAmount);

        if (capacity >= MAX_STORED_ITEMS) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int mucusFed = tag.getInt(MUCUS_FED_TAG) + 1;
        if (mucusFed >= MUCUS_PER_CAPACITY_UPGRADE) {
            tag.putInt(CAPACITY_TAG, Math.min(MAX_STORED_ITEMS, capacity + 1));
            tag.putInt(MUCUS_FED_TAG, 0);
            return;
        }

        tag.putInt(MUCUS_FED_TAG, mucusFed);
    }

    private static void repairDurability(ItemStack stack, int amount) {
        if (amount <= 0 || !stack.isDamageableItem()) {
            return;
        }

        stack.setDamageValue(Math.max(0, stack.getDamageValue() - amount));
    }

    private static ItemStack storeInDedicatedSlot(ItemStack stack, ItemStack itemToStore) {
        int slot = getDedicatedSlot(itemToStore);
        if (itemToStore.isEmpty() || slot < 0) {
            return ItemStack.EMPTY;
        }

        CompoundTag tag = stack.getOrCreateTag();
        ListTag items = getOrCreateFixedStoredItems(stack);
        ItemStack removed = ItemStack.of(items.getCompound(slot));
        if (removed.isEmpty() && getStoredItemCount(stack) >= getStorageCapacity(stack)) {
            return ItemStack.EMPTY;
        }

        ItemStack stored = itemToStore.copyWithCount(1);
        CompoundTag storedTag = new CompoundTag();
        stored.save(storedTag);
        items.set(slot, storedTag);
        tag.put(STORED_ITEMS_TAG, items);
        tag.putInt(SELECTED_SLOT_TAG, slot);
        return removed;
    }

    private static Optional<ItemStack> removeSelected(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(STORED_ITEMS_TAG)) {
            return Optional.empty();
        }

        ListTag items = tag.getList(STORED_ITEMS_TAG, 10);
        if (items.isEmpty()) {
            return Optional.empty();
        }

        int selected = getSelectedSlot(stack);
        if (selected >= items.size()) {
            return Optional.empty();
        }

        ItemStack removed = ItemStack.of(items.getCompound(selected));
        if (removed.isEmpty()) {
            return Optional.empty();
        }

        items.set(selected, new CompoundTag());
        cleanupStoredItemsTag(stack, items);
        return Optional.of(removed);
    }

    private static int getStorageCapacity(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(CAPACITY_TAG)) {
            return INITIAL_CAPACITY;
        }

        return Math.max(INITIAL_CAPACITY, Math.min(stack.getTag().getInt(CAPACITY_TAG), MAX_STORED_ITEMS));
    }

    private static int getSelectedSlot(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }

        int selected = stack.getTag().getInt(SELECTED_SLOT_TAG);
        return Math.max(0, Math.min(selected, MAX_STORED_ITEMS - 1));
    }

    private static int getSelectedFoodSlot(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }

        int activeFoodCount = MimiclingFoods.getActiveFoodEntries(stack).size();
        if (activeFoodCount <= 0) {
            return 0;
        }

        int selected = stack.getTag().getInt(SELECTED_FOOD_SLOT_TAG);
        return Math.max(0, Math.min(selected, activeFoodCount - 1));
    }

    private static int getSelectedFoodReplacementSlot(ItemStack stack, ItemStack foodStack) {
        return canSelectFoodReplacement(stack, foodStack) ? getSelectedFoodSlot(stack) : -1;
    }

    private static void cleanupStoredItemsTag(ItemStack stack, ListTag items) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean hasStoredItem = false;
        for (int i = 0; i < items.size(); i++) {
            if (!ItemStack.of(items.getCompound(i)).isEmpty()) {
                hasStoredItem = true;
                break;
            }
        }

        if (!hasStoredItem) {
            tag.remove(STORED_ITEMS_TAG);
            tag.remove(SELECTED_SLOT_TAG);
            return;
        }

        tag.put(STORED_ITEMS_TAG, items);
        tag.putInt(SELECTED_SLOT_TAG, getSelectedOccupiedSlotOrNext(items, getSelectedSlot(stack)));
    }

    private static ItemStack getStoredItem(ItemStack stack, int slot) {
        ListTag items = getStoredItemsList(stack);
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(items.getCompound(slot));
    }

    private static int getSelectedOccupiedSlotOrNext(ListTag items, int selectedSlot) {
        if (selectedSlot < items.size() && !ItemStack.of(items.getCompound(selectedSlot)).isEmpty()) {
            return selectedSlot;
        }

        for (int i = 1; i <= MAX_STORED_ITEMS; i++) {
            int slot = Math.floorMod(selectedSlot + i, MAX_STORED_ITEMS);
            if (slot < items.size() && !ItemStack.of(items.getCompound(slot)).isEmpty()) {
                return slot;
            }
        }

        return selectedSlot;
    }

    private static ListTag getOrCreateFixedStoredItems(ItemStack stack) {
        ListTag items = stack.getOrCreateTag().getList(STORED_ITEMS_TAG, 10);
        while (items.size() < MAX_STORED_ITEMS) {
            items.add(new CompoundTag());
        }
        return items;
    }

    private static ItemStack getStoredToolForCurrentForm(ItemStack stack) {
        return getStoredToolForForm(stack, getStoredForm(stack));
    }

    private static ItemStack getStoredToolForForm(ItemStack stack, String form) {
        int slot = getDedicatedSlot(form);
        return slot < 0 ? ItemStack.EMPTY : getStoredItem(stack, slot);
    }

    private static boolean isFormEffectiveForBlock(String form, BlockState state) {
        if (FORM_SWORD.equals(form)) {
            return state.is(Blocks.COBWEB);
        }
        if (FORM_PICKAXE.equals(form)) {
            return state.is(BlockTags.MINEABLE_WITH_PICKAXE);
        }
        if (FORM_AXE.equals(form)) {
            return state.is(BlockTags.MINEABLE_WITH_AXE);
        }
        if (FORM_SHOVEL.equals(form)) {
            return state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        }
        if (FORM_HOE.equals(form)) {
            return state.is(BlockTags.MINEABLE_WITH_HOE);
        }
        return false;
    }

    private static boolean isHoeHarvestCropBlock(BlockState state) {
        return state.is(DNLTags.MIMICLING_HOE_HARVESTABLE);
    }

    private static int getToolTierLevel(ItemStack stack) {
        return stack.getItem() instanceof TieredItem tieredItem ? tieredItem.getTier().getLevel() : -1;
    }

    private static int getBlockTieBreaker(String form) {
        for (int i = 0; i < BLOCK_TIE_BREAKER_FORMS.length; i++) {
            if (BLOCK_TIE_BREAKER_FORMS[i].equals(form)) {
                return i;
            }
        }
        return BLOCK_TIE_BREAKER_FORMS.length;
    }

    private static double getToolAttackDamage(ItemStack stack) {
        double attackDamage = 0.0D;
        for (AttributeModifier modifier : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
            if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                attackDamage += modifier.getAmount();
            }
        }
        return attackDamage;
    }

    private static boolean isDurabilityExhausted(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage();
    }

    private static void syncActiveEnchantmentTags(ItemStack stack, String form) {
        CompoundTag tag = stack.getOrCreateTag();
        ItemStack storedTool = getStoredToolForForm(stack, form);
        if (storedTool.isEmpty()) {
            tag.remove("Enchantments");
            return;
        }

        ListTag enchantments = storedTool.getEnchantmentTags().copy();
        MimiclingFoodEffects.applyTemporaryEnchantmentModifiers(stack, enchantments);
        if (enchantments.isEmpty()) {
            tag.remove("Enchantments");
            return;
        }

        tag.put("Enchantments", enchantments);
    }

    private static void removeStaleAttributeModifierTags(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove("AttributeModifiers");
        }
    }

    private static void playRemoveOneSound(LivingEntity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(LivingEntity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
