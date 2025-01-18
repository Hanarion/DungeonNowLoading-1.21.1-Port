package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FairkeeperSerpentCallerModel<T extends FairkeeperSerpentCallerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "fairkeeper_serpent_caller"), "main");
    private final ModelPart fairkeeper_serpent_caller;
    private final ModelPart root;

    public FairkeeperSerpentCallerModel(ModelPart root) {
        this.root = root;
        this.fairkeeper_serpent_caller = root.getChild("fairkeeper_serpent_caller");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition fairkeeper_serpent_caller = partdefinition.addOrReplaceChild("fairkeeper_serpent_caller", CubeListBuilder.create().texOffs(-8, -4).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(-4, -2).addBox(5.0F, -15.0F, -2.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-4, -2).addBox(-7.0F, -15.0F, -2.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-4, -2).addBox(-5.0F, -4.0F, -2.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(-4, -2).addBox(-3.0F, -15.0F, -2.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        fairkeeper_serpent_caller.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FairkeeperSerpentCallerEntity entity, float var2, float var3, float var4, float var5, float var6) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
    }

    public void headAnim(float netHeadYaw) {
        this.fairkeeper_serpent_caller.yRot = netHeadYaw * ((float)Math.PI / 180F);
    }
}
