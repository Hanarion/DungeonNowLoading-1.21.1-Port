package dev.hexnowloading.dungeonnowloading.wave;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WaveIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File worldDir(MinecraftServer server) {
        Path root = server.getWorldPath(LevelResource.ROOT);
        return root == null ? server.getFile("dnl/waves") : root.resolve("dnl/waves").toFile();
    }

    // Use only the last path segment of the wave id as the filename (e.g. wave_1.json)
    private static String fileName(String id) {
        if (id == null || id.isBlank()) return null;
        // id is like "dungeonnowloading:gauntlet/gauntlet1/wave_1"
        int colon = id.indexOf(':');
        String path = colon >= 0 && colon + 1 < id.length() ? id.substring(colon + 1) : id;
        int lastSlash = path.lastIndexOf('/');
        String name = lastSlash >= 0 && lastSlash + 1 < path.length() ? path.substring(lastSlash + 1) : path;
        name = name.trim();
        if (name.isEmpty()) return null;
        return name + ".json";
    }

    public static void save(MinecraftServer server, WaveDefinition def) {
        if (server == null || def == null || def.id() == null) return;
        String fn = fileName(def.id());
        if (fn == null || fn.isBlank()) return;
        File wDir = worldDir(server);
        if (!wDir.exists()) wDir.mkdirs();
        File wFile = new File(wDir, fn);
        try (Writer w = Files.newBufferedWriter(wFile.toPath(), StandardCharsets.UTF_8)) {
            GSON.toJson(def, w);
        } catch (IOException e) {
            System.err.println("[DNL][Wave] Failed world save " + def.id() + ": " + e.getMessage());
        }
    }

    public static WaveDefinition load(MinecraftServer server, String id) {
        if (server == null || id == null || id.isBlank()) return null;
        String fn = fileName(id);
        if (fn == null || fn.isBlank()) return null;
        File wFile = new File(worldDir(server), fn);
        if (wFile.exists()) {
            try (Reader r = Files.newBufferedReader(wFile.toPath(), StandardCharsets.UTF_8)) {
                return GSON.fromJson(r, WaveDefinition.class);
            } catch (Exception ignored) { }
        }
        return null;
    }

    public static List<WaveDefinition> list(MinecraftServer server) {
        if (server == null) return List.of();
        Map<String, WaveDefinition> map = new LinkedHashMap<>(); // maintain insertion order
        File wDir = worldDir(server);
        File[] wFiles = (wDir.exists() && wDir.isDirectory())
                ? wDir.listFiles((d, n) -> n.toLowerCase().endsWith(".json"))
                : null;
        if (wFiles != null) for (File f : wFiles) {
            try (Reader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                WaveDefinition def = GSON.fromJson(r, WaveDefinition.class);
                if (def != null && def.id() != null) map.put(def.id(), def);
            } catch (Exception ignored) { }
        }
        return Collections.unmodifiableList(new ArrayList<>(map.values()));
    }

    public static boolean delete(MinecraftServer server, String id) {
        if (server == null || id == null || id.isBlank()) return false;
        String fn = fileName(id);
        if (fn == null || fn.isBlank()) return false;
        File wFile = new File(worldDir(server), fn);
        return wFile.exists() && wFile.delete();
    }
}
