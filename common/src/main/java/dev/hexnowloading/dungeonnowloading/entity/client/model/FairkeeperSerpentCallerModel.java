package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.FairkeeperSerpentCallerAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperSerpentCallerModel<T extends FairkeeperSerpentCallerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "fairkeeper_serpent_caller"), "main");
    private final ModelPart fairkeeper_serpent_caller;
    private final ModelPart orb;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart root;

    public FairkeeperSerpentCallerModel(ModelPart root) {
        this.root = root;
        this.fairkeeper_serpent_caller = root.getChild("fairkeeper_serpent_caller");
        this.orb = this.fairkeeper_serpent_caller.getChild("orb");
        this.left = this.fairkeeper_serpent_caller.getChild("left");
        this.right = this.fairkeeper_serpent_caller.getChild("right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition fairkeeper_serpent_caller = partdefinition.addOrReplaceChild("fairkeeper_serpent_caller", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition orb = fairkeeper_serpent_caller.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(44, 48).addBox(-3.0F, -3.0F, -7.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left = fairkeeper_serpent_caller.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 24).addBox(-16.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 6).addBox(-16.0F, 2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(34, 24).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 40).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 48).addBox(2.0F, -16.0F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 55).addBox(4.0F, -13.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(6.0F, -14.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, 10.0F, 0.0F));

        PartDefinition right = fairkeeper_serpent_caller.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-10.0F, -10.0F, 0.0F));

        PartDefinition cube_r1 = right.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.5F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.5F, -3.0F, 0.0F, 3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r2 = right.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 40).addBox(-2.0F, -6.5F, -2.0F, 4.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, 0.0F, 0.0F, -3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r3 = right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 6).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 14.0F, 0.0F, 3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r4 = right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(34, 24).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r5 = right.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 24).addBox(-6.0F, -2.0F, -2.0F, 13.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, -3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r6 = right.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(44, 55).addBox(-2.0F, -0.5F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -6.0F, 0.0F, -3.1416F, 0.0F, -1.5708F));

        PartDefinition cube_r7 = right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 57).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, -7.0F, 0.0F, -3.1416F, 0.0F, -1.5708F));

        return LayerDefinition.create(meshdefinition, 128, 128);
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
    public void setupAnim(FairkeeperSerpentCallerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.idleAnimationState, FairkeeperSerpentCallerAnimation.IDLE, ageInTicks);
        this.animate(entity.activeAnimationState, FairkeeperSerpentCallerAnimation.ACTIVE, ageInTicks);
    }

    public void headAnim(float netHeadYaw) {
        this.fairkeeper_serpent_caller.yRot = netHeadYaw * ((float)Math.PI / 180F);
    }
}
