package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class CenteredRepeatingUvVertexConsumer implements VertexConsumer {
    private static final float UV_CENTER = 0.5F;

    private final VertexConsumer delegate;
    private final float uScale;
    private final float vScale;

    public CenteredRepeatingUvVertexConsumer(VertexConsumer delegate, int textureWidth, int textureHeight) {
        this.delegate = delegate;
        this.uScale = textureWidth / 16.0F;
        this.vScale = textureHeight / 16.0F;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return this.delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        float centeredU = (u - UV_CENTER) * this.uScale + UV_CENTER;
        float centeredV = (v - UV_CENTER) * this.vScale + UV_CENTER;
        return this.delegate.uv(centeredU, centeredV);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return this.delegate.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return this.delegate.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return this.delegate.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        this.delegate.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        this.delegate.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }
}
