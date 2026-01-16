package dev.hexnowloading.dungeonnowloading.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class WaveIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File baseDir() {
        File gameDir = Minecraft.getInstance().gameDirectory;
        return new File(gameDir, "dnl/waves");
    }

    public static boolean save(String name, List<MobNode> nodes) {
        if (name == null || name.isBlank()) return false;
        try {
            File dir = baseDir();
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, name + ".json");

            JsonObject root = new JsonObject();
            JsonArray nodesArr = new JsonArray();
            if (nodes != null) {
                for (MobNode n : nodes) {
                    JsonObject nodeObj = new JsonObject();
                    nodeObj.addProperty("name", n.name);
                    JsonObject off = new JsonObject();
                    off.addProperty("dx", n.dx);
                    off.addProperty("dy", n.dy);
                    off.addProperty("dz", n.dz);
                    nodeObj.add("offset", off);
                    nodesArr.add(nodeObj);
                }
            }
            root.add("mob_nodes", nodesArr);

            try (FileWriter fw = new FileWriter(out)) {
                GSON.toJson(root, fw);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<MobNode> load(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            File file = new File(baseDir(), name + ".json");
            if (!file.exists()) return null;
            try (FileReader fr = new FileReader(file)) {
                JsonObject root = GSON.fromJson(fr, JsonObject.class);
                JsonArray arr = root.getAsJsonArray("mob_nodes");
                List<MobNode> nodes = new ArrayList<>();
                if (arr != null) arr.forEach(el -> {
                    JsonObject nodeObj = el.getAsJsonObject();
                    String nodeName = nodeObj.get("name").getAsString();
                    JsonObject off = nodeObj.getAsJsonObject("offset");
                    int dx = off.get("dx").getAsInt();
                    int dy = off.get("dy").getAsInt();
                    int dz = off.get("dz").getAsInt();
                    nodes.add(new MobNode(nodeName, dx, dy, dz));
                });
                return nodes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class MobNode {
        public final String name;
        public final int dx, dy, dz;
        public MobNode(String name, int dx, int dy, int dz) {
            this.name = name; this.dx = dx; this.dy = dy; this.dz = dz;
        }
    }
}

