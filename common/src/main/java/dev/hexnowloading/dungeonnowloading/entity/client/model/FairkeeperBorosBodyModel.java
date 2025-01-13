package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperBorosBodyModel<T extends FairkeeperBorosPartEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "fairkeeper_boros_body_shield"), "main");
    private final ModelPart boros;
    private final ModelPart shield;
    private final ModelPart dispenser;
    private final ModelPart body;
    private final ModelPart root;

    public FairkeeperBorosBodyModel(ModelPart root) {
        this.root = root;
        this.boros = root.getChild("boros");
        this.shield = this.boros.getChild("shield");
        this.dispenser = this.boros.getChild("dispenser");
        this.body = this.boros.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition boros = partdefinition.addOrReplaceChild("boros", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition shield = boros.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -36.0F, -24.0F, 48.0F, 24.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition dispenser = boros.addOrReplaceChild("dispenser", CubeListBuilder.create().texOffs(-62, -10).addBox(-27.0F, -18.0F, -6.0F, 54.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = boros.addOrReplaceChild("body", CubeListBuilder.create().texOffs(-84, -44).addBox(-21.0F, -33.0F, -23.0F, 42.0F, 33.0F, 46.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(FairkeeperBorosPartEntity entity, float var2, float var3, float var4, float var5, float var6) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.shield.visible = entity.hasArmor();
        this.dispenser.visible = !entity.isArmoredSegment();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        boros.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
