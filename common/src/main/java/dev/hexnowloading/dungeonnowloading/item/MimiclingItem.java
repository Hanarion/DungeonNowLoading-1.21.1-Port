package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.stream.Stream;

public class MimiclingItem extends Item {
    private static final String FORM_TAG = "MimiclingForm";
    private static final String TRANSITION_START_TAG = "MimiclingTransitionStart";
    private static final String TRANSITION_FROM_TAG = "MimiclingTransitionFrom";
    private static final String TRANSITION_TO_TAG = "MimiclingTransitionTo";
    private static final String STORED_ITEMS_TAG = "MimiclingItems";
    private static final String SELECTED_SLOT_TAG = "MimiclingSelectedSlot";
    private static final String FORM_BASE = "base";
    private static final String FORM_PICKAXE = "pickaxe";
    private static final String FORM_AXE = "axe";
    private static final String FORM_SHOVEL = "shovel";
    private static final String FORM_HOE = "hoe";
    private static final String FORM_SWORD = "sword";
    private static final int MAX_STORED_ITEMS = 5;
    private static final int TRANSITION_DURATION = 20;
    private static final int FIRST_PHASE_TICKS = 10;

    public MimiclingItem(Properties properties) {
        super(properties);
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

        if (isFeedableTool(slotStack) && slot.allowModification(player)) {
            ItemStack taken = slot.safeTake(1, 1, player);
            if (!taken.isEmpty()) {
                ItemStack removed = storeInDedicatedSlot(stack, taken);
                slot.safeInsert(removed);
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

        ItemStack inserted = carriedStack.copyWithCount(1);
        ItemStack removed = storeInDedicatedSlot(stack, inserted);
        carriedStack.shrink(1);
        carriedSlot.set(removed.isEmpty() ? carriedStack : removed);
        playInsertSound(player);
        return true;
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

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!canUseStorage(stack)) {
            return Optional.empty();
        }

        NonNullList<ItemStack> contents = getTooltipContents(stack);
        return Optional.of(new MimiclingTooltip(contents, getSelectedSlot(stack)));
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemUtils.onContainerDestroyed(itemEntity, getStoredItems(itemEntity.getItem()));
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

    public static String getBaseForm() {
        return FORM_BASE;
    }

    public static boolean isValidForm(String form) {
        return FORM_BASE.equals(form) || FORM_PICKAXE.equals(form) || FORM_AXE.equals(form) || FORM_SHOVEL.equals(form) || FORM_HOE.equals(form) || FORM_SWORD.equals(form);
    }

    public static boolean tryScrollSelectedSlot(ItemStack stack, int delta) {
        if (!(stack.getItem() instanceof MimiclingItem) || !canUseStorage(stack)) {
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

    public static boolean trySelectDedicatedSlot(ItemStack stack, ItemStack toolStack) {
        int slot = getDedicatedSlot(toolStack);
        if (!(stack.getItem() instanceof MimiclingItem) || !canUseStorage(stack) || slot < 0) {
            return false;
        }

        if (getSelectedSlot(stack) == slot) {
            return false;
        }

        stack.getOrCreateTag().putInt(SELECTED_SLOT_TAG, slot);
        return true;
    }

    public static boolean trySelectNextOccupiedSlotIfSelectedEmpty(ItemStack stack) {
        if (!(stack.getItem() instanceof MimiclingItem) || !canUseStorage(stack) || getStoredItemCount(stack) == 0) {
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

    private static boolean canUseStorage(ItemStack stack) {
        return FORM_BASE.equals(getStoredForm(stack));
    }

    public static boolean isFeedableTool(ItemStack stack) {
        return getDedicatedSlot(stack) >= 0;
    }

    private static int getDedicatedSlot(ItemStack stack) {
        Item item = stack.getItem();
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

    private static ItemStack storeInDedicatedSlot(ItemStack stack, ItemStack itemToStore) {
        int slot = getDedicatedSlot(itemToStore);
        if (itemToStore.isEmpty() || slot < 0) {
            return ItemStack.EMPTY;
        }

        CompoundTag tag = stack.getOrCreateTag();
        ListTag items = getOrCreateFixedStoredItems(stack);
        ItemStack removed = ItemStack.of(items.getCompound(slot));
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

    private static int getSelectedSlot(ItemStack stack) {
        if (!stack.hasTag()) {
            return 0;
        }

        int selected = stack.getTag().getInt(SELECTED_SLOT_TAG);
        return Math.max(0, Math.min(selected, MAX_STORED_ITEMS - 1));
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

    private static void playRemoveOneSound(LivingEntity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(LivingEntity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
