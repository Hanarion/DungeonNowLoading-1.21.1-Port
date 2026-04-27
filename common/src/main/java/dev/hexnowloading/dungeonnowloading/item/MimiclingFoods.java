package dev.hexnowloading.dungeonnowloading.item;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private static final int MAX_ACTIVE_FOODS = 2;
    private static final Map<String, FoodDefinition> FOODS_BY_ID = new HashMap<>();
    private static final Map<Item, FoodDefinition> ITEM_FOODS = new HashMap<>();
    private static final List<TagFoodEntry> TAG_FOODS = new ArrayList<>();

    private MimiclingFoods() {}

    public static int getRepairAmount(ItemStack stack) {
        FoodDefinition food = getFood(stack);
        return food == null ? 0 : food.durability();
    }

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

    public static boolean shouldRemember(FoodDefinition food) {
        return food.usageCount() > 0 && food.hasLastingEffects();
    }

    public static void rememberFood(ItemStack mimicling, FoodDefinition food, ItemStack fedStack) {
        if (!shouldRemember(food)) {
            return;
        }

        CompoundTag tag = mimicling.getOrCreateTag();
        ListTag activeFoods = tag.getList(ACTIVE_FOODS_TAG, 10);
        ListTag updatedFoods = new ListTag();

        boolean mergedExistingFood = false;
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i).copy();
            if (food.id().equals(activeFood.getString(ACTIVE_FOOD_ID_TAG))) {
                activeFood.putString(ACTIVE_FOOD_ITEM_TAG, BuiltInRegistries.ITEM.getKey(fedStack.getItem()).toString());
                activeFood.putInt(ACTIVE_FOOD_USES_TAG, activeFood.getInt(ACTIVE_FOOD_USES_TAG) + food.usageCount());
                mergedExistingFood = true;
            }
            updatedFoods.add(activeFood);
        }

        if (mergedExistingFood) {
            tag.put(ACTIVE_FOODS_TAG, updatedFoods);
            MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
            return;
        }

        CompoundTag activeFood = new CompoundTag();
        activeFood.putString(ACTIVE_FOOD_ID_TAG, food.id());
        activeFood.putString(ACTIVE_FOOD_ITEM_TAG, BuiltInRegistries.ITEM.getKey(fedStack.getItem()).toString());
        activeFood.putInt(ACTIVE_FOOD_USES_TAG, food.usageCount());
        updatedFoods.add(activeFood);
        while (updatedFoods.size() > MAX_ACTIVE_FOODS) {
            updatedFoods.remove(0);
        }
        tag.put(ACTIVE_FOODS_TAG, updatedFoods);
        MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
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
                foods.add(new ActiveFood(food, getDisplayStack(activeFood, food), activeFood.getInt(ACTIVE_FOOD_USES_TAG)));
            }
        }
        return foods;
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
        ListTag activeFoods = tag.getList(ACTIVE_FOODS_TAG, 10);
        ListTag remainingFoods = new ListTag();
        for (int i = 0; i < activeFoods.size(); i++) {
            CompoundTag activeFood = activeFoods.getCompound(i).copy();
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
        MimiclingFoodEffects.retainMemoriesForActiveFoods(mimicling, getActiveFoods(mimicling));
    }

    private static void replaceAll(Map<String, FoodDefinition> foodsById, Map<Item, FoodDefinition> itemFoods, List<TagFoodEntry> tagFoods) {
        FOODS_BY_ID.clear();
        FOODS_BY_ID.putAll(foodsById);
        ITEM_FOODS.clear();
        ITEM_FOODS.putAll(itemFoods);
        TAG_FOODS.clear();
        TAG_FOODS.addAll(tagFoods);
    }

    public record FoodDefinition(String id, int durability, int usageCount, Item returnItem, List<EffectDefinition> effects, List<SynergyEffectDefinition> synergyEffects) {
        public boolean hasLastingEffects() {
            for (EffectDefinition effect : effects) {
                if (!"on_fed".equals(effect.type())) {
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

    public record ActiveFood(FoodDefinition food, ItemStack displayStack, int uses) {}

    public record EffectDefinition(String type, @Nullable String action, JsonObject data) {
        public boolean matches(String type, @Nullable String action) {
            return this.type.equals(type) && (action == null || action.equals(this.action));
        }

        public String id() {
            return data.has("id") ? data.get("id").getAsString() : "";
        }
    }

    public record SynergyEffectDefinition(String withItem, Set<String> disabledEffectIds, List<EffectDefinition> effects) {}

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

            for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
                String path = entry.getKey().getPath();
                if (!path.equals("mimicling") && !path.startsWith("mimicling/")) {
                    continue;
                }

                try {
                    JsonElement element = entry.getValue();
                    if (!element.isJsonObject()) {
                        logger.warn("Mimicling food {} must be a JSON object.", entry.getKey());
                        continue;
                    }

                    JsonObject object = element.getAsJsonObject();
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

            replaceAll(foodsById, itemFoods, tagFoods);
            logger.info("Loaded {} Mimicling food item entries and {} tag entries.", itemFoods.size(), tagFoods.size());
        }

        private void parseEntry(ResourceLocation fileId, JsonObject object, Map<String, FoodDefinition> foodsById, Map<Item, FoodDefinition> itemFoods, List<TagFoodEntry> tagFoods) {
            int repairAmount = object.has("durability") ? object.get("durability").getAsInt() : 0;
            if (repairAmount <= 0) {
                logger.warn("Mimicling food {} has non-positive durability.", fileId);
                return;
            }

            if (object.has("item")) {
                ResourceLocation itemId = new ResourceLocation(object.get("item").getAsString());
                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                if (item == null) {
                    logger.warn("Mimicling food {} references unknown item {}.", fileId, itemId);
                    return;
                }
                FoodDefinition food = parseFoodDefinition(itemId.toString(), fileId, object, repairAmount);
                foodsById.put(food.id(), food);
                itemFoods.put(item, food);
                return;
            }

            if (object.has("tag")) {
                ResourceLocation tagId = new ResourceLocation(object.get("tag").getAsString());
                FoodDefinition food = parseFoodDefinition("#" + tagId, fileId, object, repairAmount);
                foodsById.put(food.id(), food);
                tagFoods.add(new TagFoodEntry(TagKey.create(BuiltInRegistries.ITEM.key(), tagId), food));
                return;
            }

            logger.warn("Mimicling food {} must define item or tag.", fileId);
        }

        private FoodDefinition parseFoodDefinition(String id, ResourceLocation fileId, JsonObject object, int repairAmount) {
            int usageCount = object.has("usage_count") ? object.get("usage_count").getAsInt() : 0;
            List<EffectDefinition> effects = parseEffects(object);
            List<SynergyEffectDefinition> synergyEffects = parseSynergyEffects(object);
            Item returnItem = parseReturnItem(fileId, object, effects);
            return new FoodDefinition(id, repairAmount, Math.max(0, usageCount), returnItem, effects, synergyEffects);
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

        private List<EffectDefinition> parseEffects(JsonObject object) {
            List<EffectDefinition> effects = new ArrayList<>();
            if (!object.has("effects") || !object.get("effects").isJsonArray()) {
                if (object.has("on_fed")) {
                    JsonObject data = object.getAsJsonObject("on_fed");
                    effects.add(new EffectDefinition("on_fed", data.has("action") ? data.get("action").getAsString() : "return_item", data));
                }
                return effects;
            }

            for (JsonElement effectElement : object.getAsJsonArray("effects")) {
                if (!effectElement.isJsonObject()) {
                    continue;
                }

                JsonObject effect = effectElement.getAsJsonObject();
                if (effect.has("type")) {
                    effects.add(new EffectDefinition(
                            effect.get("type").getAsString(),
                            effect.has("action") ? effect.get("action").getAsString() : null,
                            effect.deepCopy()
                    ));
                }
            }
            return effects;
        }

        private List<SynergyEffectDefinition> parseSynergyEffects(JsonObject object) {
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
                        parseEffects(synergy)
                ));
            }
            return synergyEffects;
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
