package dev.hexnowloading.dungeonnowloading.item;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MimiclingFoods {
    private static final Map<Item, Integer> ITEM_REPAIR_AMOUNTS = new HashMap<>();
    private static final List<TagRepairEntry> TAG_REPAIR_AMOUNTS = new ArrayList<>();

    private MimiclingFoods() {}

    public static int getRepairAmount(ItemStack stack) {
        Integer itemAmount = ITEM_REPAIR_AMOUNTS.get(stack.getItem());
        if (itemAmount != null) {
            return itemAmount;
        }

        int tagAmount = 0;
        for (TagRepairEntry entry : TAG_REPAIR_AMOUNTS) {
            if (stack.is(entry.tag())) {
                tagAmount = Math.max(tagAmount, entry.repairAmount());
            }
        }
        return tagAmount;
    }

    private static void replaceAll(Map<Item, Integer> itemRepairAmounts, List<TagRepairEntry> tagRepairAmounts) {
        ITEM_REPAIR_AMOUNTS.clear();
        ITEM_REPAIR_AMOUNTS.putAll(itemRepairAmounts);
        TAG_REPAIR_AMOUNTS.clear();
        TAG_REPAIR_AMOUNTS.addAll(tagRepairAmounts);
    }

    public record TagRepairEntry(TagKey<Item> tag, int repairAmount) {}

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private final Logger logger;

        public ReloadListener(Gson gson, Logger logger) {
            super(gson, "behaviour");
            this.logger = logger;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<Item, Integer> itemRepairAmounts = new HashMap<>();
            List<TagRepairEntry> tagRepairAmounts = new ArrayList<>();

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
                                parseEntry(entry.getKey(), foodElement.getAsJsonObject(), itemRepairAmounts, tagRepairAmounts);
                            }
                        }
                        continue;
                    }

                    parseEntry(entry.getKey(), object, itemRepairAmounts, tagRepairAmounts);
                } catch (Exception exception) {
                    logger.error("Failed to load Mimicling food {}: {}", entry.getKey(), exception.toString());
                }
            }

            replaceAll(itemRepairAmounts, tagRepairAmounts);
            logger.info("Loaded {} Mimicling food item entries and {} tag entries.", itemRepairAmounts.size(), tagRepairAmounts.size());
        }

        private void parseEntry(ResourceLocation fileId, JsonObject object, Map<Item, Integer> itemRepairAmounts, List<TagRepairEntry> tagRepairAmounts) {
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
                itemRepairAmounts.put(item, repairAmount);
                return;
            }

            if (object.has("tag")) {
                ResourceLocation tagId = new ResourceLocation(object.get("tag").getAsString());
                tagRepairAmounts.add(new TagRepairEntry(TagKey.create(BuiltInRegistries.ITEM.key(), tagId), repairAmount));
                return;
            }

            logger.warn("Mimicling food {} must define item or tag.", fileId);
        }
    }
}
