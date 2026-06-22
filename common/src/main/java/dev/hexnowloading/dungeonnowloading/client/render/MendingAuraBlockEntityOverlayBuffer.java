package dev.hexnowloading.dungeonnowloading.client.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MendingAuraBlockEntityOverlayBuffer implements MultiBufferSource {
    public static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
    private static final ResourceLocation MENDING_AURA_SPRITE = ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "block/mending_aura_0");
    private static final float OVERLAY_OFFSET = 0.002F;
    private static final Map<ResourceLocation, TextureMask> TEXTURE_MASK_CACHE = new ConcurrentHashMap<>();
    private static Field compositeStateField;
    private static Field textureStateField;
    private static Field textureField;

    private final MultiBufferSource delegate;
    private final float alpha;

    public MendingAuraBlockEntityOverlayBuffer(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return new AuraVertexConsumer(this.delegate.getBuffer(RENDER_TYPE), this.alpha, mendingAuraSprite(), textureMask(renderType));
    }

    private static class AuraVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;
        private final TextureAtlasSprite auraSprite;
        private final TextureMask textureMask;
        private final VertexData[] quad = new VertexData[4];
        private VertexData current = new VertexData();
        private int vertexCount;

        private AuraVertexConsumer(VertexConsumer delegate, float alpha, TextureAtlasSprite auraSprite, TextureMask textureMask) {
            this.delegate = delegate;
            this.alpha = alpha;
            this.auraSprite = auraSprite;
            this.textureMask = textureMask;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.current.x = (float) x;
            this.current.y = (float) y;
            this.current.z = (float) z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.current.alpha = Math.round(alpha * this.alpha);
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.current.u = u;
            this.current.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.current.overlayU = u;
            this.current.overlayV = v;
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.current.lightU = u;
            this.current.lightV = v;
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.current.normalX = x;
            this.current.normalY = y;
            this.current.normalZ = z;
            return this;
        }

        @Override
        public void endVertex() {
            this.quad[this.vertexCount++] = this.current;
            this.current = new VertexData();
            if (this.vertexCount == 4) {
                emitTiledQuad();
                this.vertexCount = 0;
            }
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            this.delegate.defaultColor(255, 255, 255, Math.round(alpha * this.alpha));
        }

        @Override
        public void unsetDefaultColor() {
            this.delegate.unsetDefaultColor();
        }

        private void emitTiledQuad() {
            Vec3f origin = Vec3f.from(this.quad[0]);
            Vec3f uAxis = Vec3f.from(this.quad[1]).subtract(origin);
            Vec3f vAxis = Vec3f.from(this.quad[3]).subtract(origin);
            float maxU = uAxis.length();
            float maxV = vAxis.length();

            if (maxU <= 0.0001F || maxV <= 0.0001F) {
                for (VertexData vertex : this.quad) {
                    emitVertex(vertex, wrapUnit(vertex.u), wrapUnit(vertex.v));
                }
                return;
            }

            if (this.textureMask != null && emitMaskedQuad(origin, uAxis, vAxis, maxU, maxV)) {
                return;
            }

            for (float u0 = 0.0F; u0 < maxU; u0 = nextTileBoundary(u0, maxU)) {
                float u1 = nextTileBoundary(u0, maxU);
                for (float v0 = 0.0F; v0 < maxV; v0 = nextTileBoundary(v0, maxV)) {
                    float v1 = nextTileBoundary(v0, maxV);
                    emitTile(origin, uAxis, vAxis, maxU, maxV, u0, u1, v0, v1);
                }
            }
        }

        private boolean emitMaskedQuad(Vec3f origin, Vec3f uAxis, Vec3f vAxis, float maxU, float maxV) {
            float sourceMinU = Math.min(Math.min(this.quad[0].u, this.quad[1].u), Math.min(this.quad[2].u, this.quad[3].u));
            float sourceMaxU = Math.max(Math.max(this.quad[0].u, this.quad[1].u), Math.max(this.quad[2].u, this.quad[3].u));
            float sourceMinV = Math.min(Math.min(this.quad[0].v, this.quad[1].v), Math.min(this.quad[2].v, this.quad[3].v));
            float sourceMaxV = Math.max(Math.max(this.quad[0].v, this.quad[1].v), Math.max(this.quad[2].v, this.quad[3].v));
            if (sourceMaxU <= sourceMinU || sourceMaxV <= sourceMinV) {
                return false;
            }

            int xStart = Math.max(0, (int) Math.floor(sourceMinU * this.textureMask.width()));
            int xEnd = Math.min(this.textureMask.width(), (int) Math.ceil(sourceMaxU * this.textureMask.width()));
            int yStart = Math.max(0, (int) Math.floor(sourceMinV * this.textureMask.height()));
            int yEnd = Math.min(this.textureMask.height(), (int) Math.ceil(sourceMaxV * this.textureMask.height()));
            if (xEnd <= xStart || yEnd <= yStart) {
                return false;
            }

            float uForward = Math.abs(this.quad[1].u - this.quad[0].u) >= Math.abs(this.quad[3].u - this.quad[0].u)
                    ? Math.signum(this.quad[1].u - this.quad[0].u)
                    : Math.signum(this.quad[3].u - this.quad[0].u);
            float vForward = Math.abs(this.quad[3].v - this.quad[0].v) >= Math.abs(this.quad[1].v - this.quad[0].v)
                    ? Math.signum(this.quad[3].v - this.quad[0].v)
                    : Math.signum(this.quad[1].v - this.quad[0].v);
            if (uForward == 0.0F || vForward == 0.0F) {
                return false;
            }

            for (int y = yStart; y < yEnd; y++) {
                for (int x = xStart; x < xEnd; x++) {
                    if (!this.textureMask.opaque(x, y)) {
                        continue;
                    }

                    float cellMinU = x / (float) this.textureMask.width();
                    float cellMaxU = (x + 1) / (float) this.textureMask.width();
                    float cellMinV = y / (float) this.textureMask.height();
                    float cellMaxV = (y + 1) / (float) this.textureMask.height();
                    float s0 = sourceToGeometry(cellMinU, sourceMinU, sourceMaxU, uForward);
                    float s1 = sourceToGeometry(cellMaxU, sourceMinU, sourceMaxU, uForward);
                    float t0 = sourceToGeometry(cellMinV, sourceMinV, sourceMaxV, vForward);
                    float t1 = sourceToGeometry(cellMaxV, sourceMinV, sourceMaxV, vForward);
                    emitMaskedCell(origin, uAxis, vAxis, maxU, maxV, Math.min(s0, s1), Math.max(s0, s1), Math.min(t0, t1), Math.max(t0, t1));
                }
            }
            return true;
        }

        private void emitMaskedCell(Vec3f origin, Vec3f uAxis, Vec3f vAxis, float maxU, float maxV, float s0, float s1, float t0, float t1) {
            float geomMinU = maxU * s0;
            float geomMaxU = maxU * s1;
            float geomMinV = maxV * t0;
            float geomMaxV = maxV * t1;
            float tileBaseU = (float) Math.floor(geomMinU);
            float tileBaseV = (float) Math.floor(geomMinV);

            VertexData v0 = project(origin, uAxis, vAxis, maxU, maxV, geomMinU, geomMinV);
            VertexData v1 = project(origin, uAxis, vAxis, maxU, maxV, geomMaxU, geomMinV);
            VertexData v2 = project(origin, uAxis, vAxis, maxU, maxV, geomMaxU, geomMaxV);
            VertexData v3 = project(origin, uAxis, vAxis, maxU, maxV, geomMinU, geomMaxV);
            v0.copyRenderDataFrom(this.quad[0]);
            v1.copyRenderDataFrom(this.quad[1]);
            v2.copyRenderDataFrom(this.quad[2]);
            v3.copyRenderDataFrom(this.quad[3]);
            emitVertex(v0, geomMinU - tileBaseU, geomMinV - tileBaseV);
            emitVertex(v1, geomMaxU - tileBaseU, geomMinV - tileBaseV);
            emitVertex(v2, geomMaxU - tileBaseU, geomMaxV - tileBaseV);
            emitVertex(v3, geomMinU - tileBaseU, geomMaxV - tileBaseV);
        }

        private static float sourceToGeometry(float source, float min, float max, float direction) {
            float value = direction >= 0.0F ? (source - min) / (max - min) : (max - source) / (max - min);
            return Math.max(0.0F, Math.min(1.0F, value));
        }

        private void emitTile(Vec3f origin, Vec3f uAxis, Vec3f vAxis, float maxU, float maxV, float tileMinU, float tileMaxU, float tileMinV, float tileMaxV) {
            float tileBaseU = (float) Math.floor(tileMinU);
            float tileBaseV = (float) Math.floor(tileMinV);
            for (int i = 0; i < this.quad.length; i++) {
                float targetU = (i == 1 || i == 2) ? tileMaxU : tileMinU;
                float targetV = (i == 2 || i == 3) ? tileMaxV : tileMinV;
                VertexData tiled = project(origin, uAxis, vAxis, maxU, maxV, targetU, targetV);
                tiled.copyRenderDataFrom(this.quad[i]);
                emitVertex(tiled, targetU - tileBaseU, targetV - tileBaseV);
            }
        }

        private static VertexData project(Vec3f origin, Vec3f uAxis, Vec3f vAxis, float maxU, float maxV, float targetU, float targetV) {
            VertexData result = new VertexData();
            float uScale = targetU / maxU;
            float vScale = targetV / maxV;
            result.x = origin.x + uAxis.x * uScale + vAxis.x * vScale;
            result.y = origin.y + uAxis.y * uScale + vAxis.y * vScale;
            result.z = origin.z + uAxis.z * uScale + vAxis.z * vScale;
            return result;
        }

        private void emitVertex(VertexData vertex, float u, float v) {
            this.delegate.vertex(
                            vertex.x + vertex.normalX * OVERLAY_OFFSET,
                            vertex.y + vertex.normalY * OVERLAY_OFFSET,
                            vertex.z + vertex.normalZ * OVERLAY_OFFSET
                    )
                    .color(255, 255, 255, vertex.alpha)
                    .uv(this.auraSprite.getU(u * 16.0F), this.auraSprite.getV(v * 16.0F))
                    .overlayCoords(vertex.overlayU, vertex.overlayV)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(vertex.normalX, vertex.normalY, vertex.normalZ)
                    .endVertex();
        }

        private static float nextTileBoundary(float value, float max) {
            float next = (float) Math.floor(value) + 1.0F;
            if (next <= value + 0.0001F) {
                next += 1.0F;
            }
            return Math.min(next, max);
        }

        private static float wrapUnit(float value) {
            float wrapped = value - (float) Math.floor(value);
            return wrapped == 0.0F && value > 0.0F ? 1.0F : wrapped;
        }
    }

    private record Vec3f(float x, float y, float z) {
        private static Vec3f from(VertexData vertex) {
            return new Vec3f(vertex.x, vertex.y, vertex.z);
        }

        private Vec3f subtract(Vec3f other) {
            return new Vec3f(this.x - other.x, this.y - other.y, this.z - other.z);
        }

        private float length() {
            return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }
    }

    private static class VertexData {
        private float x;
        private float y;
        private float z;
        private float u;
        private float v;
        private int alpha = 255;
        private int overlayU;
        private int overlayV;
        private int lightU;
        private int lightV;
        private float normalX;
        private float normalY = 1.0F;
        private float normalZ;

        private void copyRenderDataFrom(VertexData other) {
            this.alpha = other.alpha;
            this.overlayU = other.overlayU;
            this.overlayV = other.overlayV;
            this.lightU = other.lightU;
            this.lightV = other.lightV;
            this.normalX = other.normalX;
            this.normalY = other.normalY;
            this.normalZ = other.normalZ;
        }
    }

    private static TextureAtlasSprite mendingAuraSprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(MENDING_AURA_SPRITE);
    }

    private static TextureMask textureMask(RenderType renderType) {
        Optional<ResourceLocation> location = textureLocation(renderType);
        return location.map(resourceLocation -> TEXTURE_MASK_CACHE.computeIfAbsent(resourceLocation, MendingAuraBlockEntityOverlayBuffer::loadTextureMask)).orElse(null);
    }

    private static TextureMask loadTextureMask(ResourceLocation location) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            return null;
        }

        try (NativeImage image = NativeImage.read(resource.get().open())) {
            int width = Math.max(1, image.getWidth());
            int height = Math.max(1, image.getHeight());
            boolean[] opaque = new boolean[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    opaque[y * width + x] = ((image.getPixelRGBA(x, y) >>> 24) & 0xFF) != 0;
                }
            }
            return new TextureMask(width, height, opaque);
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Optional<ResourceLocation> textureLocation(RenderType renderType) {
        try {
            Field stateField = compositeStateField;
            if (stateField == null) {
                stateField = findField(renderType.getClass(), "state");
                compositeStateField = stateField;
            }
            Object state = stateField.get(renderType);

            Field textureState = textureStateField;
            if (textureState == null) {
                textureState = findField(state.getClass(), "textureState");
                textureStateField = textureState;
            }
            Object textureStateValue = textureState.get(state);

            Field texture = textureField;
            if (texture == null) {
                texture = findField(textureStateValue.getClass(), "texture");
                textureField = texture;
            }
            return (Optional<ResourceLocation>) texture.get(textureStateValue);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private record TextureMask(int width, int height, boolean[] opaque) {
        private boolean opaque(int x, int y) {
            return this.opaque[y * this.width + x];
        }
    }
}
