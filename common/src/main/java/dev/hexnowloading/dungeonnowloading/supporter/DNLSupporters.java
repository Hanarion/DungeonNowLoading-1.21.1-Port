package dev.hexnowloading.dungeonnowloading.supporter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class DNLSupporters {
    private static final String GITHUB_URL = "https://raw.githubusercontent.com/hexnowloading/DungeonNowLoadingSupporterList/refs/heads/main/supporters.json";
    private static final Map<UUID, List<String>> supporters = new HashMap<>();

    public static void loadSupporters() {
        try {
            URL url = new URL(GITHUB_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (Reader reader = new InputStreamReader(conn.getInputStream())) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    List<String> skins = new ArrayList<>();
                    for (JsonElement skinElement : entry.getValue().getAsJsonArray()) {
                        skins.add(skinElement.getAsString());
                    }
                    supporters.put(uuid, skins);
                }

            }
        } catch (Exception e) {
            System.err.println("[DNL] Failed to load supporters.json: " + e.getMessage());
        }
    }

    public static boolean isSupporter(UUID uuid) {
        return supporters.containsKey(uuid);
    }

    public static boolean hasSkin(UUID uuid, String skinName) {
        return supporters.getOrDefault(uuid, List.of()).contains(skinName);
    }

    public static List<String> getSkins(UUID uuid) {
        return supporters.getOrDefault(uuid, List.of());
    }
}
