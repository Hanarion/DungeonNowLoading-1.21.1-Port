package dev.hexnowloading.dungeonnowloading.block.client;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class StatueSkinCache {

    private static final boolean LOG = true;

    private static final ResourceLocation DEFAULT_STONE = new ResourceLocation("minecraft", "textures/block/stone.png");
    private static final float DEFAULT_OVERLAY_ALPHA = 0.35f;
    private static final Map<String, ResourceLocation> READY = new ConcurrentHashMap<>();
    private static final Set<String> INFLIGHT = ConcurrentHashMap.newKeySet();
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "DNL-StatueSkin"); t.setDaemon(true); return t;
    });

    private StatueSkinCache() {}

    public static void clear() {
        READY.clear();
        INFLIGHT.clear();
    }

    private static String cacheKey(GameProfile p) {
        if (p.getName() != null && !p.getName().isEmpty()) {
            return "name:" + p.getName().toLowerCase(Locale.ROOT);
        }
        return "uuid:" + (p.getId() != null ? p.getId().toString() : "unknown");
    }

    public static ResourceLocation get(GameProfile profile) {
        return get(profile, DEFAULT_OVERLAY_ALPHA, DEFAULT_STONE);
    }

    public static ResourceLocation get(GameProfile profile, float overlayAlpha, ResourceLocation stoneTex) {
        final String k1 = primaryKey(profile);
        final String k2 = (k1 != null && k1.startsWith("name:")) ? uuidKey(profile) : nameKey(profile);

        // if ready under either key, use it
        ResourceLocation ready = (k1 != null ? READY.get(k1) : null);
        if (LOG) System.out.println("[DNL][Statue] k1=" + k1 + " k2=" + k2 + " ready? " + (ready != null));

        if (ready == null && k2 != null) ready = READY.get(k2);
        if (ready != null) return ready;

        // kick off async once per primary key
        if (k1 != null && INFLIGHT.add(k1)) {
            startAsyncBuild(profile, k1, overlayAlpha, stoneTex);
        }

        // Placeholder: default Steve/Alex (derived UUID) grayscaled + stone overlay
        try {
            var derived = UUIDUtil.getOrCreatePlayerUUID(profile);
            var def = DefaultPlayerSkin.getDefaultSkin(derived);
            var img = readResource(def);
            if (img != null) {
                grayscale(img);
                var stone = readResource(stoneTex);
                if (stone != null) { blendOverlay(img, stone, overlayAlpha); stone.close(); }
                DynamicTexture dyn = new DynamicTexture(img);

                String seed = (k1 != null ? k1 : "unknown") + "|ph|" +
                        String.format(Locale.ROOT,"%.2f", overlayAlpha) + "|" + stoneTex;
                String digest = com.google.common.hash.Hashing.sha1()
                        .hashString(seed, StandardCharsets.UTF_8).toString();
                ResourceLocation loc = new ResourceLocation("dungeonnowloading", "statue/" + digest);

                Minecraft.getInstance().getTextureManager().register(loc, dyn);
                return loc;
            } else {
                // ✅ Don’t fall back to stone: show default skin immediately
                return def;
            }
        } catch (Exception ignored) {}

// Absolute last resort (should rarely hit)
        return stoneTex;
    }

    private static void startAsyncBuild(GameProfile profile, String key, float overlayAlpha, ResourceLocation stoneTex) {
        EXEC.submit(() -> {
            try {
                // Try to get a fully decoded skin image (handles SkinManager, Mojang, Crafatar)
                NativeImage img = resolveSkinImage(profile);

                // Fallback: default Steve/Alex derived from name if needed
                if (img == null) {
                    var derived = UUIDUtil.getOrCreatePlayerUUID(profile);
                    var def = DefaultPlayerSkin.getDefaultSkin(derived);
                    img = readResource(def);
                    if (LOG) System.out.println("[DNL][Statue] async fallback default skin: " + def);
                }
                if (img == null) return; // nothing to register

                // Process then overlay stone
                grayscale(img);
                NativeImage stone = readResource(stoneTex);
                if (stone != null) { blendOverlay(img, stone, overlayAlpha); stone.close(); }

                // Register on main thread and mark READY
                NativeImage finalImg = img;
                String seed   = key + "|" + String.format(Locale.ROOT, "%.2f", overlayAlpha) + "|" + stoneTex;
                String digest = Hashing.sha1().hashString(seed, StandardCharsets.UTF_8).toString();
                ResourceLocation loc = new ResourceLocation("dungeonnowloading", "statue/" + digest);

                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().getTextureManager().register(loc, new DynamicTexture(finalImg));

                    // index under both keys so later key flips still hit
                    String nk = nameKey(profile), uk = uuidKey(profile);
                    if (nk != null) READY.put(nk, loc);
                    if (uk != null) READY.put(uk, loc);

                    if (LOG) System.out.println("[DNL][Statue] READY for " + (nk != null ? nk : uk) + " -> " + loc);
                    INFLIGHT.remove(key); // keep your finally{} too, this is just extra safety
                });
            } catch (Exception e) {
                if (LOG) e.printStackTrace();
            } finally {
                // ✅ always clear, even on early returns
                INFLIGHT.remove(key);
            }
        });
    }



    // === Resolution paths ====================================================

    private static NativeImage resolveSkinImage(GameProfile profile) {
        // A) skin manager (works if textures property is present)
        String url = trySkinManager(profile);
        if (url != null) {
            if (LOG) System.out.println("[DNL][Statue] SkinManager URL: " + url);
            NativeImage img = downloadPng(url);
            if (img != null) return img;
        }

        // Determine UUID (no dashes) — either from profile or by name → uuid
        String uuidNoDash = null;
        if (profile.getId() != null) {
            uuidNoDash = profile.getId().toString().replace("-", "");
        } else if (profile.getName() != null && !profile.getName().isEmpty()) {
            uuidNoDash = lookupUuidByName(profile.getName());
            if (LOG) System.out.println("[DNL][Statue] Mojang name→uuid: " + profile.getName() + " -> " + uuidNoDash);
        }

        if (uuidNoDash == null) return null;

        // B) Mojang sessionserver textures (preferred)
        String mojangUrl = lookupSkinUrlViaSessionServer(uuidNoDash);
        if (mojangUrl != null) {
            if (LOG) System.out.println("[DNL][Statue] SessionServer SKIN URL: " + mojangUrl);
            NativeImage img = downloadPng(mojangUrl);
            if (img != null) return img;
        }

        // C) Crafatar direct PNG (unauth, extremely reliable)
        String crafatarUrl = "https://crafatar.com/skins/" + uuidNoDash;
        if (LOG) System.out.println("[DNL][Statue] Crafatar fallback: " + crafatarUrl);
        return downloadPng(crafatarUrl);
    }

    private static String trySkinManager(GameProfile profile) {
        try {
            SkinManager sm = Minecraft.getInstance().getSkinManager();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = sm.getInsecureSkinInformation(profile);
            MinecraftProfileTexture skin = map.get(MinecraftProfileTexture.Type.SKIN);
            return skin != null ? skin.getUrl() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String lookupUuidByName(String name) {
        try {
            JsonObject o = httpJson("https://api.mojang.com/users/profiles/minecraft/" + name);
            if (o == null || !o.has("id")) return null;
            return o.get("id").getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String lookupSkinUrlViaSessionServer(String uuidNoDash) {
        try {
            JsonObject prof = httpJson("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidNoDash);
            if (prof == null || !prof.has("properties")) return null;

            JsonArray props = prof.getAsJsonArray("properties");
            for (int i = 0; i < props.size(); i++) {
                JsonObject p = props.get(i).getAsJsonObject();
                if (!"textures".equals(p.get("name").getAsString())) continue;
                String val = p.get("value").getAsString();
                String decoded = new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
                JsonObject textures = JsonParser.parseString(decoded).getAsJsonObject().getAsJsonObject("textures");
                if (textures != null && textures.has("SKIN")) {
                    JsonObject skin = textures.getAsJsonObject("SKIN");
                    if (skin.has("url")) return skin.get("url").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static JsonObject httpJson(String urlStr) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            c.setRequestProperty("User-Agent", "DNL-Statue/1.0");
            try (InputStream in = c.getInputStream();
                 InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(r).getAsJsonObject();
            }
        } catch (Exception e) {
            if (LOG) System.out.println("[DNL][Statue] httpJson fail " + urlStr + " : " + e.getMessage());
            return null;
        }
    }

    // === IO & image ops ======================================================

    private static NativeImage downloadPng(String urlStr) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
            c.setConnectTimeout(6000);
            c.setReadTimeout(6000);
            c.setRequestProperty("User-Agent", "DNL-Statue/1.0");
            try (InputStream in = c.getInputStream()) {
                return NativeImage.read(in);
            }
        } catch (Exception e) {
            if (LOG) System.out.println("[DNL][Statue] download fail " + urlStr + " : " + e.getMessage());
            return null;
        }
    }

    private static NativeImage readResource(ResourceLocation rl) {
        try {
            Optional<net.minecraft.server.packs.resources.Resource> res = Minecraft.getInstance().getResourceManager().getResource(rl);
            if (res.isEmpty()) return null;
            try (InputStream in = res.get().open()) {
                return NativeImage.read(in);
            }
        } catch (Exception e) {
            if (LOG) System.out.println("[DNL][Statue] resource fail " + rl + " : " + e.getMessage());
            return null;
        }
    }

    private static void grayscale(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int abgr = img.getPixelRGBA(x, y);
            int a = (abgr >>> 24) & 0xFF, b = (abgr >>> 16) & 0xFF, g = (abgr >>> 8) & 0xFF, r = abgr & 0xFF;
            int gray = (int)(0.2126*r + 0.7152*g + 0.0722*b) & 0xFF;
            img.setPixelRGBA(x, y, (a << 24) | (gray << 16) | (gray << 8) | gray);
        }
    }

    private static void blendOverlay(NativeImage base, NativeImage overlay, float alpha) {
        int w = base.getWidth(), h = base.getHeight();
        int ow = overlay.getWidth(), oh = overlay.getHeight();
        alpha = Math.max(0f, Math.min(1f, alpha));
        for (int y = 0; y < h; y++) {
            int oy = y % oh;
            for (int x = 0; x < w; x++) {
                int ox = x % ow;
                int bc = base.getPixelRGBA(x, y);
                int a = (bc >>> 24) & 0xFF; if (a == 0) continue;
                int bb = (bc >>> 16) & 0xFF, bg = (bc >>> 8) & 0xFF, br = bc & 0xFF;

                int oc = overlay.getPixelRGBA(ox, oy);
                int ob = (oc >>> 16) & 0xFF, og = (oc >>> 8) & 0xFF, or = oc & 0xFF, oa = (oc >>> 24) & 0xFF;

                float oaf = (oa / 255f) * alpha;
                int r = (int)(br * (1f - oaf) + or * oaf);
                int g = (int)(bg * (1f - oaf) + og * oaf);
                int b = (int)(bb * (1f - oaf) + ob * oaf);
                base.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
    }

    private static String nameKey(GameProfile p) {
        return (p.getName() != null && !p.getName().isEmpty())
                ? "name:" + p.getName().toLowerCase(Locale.ROOT) : null;
    }
    private static String uuidKey(GameProfile p) {
        return (p.getId() != null) ? "uuid:" + p.getId() : null;
    }
    private static String primaryKey(GameProfile p) {
        String nk = nameKey(p), uk = uuidKey(p);
        return nk != null ? nk : (uk != null ? uk : "unknown");
    }
}
