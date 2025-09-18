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
    public record StatueSkin(ResourceLocation texture, boolean slim) {}
    private static final ResourceLocation DEFAULT_STONE = new ResourceLocation("dungeonnowloading", "textures/block/player_statue_stone.png");
    private static final float DEFAULT_OVERLAY_ALPHA = 0.35f;
    private static final Map<String, StatueSkin> READY = new ConcurrentHashMap<>();
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

    public static StatueSkin get(GameProfile profile) {
        return get(profile, DEFAULT_OVERLAY_ALPHA, DEFAULT_STONE);
    }

    public static StatueSkin get(GameProfile profile, float overlayAlpha, ResourceLocation stoneTex) {
        if (profile == null) {
            return buildPlaceholder(overlayAlpha, stoneTex, "null-profile");
        }

        final String k1 = primaryKey(profile);
        final String k2 = (k1 != null && k1.startsWith("name:")) ? uuidKey(profile) : nameKey(profile);

        StatueSkin ready = (k1 != null ? READY.get(k1) : null);
        if (ready == null && k2 != null) ready = READY.get(k2);
        if (ready != null) return ready;

        if (k1 != null && INFLIGHT.add(k1)) {
            startAsyncBuild(profile, k1, overlayAlpha, stoneTex);
        }

        // placeholder while async fetch runs
        return buildPlaceholder(overlayAlpha, stoneTex, k1 != null ? k1 : "unknown");
    }

    private static void startAsyncBuild(GameProfile profile, String key, float overlayAlpha, ResourceLocation stoneTex) {
        EXEC.submit(() -> {
            try {
                SkinImg s = (profile != null) ? resolveSkinImageAndModel(profile) : null;
                if (s == null || s.img == null) {
                    UUID derived = (profile != null) ? UUIDUtil.getOrCreatePlayerUUID(profile) : new UUID(0L, 0L);
                    ResourceLocation def = DefaultPlayerSkin.getDefaultSkin(derived);
                    boolean slim = "slim".equals(DefaultPlayerSkin.getSkinModelName(derived));
                    NativeImage img = readResource(def);
                    if (img == null) return;
                    s = new SkinImg(img, slim);
                }

                grayscale(s.img);
                NativeImage stone = readResource(stoneTex);
                if (stone != null) { blendOverlay(s.img, stone, overlayAlpha); stone.close(); }

                NativeImage finalImg = s.img;
                String seed = key + "|" + String.format(Locale.ROOT, "%.2f", overlayAlpha) + "|" + stoneTex;
                String digest = Hashing.sha1().hashString(seed, StandardCharsets.UTF_8).toString();
                ResourceLocation loc = new ResourceLocation("dungeonnowloading", "statue/" + digest);

                boolean isSlim = s.slim;
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().getTextureManager().register(loc, new DynamicTexture(finalImg));
                    StatueSkin ss = new StatueSkin(loc, isSlim);
                    READY.put(key, ss); // ensure we cache under the primary key
                    String nk = nameKey(profile), uk = uuidKey(profile);
                    if (nk != null) READY.put(nk, ss);
                    if (uk != null) READY.put(uk, ss);
                });
            } catch (Exception e) {
                if (LOG) e.printStackTrace();
            } finally {
                INFLIGHT.remove(key);
            }
        });
    }

    private static final class SkinImg {
        final NativeImage img; final boolean slim;
        SkinImg(NativeImage img, boolean slim) { this.img = img; this.slim = slim; }
    }

    private static SkinImg resolveSkinImageAndModel(GameProfile profile) {
        // A) SkinManager (has MinecraftProfileTexture with metadata)
        try {
            SkinManager sm = Minecraft.getInstance().getSkinManager();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = sm.getInsecureSkinInformation(profile);
            MinecraftProfileTexture skin = map.get(MinecraftProfileTexture.Type.SKIN);
            if (skin != null) {
                String model = skin.getMetadata("model"); // returns "slim" for Alex-type skins
                boolean slim = "slim".equals(model);
                NativeImage img = downloadPng(skin.getUrl());
                if (img != null) return new SkinImg(img, slim);
            }
        } catch (Exception ignored) {}

        // Determine UUID (for sessionserver and fallback)
        String uuidNoDash = (profile.getId() != null)
                ? profile.getId().toString().replace("-", "")
                : (profile.getName() != null ? lookupUuidByName(profile.getName()) : null);
        if (uuidNoDash == null) return null;

        // B) Mojang sessionserver (metadata.model)
        try {
            JsonObject prof = httpJson("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidNoDash);
            if (prof != null && prof.has("properties")) {
                JsonArray props = prof.getAsJsonArray("properties");
                for (int i = 0; i < props.size(); i++) {
                    JsonObject p = props.get(i).getAsJsonObject();
                    if (!"textures".equals(p.get("name").getAsString())) continue;
                    String val = p.get("value").getAsString();
                    String decoded = new String(Base64.getDecoder().decode(val), StandardCharsets.UTF_8);
                    JsonObject textures = JsonParser.parseString(decoded).getAsJsonObject().getAsJsonObject("textures");
                    if (textures != null && textures.has("SKIN")) {
                        JsonObject skin = textures.getAsJsonObject("SKIN");
                        String url = skin.get("url").getAsString();
                        boolean slim = false;
                        if (skin.has("metadata")) {
                            JsonObject md = skin.getAsJsonObject("metadata");
                            if (md.has("model")) slim = "slim".equals(md.get("model").getAsString());
                        }
                        NativeImage img = downloadPng(url);
                        if (img != null) return new SkinImg(img, slim);
                    }
                }
            }
        } catch (Exception ignored) {}

        // C) Crafatar fallback: no metadata → derive from UUID parity
        boolean slim = "slim".equals(DefaultPlayerSkin.getSkinModelName(UUID.fromString(
                uuidNoDash.replaceFirst(
                        "(........)(....)(....)(....)(............)",
                        "$1-$2-$3-$4-$5"
                ))));
        NativeImage img = downloadPng("https://crafatar.com/skins/" + uuidNoDash);
        return (img != null) ? new SkinImg(img, slim) : null;
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

    private static StatueSkin buildPlaceholder(float overlayAlpha, ResourceLocation stoneTex, String keySeed) {
        try {
            // fixed UUID -> stable Steve/Alex selection
            UUID derived = new UUID(0L, 0L);
            ResourceLocation def = DefaultPlayerSkin.getDefaultSkin(derived);
            boolean slim = "slim".equals(DefaultPlayerSkin.getSkinModelName(derived));

            NativeImage img = readResource(def);
            if (img != null) {
                grayscale(img);
                NativeImage stone = readResource(stoneTex);
                if (stone != null) { blendOverlay(img, stone, overlayAlpha); stone.close(); }
                DynamicTexture dyn = new DynamicTexture(img);

                String seed = keySeed + "|ph|" + String.format(Locale.ROOT, "%.2f", overlayAlpha) + "|" + stoneTex;
                String digest = Hashing.sha1().hashString(seed, StandardCharsets.UTF_8).toString();
                ResourceLocation loc = new ResourceLocation("dungeonnowloading", "statue/" + digest);
                Minecraft.getInstance().getTextureManager().register(loc, dyn);
                return new StatueSkin(loc, slim);
            }
        } catch (Exception ignored) {}
        return new StatueSkin(stoneTex, false);
    }

    private static String nameKey(GameProfile p) {
        if (p == null) return null;
        return (p.getName() != null && !p.getName().isEmpty())
                ? "name:" + p.getName().toLowerCase(Locale.ROOT) : null;
    }
    private static String uuidKey(GameProfile p) {
        if (p == null) return null;
        return (p.getId() != null) ? "uuid:" + p.getId() : null;
    }
    private static String primaryKey(GameProfile p) {
        if (p == null) return "null";
        String nk = nameKey(p), uk = uuidKey(p);
        return nk != null ? nk : (uk != null ? uk : "unknown");
    }
}
