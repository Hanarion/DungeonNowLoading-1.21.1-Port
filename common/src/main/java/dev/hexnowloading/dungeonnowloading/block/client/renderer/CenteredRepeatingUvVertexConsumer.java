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
    public VertexConsumer addVertex(float x, float y, float z) {
        return this.delegate.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this.delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        float centeredU = (u - UV_CENTER) * this.uScale + UV_CENTER;
        float centeredV = (v - UV_CENTER) * this.vScale + UV_CENTER;
        return this.delegate.setUv(centeredU, centeredV);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this.delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this.delegate.setNormal(x, y, z);
    }
}
