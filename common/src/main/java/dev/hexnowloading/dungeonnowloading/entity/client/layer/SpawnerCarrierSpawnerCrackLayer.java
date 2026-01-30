package dev.hexnowloading.dungeonnowloading.entity.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class SpawnerCarrierSpawnerCrackLayer<T extends SpawnerCarrierEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[] {
            new ResourceLocation("textures/block/destroy_stage_0.png"),
            new ResourceLocation("textures/block/destroy_stage_1.png"),
            new ResourceLocation("textures/block/destroy_stage_2.png"),
            new ResourceLocation("textures/block/destroy_stage_3.png"),
            new ResourceLocation("textures/block/destroy_stage_4.png"),
            new ResourceLocation("textures/block/destroy_stage_5.png"),
            new ResourceLocation("textures/block/destroy_stage_6.png"),
            new ResourceLocation("textures/block/destroy_stage_7.png"),
            new ResourceLocation("textures/block/destroy_stage_8.png"),
            new ResourceLocation("textures/block/destroy_stage_9.png"),
    };

    public SpawnerCarrierSpawnerCrackLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T carrier, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        int stage = carrier.getSpawnerCrackStage();
        if (stage < 0) return; // no cracks or broken

        if (!(this.getParentModel() instanceof SpawnerCarrierModel<?> model)) return;

        poseStack.pushPose();

        // Anchor to the spawner ModelPart pivot
        model.root().translateAndRotate(poseStack);
        model.getAllPart().translateAndRotate(poseStack);
        model.getBodyPart().translateAndRotate(poseStack);

        // Render only the spawner part using crumbling render type
        VertexConsumer vc = buffer.getBuffer(net.minecraft.client.renderer.RenderType.crumbling(DESTROY_STAGES[stage]));

// 128x128 model UVs vs 16x16 crack => tile 8x so pixels aren't huge
        VertexConsumer tiled = new TiledUvVertexConsumer(vc, 8.0f, 8.0f);

        model.getSpawnerPart().render(
                poseStack,
                tiled,
                packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1f, 0.2f, 0.2f, 1f
        );

        poseStack.popPose();
    }

    public class TiledUvVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float uMul, vMul;

        public TiledUvVertexConsumer(VertexConsumer delegate, float uMul, float vMul) {
            this.delegate = delegate;
            this.uMul = uMul;
            this.vMul = vMul;
        }

        @Override public VertexConsumer vertex(double x, double y, double z) { return delegate.vertex(x,y,z); }
        @Override public VertexConsumer color(int r, int g, int b, int a) { return delegate.color(r,g,b,a); }

        @Override
        public VertexConsumer uv(float u, float v) {
            float tu = u * uMul;
            float tv = v * vMul;

            // pixel offset on the 16x16 destroy texture
            // +u = right, -u = left
            // +v = down,  -v = up
            float uOffPx = 0.0f; // try 4 px
            float vOffPx = 5.0f;

            float uOff = uOffPx / 16.0f;
            float vOff = vOffPx / 16.0f;

            return delegate.uv(tu + uOff, tv + vOff);
        }

        @Override public VertexConsumer overlayCoords(int u, int v) { return delegate.overlayCoords(u,v); }
        @Override public VertexConsumer uv2(int u, int v) { return delegate.uv2(u,v); }
        @Override public VertexConsumer normal(float x, float y, float z) { return delegate.normal(x,y,z); }
        @Override public void endVertex() { delegate.endVertex(); }
        @Override public void defaultColor(int r, int g, int b, int a) { delegate.defaultColor(r,g,b,a); }
        @Override public void unsetDefaultColor() { delegate.unsetDefaultColor(); }
    }
}
