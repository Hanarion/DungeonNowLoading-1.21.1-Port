package dev.hexnowloading.dungeonnowloading.supporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
                DATA = fresh;
                cacheToDisk(server, fresh);
                return;
            }
        } catch (Exception ignored) {}
        // Fallback to cache on disk if HTTP failed
        Map<String, Campaign> cached = readCache(server);
        if (!cached.isEmpty()) DATA = cached;
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
        return server.getFile("config/dnl/patrons.json");
    }
}
