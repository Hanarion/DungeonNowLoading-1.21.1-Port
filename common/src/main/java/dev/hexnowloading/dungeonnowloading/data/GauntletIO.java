package dev.hexnowloading.dungeonnowloading.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class GauntletIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File baseDir() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        return new File(gameDir, "dnl/gauntlet");
    }

    public static boolean save(String name, int relX, int relY, int relZ,
                               int sizeX, int sizeY, int sizeZ,
                               int activationRange,
                               List<String> waves,
                               String lootTable) {
        if (name == null || name.isBlank()) return false;
        try {
            File dir = baseDir();
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, name + ".json");

            JsonObject root = new JsonObject();
            JsonObject rel = new JsonObject(); rel.addProperty("x", relX); rel.addProperty("y", relY); rel.addProperty("z", relZ);
            JsonObject size = new JsonObject(); size.addProperty("x", sizeX); size.addProperty("y", sizeY); size.addProperty("z", sizeZ);
            root.add("relative_pos", rel);
            root.add("gauntlet_size", size);
            root.addProperty("activation_range", activationRange);
            JsonArray wavesArr = new JsonArray();
            if (waves != null) for (String w : waves) wavesArr.add(w == null ? "" : w);
            root.add("waves", wavesArr);
            root.addProperty("loot_table", lootTable == null ? "" : lootTable);

            try (FileWriter fw = new FileWriter(out)) {
                GSON.toJson(root, fw);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static LoadedGauntlet load(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            File file = new File(baseDir(), name + ".json");
            if (!file.exists()) return null;
            try (FileReader fr = new FileReader(file)) {
                JsonObject root = GSON.fromJson(fr, JsonObject.class);
                JsonObject rel = root.getAsJsonObject("relative_pos");
                JsonObject size = root.getAsJsonObject("gauntlet_size");
                int relX = rel.get("x").getAsInt();
                int relY = rel.get("y").getAsInt();
                int relZ = rel.get("z").getAsInt();
                int sizeX = size.get("x").getAsInt();
                int sizeY = size.get("y").getAsInt();
                int sizeZ = size.get("z").getAsInt();
                int activationRange = root.get("activation_range").getAsInt();
                String loot = root.has("loot_table") ? root.get("loot_table").getAsString() : "";
                List<String> waves = new ArrayList<>();
                JsonArray arr = root.getAsJsonArray("waves");
                if (arr != null) arr.forEach(el -> waves.add(el.getAsString()));
                return new LoadedGauntlet(relX, relY, relZ, sizeX, sizeY, sizeZ, activationRange, loot, waves);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public record LoadedGauntlet(int relX, int relY, int relZ,
                                 int sizeX, int sizeY, int sizeZ,
                                 int activationRange,
                                 String lootTable,
                                 List<String> waves) {}
}

