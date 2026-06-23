package dev.hexnowloading.dungeonnowloading.supporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class PatronRegistry {

    public static final String DEFAULT_URL =
            "https://raw.githubusercontent.com/hexnowloading/DungeonNowLoadingSupporterList/refs/heads/main/statues.json";


    // ======= public API =====================================================
    public static void initOrReload(MinecraftServer server) {
        initOrReload(server, DEFAULT_URL);
    }

    public static void initOrReload(MinecraftServer server, String remoteUrl) {
        try {
            Map<String, Campaign> fresh = fetch(remoteUrl);
            if (!fresh.isEmpty()) {
                // Fill names from server cache if some are missing (no network)
                enrichNamesFromProfileCache(server, fresh);
                DATA = fresh;
                cacheToDisk(server, fresh);
                return;
            }
        } catch (Exception ignored) {}

        Map<String, Campaign> cached = readCache(server);
        if (!cached.isEmpty()) {
            enrichNamesFromProfileCache(server, cached);
            DATA = cached;
        }
    }


    /** Pick a random patron for a campaign (or null if none). */
    public static @Nullable Patron pickPatron(String campaignId, RandomSource rand) {
        Campaign c = DATA.get(campaignId);
        if (c == null || c.patrons == null || c.patrons.isEmpty()) return null;
        return c.patrons.get(rand.nextInt(c.patrons.size()));
    }

    /** Compute statue notch tier for an entry (months → tier, or explicit override). */
    public static PlayerStatueBlockEntity.NotchTier tierFor(Patron p) {
        if (p == null) return PlayerStatueBlockEntity.NotchTier.NONE;
        if (p.tier != null) {
            try { return PlayerStatueBlockEntity.NotchTier.valueOf(p.tier.toUpperCase(Locale.ROOT)); }
            catch (Exception ignored) {}
        }
        int m = Math.max(0, p.months);
        if (m >= 4) return PlayerStatueBlockEntity.NotchTier.DIAMOND; // 4+
        if (m == 3) return PlayerStatueBlockEntity.NotchTier.GOLD;
        if (m == 2) return PlayerStatueBlockEntity.NotchTier.IRON;
        if (m == 1) return PlayerStatueBlockEntity.NotchTier.COPPER;
        return PlayerStatueBlockEntity.NotchTier.NONE;
    }

    // ======= data model =====================================================
    public static final class Root {
        public String lastUpdated;
        public Map<String, Campaign> campaigns = new HashMap<>();
    }
    public static final class Campaign {
        public List<Patron> patrons = new ArrayList<>();
    }

    public static final class Patron {
        public java.util.UUID uuid;

        // accept either "username" (your JSON) or "name" (future-proof)
        @SerializedName(value = "name", alternate = {"username"})
        public @Nullable String name;

        public int months;
        public String tier;
    }




    // ======= impl ===========================================================
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static volatile Map<String, Campaign> DATA = new HashMap<>();

    private static Map<String, Campaign> fetch(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setConnectTimeout(7000);
        conn.setReadTimeout(7000);
        conn.setRequestProperty("User-Agent", "DNL-PatronFetcher");
        try (InputStream in = conn.getInputStream();
             Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Root root = GSON.fromJson(r, Root.class);
            return (root != null && root.campaigns != null) ? root.campaigns : Map.of();
        }
    }

    private static void cacheToDisk(MinecraftServer server, Map<String, Campaign> data) {
        try {
            File f = getCacheFile(server);
            f.getParentFile().mkdirs();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
                Root r = new Root(); r.campaigns = data;
                GSON.toJson(r, w);
            }
        } catch (Exception ignored) {}
    }

    private static Map<String, Campaign> readCache(MinecraftServer server) {
        try {
            File f = getCacheFile(server);
            if (!f.exists()) return Map.of();
            try (Reader r = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                Root root = GSON.fromJson(r, Root.class);
                return (root != null && root.campaigns != null) ? root.campaigns : Map.of();
            }
        } catch (Exception ignored) {}
        return Map.of();
    }

    private static File getCacheFile(MinecraftServer server) {
        // config/dnl/patrons.json
        return server.getFile("config/dnl/patrons.json").toFile();
    }

    private static void enrichNamesFromProfileCache(MinecraftServer server, Map<String, Campaign> data) {
        if (server == null || data == null) return;
        var cache = server.getProfileCache();
        if (cache == null) return;

        for (var entry : data.entrySet()) {
            Campaign c = entry.getValue();
            if (c == null || c.patrons == null) continue;
            for (Patron p : c.patrons) {
                if (p == null || p.uuid == null) continue;
                if (p.name != null && !p.name.isBlank()) continue;

                cache.get(p.uuid).ifPresent(gp -> {
                    if (gp.getName() != null && !gp.getName().isBlank()) {
                        p.name = gp.getName();
                    }
                });
            }
        }
    }

    // ======= campaign-scoped lookup helpers ===================================

    public static @Nullable Patron findByName(String campaignId, String name) {
        if (campaignId == null || name == null) return null;
        Campaign c = DATA.get(campaignId);
        if (c == null || c.patrons == null || c.patrons.isEmpty()) return null;

        String needle = name.trim();
        if (needle.isEmpty()) return null;

        Patron ciMatch = null;
        for (Patron p : c.patrons) {
            if (p == null || p.name == null || p.name.isBlank()) continue;

            if (p.name.equals(needle)) return p;                 // exact-case preferred
            if (ciMatch == null && p.name.equalsIgnoreCase(needle)) ciMatch = p;
        }
        return ciMatch;
    }

    public static @Nullable Patron findByUuid(String campaignId, UUID id) {
        if (campaignId == null || id == null) return null;
        Campaign c = DATA.get(campaignId);
        if (c == null || c.patrons == null || c.patrons.isEmpty()) return null;

        for (Patron p : c.patrons) {
            if (p != null && id.equals(p.uuid)) return p;
        }
        return null;
    }

}
