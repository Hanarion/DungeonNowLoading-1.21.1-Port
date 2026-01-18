package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.BrokenGarholdAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.BrokenGarholdEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class BrokenGarholdModel<T extends BrokenGarholdEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "broken_garhold"), "main");
    private final ModelPart root;
    private final ModelPart Garhold;
    private final ModelPart bone;
    private final ModelPart rightwing;
    private final ModelPart leftwing;
    private final ModelPart gate;
    private final ModelPart LeftBar;
    private final ModelPart leftgate;
    private final ModelPart thirdBar;
    private final ModelPart rightgate;
    private final ModelPart RightBar;
    private final ModelPart UpBar;
    private final ModelPart Rightdoor;
    private final ModelPart BottBar;
    private final ModelPart Leftdoor;

    public BrokenGarholdModel(ModelPart root) {
        this.root = root;
        this.Garhold = root.getChild("Garhold");
        this.bone = this.Garhold.getChild("bone");
        this.rightwing = this.bone.getChild("rightwing");
        this.leftwing = this.bone.getChild("leftwing");
        this.gate = this.bone.getChild("gate");
        this.LeftBar = this.gate.getChild("LeftBar");
        this.leftgate = this.LeftBar.getChild("leftgate");
        this.thirdBar = this.gate.getChild("thirdBar");
        this.rightgate = this.thirdBar.getChild("rightgate");
        this.RightBar = this.gate.getChild("RightBar");
        this.UpBar = this.RightBar.getChild("UpBar");
        this.Rightdoor = this.gate.getChild("Rightdoor");
        this.BottBar = this.Rightdoor.getChild("BottBar");
        this.Leftdoor = this.gate.getChild("Leftdoor");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Garhold = partdefinition.addOrReplaceChild("Garhold", CubeListBuilder.create(), PartPose.offset(0.0714F, 24.2857F, -0.4286F));

        PartDefinition bone = Garhold.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightwing = bone.addOrReplaceChild("rightwing", CubeListBuilder.create().texOffs(57, 117).addBox(-16.0F, -15.0F, 0.0F, 18.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.8452F, -41.2938F, -0.0194F));

        PartDefinition leftwing = bone.addOrReplaceChild("leftwing", CubeListBuilder.create().texOffs(101, 116).addBox(-2.0F, -15.0F, 0.0F, 18.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.1548F, -41.2938F, -0.0194F));

        PartDefinition gate = bone.addOrReplaceChild("gate", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 7.4667F, -6.9333F, 16.0F, 32.0F, 16.0F, new CubeDeformation(-0.001F))
                .texOffs(0, 0).addBox(-8.0F, 7.4667F, -6.9333F, 16.0F, 32.0F, 16.0F, new CubeDeformation(-0.001F))
                .texOffs(64, 11).addBox(-9.0F, 0.4667F, -1.9333F, 18.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-9.0F, 5.4667F, -4.9333F, 18.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(64, 23).addBox(-4.0F, 0.4667F, -7.9333F, 8.0F, 7.0F, 10.0F, new CubeDeformation(0.01F))
                .texOffs(102, 62).addBox(-3.0F, -0.5333F, -7.9333F, 6.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(48, 105).addBox(8.0F, 2.4667F, -6.9333F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(64, 40).addBox(-10.0F, 4.4667F, -7.9333F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 93).addBox(-10.0F, 4.4667F, 0.0667F, 20.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 105).addBox(-12.0F, 2.4667F, -6.9333F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-12.0F, 2.4667F, 9.0667F, 24.0F, 39.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 99).addBox(6.0F, 7.4667F, 7.0667F, 3.0F, 32.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 87).addBox(-10.0F, 4.4667F, 7.0667F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1548F, -41.7605F, -0.0861F));

        PartDefinition LeftBar = gate.addOrReplaceChild("LeftBar", CubeListBuilder.create().texOffs(0, 99).addBox(-1.55F, 0.0F, -1.55F, 3.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.55F, 7.4667F, -6.3833F));

        PartDefinition leftgate = LeftBar.addOrReplaceChild("leftgate", CubeListBuilder.create().texOffs(196, 50).addBox(-8.0F, -16.0F, 0.0F, 15.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.55F, 16.0F, -0.55F));

        PartDefinition thirdBar = gate.addOrReplaceChild("thirdBar", CubeListBuilder.create().texOffs(12, 99).addBox(-1.0F, 0.0F, -1.25F, 3.0F, 32.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 7.4667F, -6.6833F));

        PartDefinition rightgate = thirdBar.addOrReplaceChild("rightgate", CubeListBuilder.create().texOffs(196, 12).addBox(-7.5F, -16.0F, -0.1F, 15.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.0F, -0.25F));

        PartDefinition RightBar = gate.addOrReplaceChild("RightBar", CubeListBuilder.create(), PartPose.offset(-15.0F, 39.4667F, 16.0667F));

        PartDefinition UpBar = RightBar.addOrReplaceChild("UpBar", CubeListBuilder.create().texOffs(36, 99).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.5F, -32.0F, -7.5F));

        PartDefinition Rightdoor = gate.addOrReplaceChild("Rightdoor", CubeListBuilder.create().texOffs(0, 138).addBox(-0.5F, 0.0F, -9.0F, 9.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(51, 78).addBox(-1.5F, -1.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.5F, 39.4667F, 1.0667F));

        PartDefinition BottBar = Rightdoor.addOrReplaceChild("BottBar", CubeListBuilder.create().texOffs(37, 114).addBox(-1.5F, -16.0F, -1.5F, 3.0F, 16.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(138, 78).addBox(-0.5F, -16.0F, -8.5F, 0.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 7.5F));

        PartDefinition Leftdoor = gate.addOrReplaceChild("Leftdoor", CubeListBuilder.create().texOffs(80, 77).mirror().addBox(-1.5F, -1.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 163).addBox(-8.5F, 0.0F, -9.0F, 9.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 126).addBox(-2.5F, -4.0F, -9.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5F, 39.4667F, 1.0667F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(BrokenGarholdEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        entity.idleAnimationState.startIfStopped(entity.tickCount);

        if (entity.isHanging()) {
            this.animate(entity.idleAnimationState, BrokenGarholdAnimation.BROKEN_IDLE, ageInTicks);
        }
        this.animate(entity.fallingStartAnimationState, BrokenGarholdAnimation.FALLING_START, ageInTicks);
        this.animate(entity.fallingAnimationState, BrokenGarholdAnimation.FALLING, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Garhold.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
