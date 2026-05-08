package dev.hexnowloading.dungeonnowloading.item;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MimiclingFoods {
    private static final String ACTIVE_FOODS_TAG = "MimiclingActiveFoods";
    private static final String ACTIVE_FOOD_ID_TAG = "Food";
    private static final String ACTIVE_FOOD_ITEM_TAG = "Item";
    private static final String ACTIVE_FOOD_USES_TAG = "Uses";
    private static final String ACTIVE_FOOD_MAX_USES_TAG = "MaxUses";
    private static final String USAGE_HANDLED_FOODS_TAG = "MimiclingUsageHandledFoods";
    private static final int INFINITE_USES = -1;
    private static final int MAX_USAGE_STACKS = 3;
    private static final int MAX_ACTIVE_FOODS = 2;
    private static final String FOOD_TOOLTIP_PREFIX = "item.dungeonnowloading.mimicling.tooltip.food.";
    private static final Map<String, FoodDefinition> FOODS_BY_ID = new HashMap<>();
    private static final Map<Item, FoodDefinition> ITEM_FOODS = new HashMap<>();
    private static final List<TagFoodEntry> TAG_FOODS = new ArrayList<>();

    private MimiclingFoods() {}

    public static FoodDefinition getFood(ItemStack stack) {
        FoodDefinition itemFood = ITEM_FOODS.get(stack.getItem());
        if (itemFood != null) {
            return itemFood;
        }

        FoodDefinition tagFood = null;
        for (TagFoodEntry entry : TAG_FOODS) {
            if (stack.is(entry.tag())) {
                if (tagFood == null || entry.food().durability() > tagFood.durability()) {
                    tagFood = entry.food();
                }
            }
        }
        return tagFood;
    }

    public static ItemStack getOnFedReturnStack(FoodDefinition food) {
        return food.returnItem() == null ? ItemStack.EMPTY : new ItemStack(food.returnItem());
    }

    public static List<Component> getPreviewDescription(ItemStack mimicling, ItemStack fedStack) {
        FoodDefinition food = getFood(fedStack);
        if (food == null) {
            return List.of();
        }

        int repairedDurability = food.durability() > 0 && mimicling.isDamageableItem() ? Math.min(food.durability(), mimicling.getDamageValue()) : food.durability();
        int usesToAdd = getFedUsesToAdd(food, repairedDurability);
        boolean reducedUses = !food.infiniteUsage() && food.durability() > 0 && usesToAdd < food.usageCount();
        return getPreviewDescription(food, usesToAdd, reducedUses);
    }

    public static List<Component> getPreviewDescription(FoodDefinition food) {
        return getPreviewDescription(food, food.infiniteUsage() ? INFINITE_USES : food.usageCount(), false);
    }

    private static List<Component> getPreviewDescription(FoodDefinition food, int usesToAdd, boolean reducedUses) {
        List<Component> lines = new ArrayList<>();
        lines.add(getPreviewStats(food, usesToAdd, reducedUses));
        for (String description : food.description()) {
            lines.add(toDescriptionComponent(description));
        }
        return lines;
    }

    private static Component getPreviewStats(FoodDefinition food, int usesToAdd, boolean reducedUses) {
        Component durability = food.durability() > 0
                ? Component.translatable(FOOD_TOOLTIP_PREFIX + "durability_amount", food.durability(), Component.translatable(FOOD_TOOLTIP_PREFIX + "durability"))
                : Component.translatable(FOOD_TOOLTIP_PREFIX + "no_durability");
        MutableComponent uses = food.infiniteUsage()
                ? Component.translatable(FOOD_TOOLTIP_PREFIX + "infinite_uses", Component.translatable(FOOD_TOOLTIP_PREFIX + "uses"))
                : Component.translatable(FOOD_TOOLTIP_PREFIX + "use_amount", Math.max(0, usesToAdd), Component.translatable(usesToAdd == 1 ? FOOD_TOOLTIP_PREFIX + "use" : FOOD_TOOLTIP_PREFIX + "uses"));
        if (reducedUses) {
            uses.withStyle(ChatFormatting.RED);
        }

        if (food.durability() <= 0 && (food.infiniteUsage() || food.usageCount() > 0)) {
            return uses;
        }
        if (!food.infiniteUsage() && food.usageCount() <= 0) {
            return durability;
        }
        return Component.translatable(FOOD_TOOLTIP_PREFIX + "stats", durability, uses);
    }

    private static Component toDescriptionComponent(String description) {
        for (int i = 0; i < description.length(); i++) {
            char character = description.charAt(i);
            if (!(character == '.' || character == '_' || character == '-' || character == ':' || Character.isLetterOrDigit(character))) {
                return Component.literal(description);
            }
        }
        return Component.translatable(description);
    }

    public static boolean shouldRemember(FoodDefinition food) {
        return (food.infiniteUsage() || food.usageCount() > 0) && food.hasLastingEffects();
    }

    public static boolean canAcceptFood(ItemStack mimicling, FoodDefinition food, boolean canRepair) {
        if (!shouldRemember(food)) {
            return canRepair || !getOnFedReturnStack(food).isEmpty();
        }
        if (isAtUsageCap(mimicling, food)) {
            return canRepair;
        }
        if (!food.infiniteUsage() && food.usageCount() > 0 && food.durability() > 0 && !canRepair) {
            return false;
        }
        return true;
    }

    public static List<ItemStack> rememberFood(ItemStack mimicling, FoodDefinition food, ItemStack fedStack, int preferredReplacementIndex, int repairedDurability) {
        List<ItemStack> returnedItems = new ArrayList<>();
        if (!shouldRemember(food)) {
            return returnedItems;
        }

        int usesToAdd = getFedUsesToAdd(food, repairedDurability);
        if (!food.infiniteUsage() && usesToAdd <= 0) {
            return returnedItems;
        }

        CompoundTag tag = mimicling.getOrCreateTag();
        ListTag activeFoods = tag.getList(ACTIVE_FOODS_TAG, 10);
        ListTag updatedFoods = new ListTag();

        boolean mergedExistingFood = false;
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i).copy();
            if (food.id().equals(activeFood.getString(ACTIVE_FOOD_ID_TAG))) {
                activeFood.putString(ACTIVE_FOOD_ITEM_TAG, BuiltInRegistries.ITEM.getKey(fedStack.getItem()).toString());
                activeFood.putInt(ACTIVE_FOOD_USES_TAG, getRememberedUses(activeFood, food, usesToAdd));
                activeFood.putInt(ACTIVE_FOOD_MAX_USES_TAG, getMaxUses(food));
                mergedExistingFood = true;
            }
            updatedFoods.add(activeFood);
        }

        if (mergedExistingFood) {
            tag.put(ACTIVE_FOODS_TAG, updatedFoods);
            MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
            return returnedItems;
        }

        CompoundTag activeFood = new CompoundTag();
        activeFood.putString(ACTIVE_FOOD_ID_TAG, food.id());
        activeFood.putString(ACTIVE_FOOD_ITEM_TAG, BuiltInRegistries.ITEM.getKey(fedStack.getItem()).toString());
        activeFood.putInt(ACTIVE_FOOD_USES_TAG, food.infiniteUsage() ? INFINITE_USES : usesToAdd);
        activeFood.putInt(ACTIVE_FOOD_MAX_USES_TAG, getMaxUses(food));
        if (updatedFoods.size() >= MAX_ACTIVE_FOODS && preferredReplacementIndex >= 0 && preferredReplacementIndex < updatedFoods.size()) {
            CompoundTag removedFood = updatedFoods.getCompound(preferredReplacementIndex).copy();
            collectReplacementReturn(removedFood, returnedItems);
            updatedFoods.set(preferredReplacementIndex, activeFood);
        } else {
            updatedFoods.add(activeFood);
            while (updatedFoods.size() > MAX_ACTIVE_FOODS) {
                CompoundTag removedFood = updatedFoods.getCompound(0).copy();
                collectReplacementReturn(removedFood, returnedItems);
                updatedFoods.remove(0);
            }
        }
        tag.put(ACTIVE_FOODS_TAG, updatedFoods);
        MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
        return returnedItems;
    }

    private static int getFedUsesToAdd(FoodDefinition food, int repairedDurability) {
        if (food.infiniteUsage()) {
            return INFINITE_USES;
        }
        if (food.durability() <= 0) {
            return food.usageCount();
        }
        int usedDurability = Math.max(0, Math.min(food.durability(), repairedDurability));
        return food.usageCount() * usedDurability / food.durability();
    }

    private static int getRememberedUses(CompoundTag activeFood, FoodDefinition food, int usesToAdd) {
        if (food.infiniteUsage() || activeFood.getInt(ACTIVE_FOOD_USES_TAG) == INFINITE_USES) {
            return INFINITE_USES;
        }
        return Math.min(getMaxUses(food), activeFood.getInt(ACTIVE_FOOD_USES_TAG) + usesToAdd);
    }

    private static boolean isAtUsageCap(ItemStack mimicling, FoodDefinition food) {
        if (!mimicling.hasTag()) {
            return false;
        }

        ListTag activeFoods = mimicling.getTag().getList(ACTIVE_FOODS_TAG, 10);
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i);
            if (!food.id().equals(activeFood.getString(ACTIVE_FOOD_ID_TAG))) {
                continue;
            }
            if (food.infiniteUsage() || activeFood.getInt(ACTIVE_FOOD_USES_TAG) == INFINITE_USES) {
                return true;
            }
            return activeFood.getInt(ACTIVE_FOOD_USES_TAG) >= getActiveFoodMaxUses(activeFood, food);
        }
        return false;
    }

    private static int getMaxUses(FoodDefinition food) {
        return food.infiniteUsage() ? INFINITE_USES : food.usageCount() * MAX_USAGE_STACKS;
    }

    private static int getActiveFoodMaxUses(CompoundTag activeFood, FoodDefinition food) {
        if (activeFood.contains(ACTIVE_FOOD_MAX_USES_TAG)) {
            return activeFood.getInt(ACTIVE_FOOD_MAX_USES_TAG);
        }
        return getMaxUses(food);
    }

    private static void collectReplacementReturn(CompoundTag activeFood, List<ItemStack> returnedItems) {
        FoodDefinition food = FOODS_BY_ID.get(activeFood.getString(ACTIVE_FOOD_ID_TAG));
        if (food == null || !food.returnOnReplacement()) {
            return;
        }

        ItemStack returnStack = getDisplayStack(activeFood, food);
        if (!returnStack.isEmpty()) {
            returnedItems.add(returnStack);
        }
    }

    public static List<FoodDefinition> getActiveFoods(ItemStack mimicling) {
        List<FoodDefinition> foods = new ArrayList<>();
        for (ActiveFood activeFood : getActiveFoodEntries(mimicling)) {
            foods.add(activeFood.food());
        }
        return foods;
    }

    public static List<EffectDefinition> getActiveEffects(ItemStack mimicling) {
        List<FoodDefinition> activeFoods = getActiveFoods(mimicling);
        Set<String> activeFoodIds = new HashSet<>();
        for (FoodDefinition food : activeFoods) {
            activeFoodIds.add(food.id());
        }

        Set<String> disabledEffectIds = new HashSet<>();
        List<EffectDefinition> synergyEffects = new ArrayList<>();
        for (FoodDefinition food : activeFoods) {
            for (SynergyEffectDefinition synergyEffect : food.synergyEffects()) {
                if (!activeFoodIds.contains(synergyEffect.withItem())) {
                    continue;
                }

                disabledEffectIds.addAll(synergyEffect.disabledEffectIds());
                synergyEffects.addAll(synergyEffect.effects());
            }
        }

        List<EffectDefinition> effects = new ArrayList<>();
        for (FoodDefinition food : activeFoods) {
            for (EffectDefinition effect : food.effects()) {
                if (!disabledEffectIds.contains(effect.id())) {
                    effects.add(effect);
                }
            }
        }
        effects.addAll(synergyEffects);
        return effects;
    }

    public static List<ActiveFood> getActiveFoodEntries(ItemStack mimicling) {
        List<ActiveFood> foods = new ArrayList<>();
        if (!mimicling.hasTag()) {
            return foods;
        }

        ListTag activeFoods = mimicling.getTag().getList(ACTIVE_FOODS_TAG, 10);
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i);
            FoodDefinition food = FOODS_BY_ID.get(activeFood.getString(ACTIVE_FOOD_ID_TAG));
            if (food != null) {
                foods.add(new ActiveFood(food, getDisplayStack(activeFood, food), activeFood.getInt(ACTIVE_FOOD_USES_TAG), getActiveFoodMaxUses(activeFood, food)));
            }
        }
        return foods;
    }

    public static boolean hasActiveFood(ItemStack mimicling, String foodId) {
        if (!mimicling.hasTag()) {
            return false;
        }

        ListTag activeFoods = mimicling.getTag().getList(ACTIVE_FOODS_TAG, 10);
        for (int i = 0; i < activeFoods.size(); i++) {
            if (foodId.equals(activeFoods.getCompound(i).getString(ACTIVE_FOOD_ID_TAG))) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack getDisplayStack(CompoundTag activeFood, FoodDefinition food) {
        if (activeFood.contains(ACTIVE_FOOD_ITEM_TAG)) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(activeFood.getString(ACTIVE_FOOD_ITEM_TAG)));
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }

        if (!food.id().startsWith("#")) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(food.id()));
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasActiveEffect(ItemStack mimicling, String type, @Nullable String action) {
        if (!mimicling.hasTag()) {
            return false;
        }

        for (EffectDefinition effect : getActiveEffects(mimicling)) {
            if (effect.matches(type, action)) {
                return true;
            }
        }
        return false;
    }

    public static void consumeUsage(ItemStack mimicling) {
        if (!mimicling.hasTag()) {
            return;
        }

        CompoundTag tag = mimicling.getOrCreateTag();
        Set<String> handledFoods = getHandledUsageFoods(tag);
        Set<String> actionManagedFoods = getActionManagedUsageFoods(mimicling);
        ListTag activeFoods = tag.getList(ACTIVE_FOODS_TAG, 10);
        ListTag remainingFoods = new ListTag();
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i).copy();
            String foodId = activeFood.getString(ACTIVE_FOOD_ID_TAG);
            if (handledFoods.contains(foodId) || actionManagedFoods.contains(foodId)) {
                remainingFoods.add(activeFood);
                continue;
            }
            if (activeFood.getInt(ACTIVE_FOOD_USES_TAG) == INFINITE_USES) {
                remainingFoods.add(activeFood);
                continue;
            }

            int uses = activeFood.getInt(ACTIVE_FOOD_USES_TAG) - 1;
            if (uses > 0) {
                activeFood.putInt(ACTIVE_FOOD_USES_TAG, uses);
                remainingFoods.add(activeFood);
            }
        }

        if (remainingFoods.isEmpty()) {
            tag.remove(ACTIVE_FOODS_TAG);
        } else {
            tag.put(ACTIVE_FOODS_TAG, remainingFoods);
        }
        tag.remove(USAGE_HANDLED_FOODS_TAG);
        MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
    }

    private static Set<String> getActionManagedUsageFoods(ItemStack mimicling) {
        Set<String> actionManagedFoods = new HashSet<>();
        for (EffectDefinition effect : getActiveEffects(mimicling)) {
            if (effect.matches("while_in_hand", "remove_underwater_mining_penalty")
                    || effect.matches("while_in_hand", "extend_reach")
                    || effect.matches("while_in_inventory", "drain_durability_over_time")
                    || effect.matches("on_break", "grant_air")
                    || effect.matches("on_break", "trail_to_matching_block")
                    || effect.matches("change_drop", "auto_smelt")
                    || effect.matches("change_drop", "collect_drops_to_inventory")
                    || effect.matches("change_drop", "drop_loot_as_falling_blocks")
                    || effect.matches("on_kill", "trail_to_matching_entity")
                    || effect.matches("on_kill", "summon_entities_at_death")
                    || effect.matches("on_kill", "roll_effect_group")) {
                actionManagedFoods.add(effect.ownerId());
            }
        }
        return actionManagedFoods;
    }

    public static boolean consumeUsage(ItemStack mimicling, String foodId, int amount) {
        if (amount <= 0 || !mimicling.hasTag()) {
            return false;
        }

        CompoundTag tag = mimicling.getOrCreateTag();
        ListTag activeFoods = tag.getList(ACTIVE_FOODS_TAG, 10);
        ListTag remainingFoods = new ListTag();
        boolean consumed = false;
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i).copy();
            if (foodId.equals(activeFood.getString(ACTIVE_FOOD_ID_TAG))) {
                if (activeFood.getInt(ACTIVE_FOOD_USES_TAG) == INFINITE_USES) {
                    remainingFoods.add(activeFood);
                    consumed = true;
                    continue;
                }
                int uses = activeFood.getInt(ACTIVE_FOOD_USES_TAG) - amount;
                consumed = true;
                if (uses > 0) {
                    activeFood.putInt(ACTIVE_FOOD_USES_TAG, uses);
                    remainingFoods.add(activeFood);
                }
            } else {
                remainingFoods.add(activeFood);
            }
        }

        if (remainingFoods.isEmpty()) {
            tag.remove(ACTIVE_FOODS_TAG);
        } else {
            tag.put(ACTIVE_FOODS_TAG, remainingFoods);
        }
        MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
        return consumed;
    }

    public static void markUsageHandled(ItemStack mimicling, String foodId) {
        CompoundTag tag = mimicling.getOrCreateTag();
        Set<String> handledFoods = getHandledUsageFoods(tag);
        if (handledFoods.contains(foodId)) {
            return;
        }

        ListTag handled = tag.getList(USAGE_HANDLED_FOODS_TAG, 8);
        handled.add(StringTag.valueOf(foodId));
        tag.put(USAGE_HANDLED_FOODS_TAG, handled);
    }

    public static boolean isUsageHandled(ItemStack mimicling, String foodId) {
        return mimicling.hasTag() && getHandledUsageFoods(mimicling.getTag()).contains(foodId);
    }

    private static Set<String> getHandledUsageFoods(CompoundTag tag) {
        Set<String> handledFoods = new HashSet<>();
        ListTag handled = tag.getList(USAGE_HANDLED_FOODS_TAG, 8);
        for (int i = 0; i < handled.size(); i++) {
            handledFoods.add(handled.getString(i));
        }
        return handledFoods;
    }

    private static void replaceAll(Map<String, FoodDefinition> foodsById, Map<Item, FoodDefinition> itemFoods, List<TagFoodEntry> tagFoods) {
        FOODS_BY_ID.clear();
        FOODS_BY_ID.putAll(foodsById);
        ITEM_FOODS.clear();
        ITEM_FOODS.putAll(itemFoods);
        TAG_FOODS.clear();
        TAG_FOODS.addAll(tagFoods);
    }

    public record FoodDefinition(String id, int durability, int usageCount, boolean infiniteUsage, boolean returnOnReplacement, Item returnItem, List<String> description, List<EffectDefinition> effects, List<SynergyEffectDefinition> synergyEffects) {
        public boolean hasLastingEffects() {
            for (EffectDefinition effect : effects) {
                if (!"on_fed".equals(effect.trigger())) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasEffect(String type, @Nullable String action) {
            for (EffectDefinition effect : effects) {
                if (effect.matches(type, action)) {
                    return true;
                }
            }
            return false;
        }

    }

    public record ActiveFood(FoodDefinition food, ItemStack displayStack, int uses, int maxUses) {}

    public record EffectDefinition(String ownerId, String trigger, String action, JsonObject data) {
        public boolean matches(String trigger, @Nullable String action) {
            return this.trigger.equals(trigger) && (action == null || action.equals(this.action));
        }

        public String id() {
            return data.has("id") ? data.get("id").getAsString() : "";
        }
    }

    public record SynergyEffectDefinition(String withItem, Set<String> disabledEffectIds, List<EffectDefinition> effects) {}

    private record PendingSynergyDefinition(String ownerId, String withItem, Set<String> disabledEffectIds, List<EffectDefinition> effects) {}

    public record TagFoodEntry(TagKey<Item> tag, FoodDefinition food) {}

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private final Logger logger;

        public ReloadListener(Gson gson, Logger logger) {
            super(gson, "behaviour");
            this.logger = logger;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<Item, FoodDefinition> itemFoods = new HashMap<>();
            Map<String, FoodDefinition> foodsById = new HashMap<>();
            List<TagFoodEntry> tagFoods = new ArrayList<>();
            List<PendingSynergyDefinition> pendingSynergies = new ArrayList<>();

            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
                String path = entry.getKey().getPath();
                boolean isFoodFile = path.equals("mimicling") || path.startsWith("mimicling/");
                boolean isSynergyFile = path.equals("mimicling_synergies") || path.startsWith("mimicling_synergies/");
                if (!isFoodFile && !isSynergyFile) {
                    continue;
                }

                try {
                    JsonElement element = entry.getValue();
                    if (!element.isJsonObject()) {
                        logger.warn("Mimicling food {} must be a JSON object.", entry.getKey());
                        continue;
                    }

                    JsonObject object = element.getAsJsonObject();
                    if (isSynergyFile) {
                        parseSynergyFile(entry.getKey(), object, pendingSynergies);
                        continue;
                    }

                    if (object.has("entries") && object.get("entries").isJsonArray()) {
                        JsonArray entries = object.getAsJsonArray("entries");
                        for (JsonElement foodElement : entries) {
                            if (foodElement.isJsonObject()) {
                                parseEntry(entry.getKey(), foodElement.getAsJsonObject(), foodsById, itemFoods, tagFoods);
                            }
                        }
                        continue;
                    }

                    parseEntry(entry.getKey(), object, foodsById, itemFoods, tagFoods);
                } catch (Exception exception) {
                    logger.error("Failed to load Mimicling food {}: {}", entry.getKey(), exception.toString());
                }
            }

            applyPendingSynergies(foodsById, pendingSynergies);
            syncFoodDefinitions(foodsById, itemFoods, tagFoods);
            replaceAll(foodsById, itemFoods, tagFoods);
            logger.info("Loaded {} Mimicling food item entries, {} tag entries, and {} synergies.", itemFoods.size(), tagFoods.size(), pendingSynergies.size());
        }

        private void parseEntry(ResourceLocation fileId, JsonObject object, Map<String, FoodDefinition> foodsById, Map<Item, FoodDefinition> itemFoods, List<TagFoodEntry> tagFoods) {
            int repairAmount = object.has("durability") ? object.get("durability").getAsInt() : 0;
            if (repairAmount < 0) {
                logger.warn("Mimicling food {} has negative durability.", fileId);
                return;
            }

            if (object.has("item")) {
                ResourceLocation itemId = new ResourceLocation(object.get("item").getAsString());
                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                if (item == null) {
                    logger.warn("Mimicling food {} references unknown item {}.", fileId, itemId);
                    return;
                }
                FoodDefinition food = parseFoodDefinition(getFoodId(object, itemId.toString()), fileId, object, repairAmount);
                foodsById.put(food.id(), food);
                itemFoods.put(item, food);
                return;
            }

            if (object.has("tag")) {
                ResourceLocation tagId = new ResourceLocation(object.get("tag").getAsString());
                FoodDefinition food = parseFoodDefinition(getFoodId(object, "#" + tagId), fileId, object, repairAmount);
                foodsById.put(food.id(), food);
                tagFoods.add(new TagFoodEntry(TagKey.create(BuiltInRegistries.ITEM.key(), tagId), food));
                return;
            }

            logger.warn("Mimicling food {} must define item or tag.", fileId);
        }

        private String getFoodId(JsonObject object, String fallback) {
            return object.has("food_id") ? object.get("food_id").getAsString() : fallback;
        }

        private FoodDefinition parseFoodDefinition(String id, ResourceLocation fileId, JsonObject object, int repairAmount) {
            int usageCount = object.has("usage_count") ? object.get("usage_count").getAsInt() : 0;
            boolean infiniteUsage = object.has("infinite_usage") && object.get("infinite_usage").getAsBoolean();
            boolean returnOnReplacement = object.has("return_on_replacement") ? object.get("return_on_replacement").getAsBoolean() : infiniteUsage;
            List<String> description = parseDescription(object);
            List<EffectDefinition> effects = parseEffects(object, id);
            List<SynergyEffectDefinition> synergyEffects = parseSynergyEffects(object, id);
            Item returnItem = parseReturnItem(fileId, object, effects);
            return new FoodDefinition(id, repairAmount, Math.max(0, usageCount), infiniteUsage, returnOnReplacement, returnItem, description, effects, synergyEffects);
        }

        private List<String> parseDescription(JsonObject object) {
            List<String> description = new ArrayList<>();
            if (!object.has("description")) {
                return description;
            }

            JsonElement element = object.get("description");
            if (element.isJsonArray()) {
                for (JsonElement line : element.getAsJsonArray()) {
                    description.add(line.getAsString());
                }
            } else {
                description.add(element.getAsString());
            }
            return description;
        }

        private Item parseReturnItem(ResourceLocation fileId, JsonObject object, List<EffectDefinition> effects) {
            for (EffectDefinition effect : effects) {
                if (effect.matches("on_fed", "return_item") && effect.data().has("return_item")) {
                    return parseReturnItemId(fileId, effect.data().get("return_item").getAsString());
                }
            }

            if (object.has("on_fed")) {
                JsonObject onFed = object.getAsJsonObject("on_fed");
                if (onFed.has("return_item")) {
                    return parseReturnItemId(fileId, onFed.get("return_item").getAsString());
                }
            }

            return null;
        }

        private Item parseReturnItemId(ResourceLocation fileId, String rawItemId) {
            ResourceLocation itemId = new ResourceLocation(rawItemId);
            Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
            if (item == null || item == Items.AIR) {
                logger.warn("Mimicling food {} references unknown return item {}.", fileId, itemId);
                return null;
            }
            return item;
        }

        private List<EffectDefinition> parseEffects(JsonObject object, String ownerId) {
            List<EffectDefinition> effects = new ArrayList<>();
            if (!object.has("effects") || !object.get("effects").isJsonArray()) {
                if (object.has("on_fed")) {
                    JsonObject data = object.getAsJsonObject("on_fed");
                    effects.add(new EffectDefinition(ownerId, "on_fed", data.has("action") ? data.get("action").getAsString() : "return_item", data));
                }
                return effects;
            }

            for (JsonElement effectElement : object.getAsJsonArray("effects")) {
                if (!effectElement.isJsonObject()) {
                    continue;
                }

                JsonObject effect = effectElement.getAsJsonObject();
                if (effect.has("trigger") && effect.has("action")) {
                    effects.add(new EffectDefinition(
                            ownerId,
                            effect.get("trigger").getAsString(),
                            effect.get("action").getAsString(),
                            effect.deepCopy()
                    ));
                }
            }
            return effects;
        }

        private List<SynergyEffectDefinition> parseSynergyEffects(JsonObject object, String ownerId) {
            List<SynergyEffectDefinition> synergyEffects = new ArrayList<>();
            if (!object.has("synergy_effects") || !object.get("synergy_effects").isJsonArray()) {
                return synergyEffects;
            }

            for (JsonElement synergyElement : object.getAsJsonArray("synergy_effects")) {
                if (!synergyElement.isJsonObject()) {
                    continue;
                }

                JsonObject synergy = synergyElement.getAsJsonObject();
                if (!synergy.has("with_item")) {
                    continue;
                }
                synergyEffects.add(new SynergyEffectDefinition(
                        synergy.get("with_item").getAsString(),
                        parseDisabledEffectIds(synergy),
                        parseEffects(synergy, ownerId)
                ));
            }
            return synergyEffects;
        }

        private void parseSynergyFile(ResourceLocation fileId, JsonObject object, List<PendingSynergyDefinition> pendingSynergies) {
            if (!object.has("synergies") || !object.get("synergies").isJsonArray()) {
                logger.warn("Mimicling synergy file {} must define a synergies array.", fileId);
                return;
            }

            for (JsonElement synergyElement : object.getAsJsonArray("synergies")) {
                if (!synergyElement.isJsonObject()) {
                    continue;
                }

                JsonObject synergy = synergyElement.getAsJsonObject();
                if (!synergy.has("items") || !synergy.get("items").isJsonArray() || synergy.getAsJsonArray("items").size() != 2) {
                    logger.warn("Mimicling synergy {} must define exactly two items.", fileId);
                    continue;
                }

                JsonArray items = synergy.getAsJsonArray("items");
                String ownerId = items.get(0).getAsString();
                String withItem = items.get(1).getAsString();
                pendingSynergies.add(new PendingSynergyDefinition(
                        ownerId,
                        withItem,
                        parseDisabledEffectIds(synergy),
                        parseEffects(synergy, ownerId)
                ));
            }
        }

        private void applyPendingSynergies(Map<String, FoodDefinition> foodsById, List<PendingSynergyDefinition> pendingSynergies) {
            for (PendingSynergyDefinition pending : pendingSynergies) {
                FoodDefinition owner = foodsById.get(pending.ownerId());
                if (owner == null) {
                    logger.warn("Mimicling synergy references unknown owner food {}.", pending.ownerId());
                    continue;
                }
                if (!foodsById.containsKey(pending.withItem())) {
                    logger.warn("Mimicling synergy {} references unknown paired food {}.", pending.ownerId(), pending.withItem());
                    continue;
                }

                List<SynergyEffectDefinition> synergies = new ArrayList<>(owner.synergyEffects());
                synergies.add(new SynergyEffectDefinition(pending.withItem(), pending.disabledEffectIds(), pending.effects()));
                foodsById.put(owner.id(), new FoodDefinition(owner.id(), owner.durability(), owner.usageCount(), owner.infiniteUsage(), owner.returnOnReplacement(), owner.returnItem(), owner.description(), owner.effects(), synergies));
            }
        }

        private void syncFoodDefinitions(Map<String, FoodDefinition> foodsById, Map<Item, FoodDefinition> itemFoods, List<TagFoodEntry> tagFoods) {
            for (Map.Entry<Item, FoodDefinition> entry : itemFoods.entrySet()) {
                FoodDefinition shared = foodsById.get(entry.getValue().id());
                if (shared != null) {
                    entry.setValue(withSharedSynergies(entry.getValue(), shared));
                }
            }

            for (int i = 0; i < tagFoods.size(); i++) {
                TagFoodEntry entry = tagFoods.get(i);
                FoodDefinition shared = foodsById.get(entry.food().id());
                if (shared != null) {
                    tagFoods.set(i, new TagFoodEntry(entry.tag(), withSharedSynergies(entry.food(), shared)));
                }
            }
        }

        private FoodDefinition withSharedSynergies(FoodDefinition food, FoodDefinition shared) {
            return new FoodDefinition(
                    food.id(),
                    food.durability(),
                    food.usageCount(),
                    food.infiniteUsage(),
                    food.returnOnReplacement(),
                    food.returnItem(),
                    food.description(),
                    food.effects(),
                    shared.synergyEffects()
            );
        }

        private Set<String> parseDisabledEffectIds(JsonObject object) {
            return parseEffectIdList(object, "disable");
        }

        private Set<String> parseEffectIdList(JsonObject object, String field) {
            Set<String> effectIds = new HashSet<>();
            if (!object.has(field) || !object.get(field).isJsonArray()) {
                return effectIds;
            }

            for (JsonElement element : object.getAsJsonArray(field)) {
                effectIds.add(element.getAsString());
            }
            return effectIds;
        }
    }
}
