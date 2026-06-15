package dev.hexnowloading.dungeonnowloading.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class MendingAuraBlockEntityOverlayBuffer implements MultiBufferSource {
    public static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

    private final MultiBufferSource delegate;
    private final float alpha;

    public MendingAuraBlockEntityOverlayBuffer(MultiBufferSource delegate, float alpha) {
        this.delegate = delegate;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return new AuraVertexConsumer(this.delegate.getBuffer(RENDER_TYPE), this.alpha, mendingAuraSprite());
    }

    private static class AuraVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alpha;
        private final TextureAtlasSprite auraSprite;
        private final VertexData[] quad = new VertexData[4];
        private VertexData current = new VertexData();
        private int vertexCount;

        private AuraVertexConsumer(VertexConsumer delegate, float alpha, TextureAtlasSprite auraSprite) {
            this.delegate = delegate;
            this.alpha = alpha;
            this.auraSprite = auraSprite;
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

            for (float u0 = 0.0F; u0 < maxU; u0 = nextTileBoundary(u0, maxU)) {
                float u1 = nextTileBoundary(u0, maxU);
                for (float v0 = 0.0F; v0 < maxV; v0 = nextTileBoundary(v0, maxV)) {
                    float v1 = nextTileBoundary(v0, maxV);
                    emitTile(origin, uAxis, vAxis, maxU, maxV, u0, u1, v0, v1);
                }
            }
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
            this.delegate.vertex(vertex.x, vertex.y, vertex.z)
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
                .getBlockRenderer()
                .getBlockModel(DNLBlocks.MENDING_AURA.get().defaultBlockState())
                .getParticleIcon();
    }
}
