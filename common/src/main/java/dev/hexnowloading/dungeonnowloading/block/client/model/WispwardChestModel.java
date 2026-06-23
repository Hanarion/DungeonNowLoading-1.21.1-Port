package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class WispwardChestModel extends Model{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "wispward_chest"), "main");
    private final ModelPart wispward_chest;
    private final ModelPart fire;

    public WispwardChestModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.wispward_chest = root.getChild("wispward_chest");
        this.fire = this.wispward_chest.getChild("fire");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wispward_chest = partdefinition.addOrReplaceChild("wispward_chest", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition fire = wispward_chest.addOrReplaceChild("fire", CubeListBuilder.create().texOffs(0, 32).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        wispward_chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void renderBase(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.fire.visible = false;
        this.wispward_chest.render(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
        this.fire.visible = true;
    }

    public void renderFire(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float progress) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        float red = 0.08F + 0.92F * clamped;
        float green = 0.08F + 0.92F * clamped;
        float blue = 0.08F + 0.92F * clamped;
        poseStack.pushPose();
        this.wispward_chest.translateAndRotate(poseStack);
        this.fire.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(1.0F, red, green, blue));
        poseStack.popPose();
    }
}
