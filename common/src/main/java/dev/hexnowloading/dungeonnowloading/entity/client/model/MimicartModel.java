package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.MimicartAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.MimicartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class MimicartModel<T extends MimicartEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "mimicart"), "main");
    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart tounge;
    private final ModelPart tou1;
    private final ModelPart tou2;
    private final ModelPart tou3;
    private final ModelPart tou4;
    private final ModelPart tou5;
    private final ModelPart tou6;
    private final ModelPart Right;
    private final ModelPart teethright;
    private final ModelPart Left;
    private final ModelPart teethleft;
    private final ModelPart Front;
    private final ModelPart teethfront;
    private final ModelPart Back;
    private final ModelPart teethback;
    private final ModelPart eyes;
    private final ModelPart Bottom;

    public MimicartModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.tounge = this.bone.getChild("tounge");
        this.tou1 = this.tounge.getChild("tou1");
        this.tou2 = this.tou1.getChild("tou2");
        this.tou3 = this.tou2.getChild("tou3");
        this.tou4 = this.tou3.getChild("tou4");
        this.tou5 = this.tou4.getChild("tou5");
        this.tou6 = this.tou5.getChild("tou6");
        this.Right = this.bone.getChild("Right");
        this.teethright = this.Right.getChild("teethright");
        this.Left = this.bone.getChild("Left");
        this.teethleft = this.Left.getChild("teethleft");
        this.Front = this.bone.getChild("Front");
        this.teethfront = this.Front.getChild("teethfront");
        this.Back = this.bone.getChild("Back");
        this.teethback = this.Back.getChild("teethback");
        this.eyes = this.bone.getChild("eyes");
        this.Bottom = root.getChild("Bottom");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(-4.2667F, 12.3333F, 0.0F));

        PartDefinition tounge = bone.addOrReplaceChild("tounge", CubeListBuilder.create(), PartPose.offset(4.2667F, 9.6667F, 5.0F));

        PartDefinition tou1 = tounge.addOrReplaceChild("tou1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = tou1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(108, 69).addBox(-4.0F, -13.0F, 0.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tou2 = tou1.addOrReplaceChild("tou2", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -13.0F));

        PartDefinition cube_r2 = tou2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(108, 54).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tou3 = tou2.addOrReplaceChild("tou3", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 13.0F));

        PartDefinition cube_r3 = tou3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(108, 39).addBox(-4.0F, -13.0F, 0.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition tou4 = tou3.addOrReplaceChild("tou4", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -13.0F));

        PartDefinition cube_r4 = tou4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(108, 24).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tou5 = tou4.addOrReplaceChild("tou5", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 13.0F));

        PartDefinition cube_r5 = tou5.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(108, 84).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tou6 = tou5.addOrReplaceChild("tou6", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -13.0F));

        PartDefinition cube_r6 = tou6.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(108, 99).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition Right = bone.addOrReplaceChild("Right", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -10.0F, -10.0F, 2.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(10.2667F, 11.6667F, 0.0F));

        PartDefinition teethright = Right.addOrReplaceChild("teethright", CubeListBuilder.create().texOffs(45, 78).addBox(-4.0F, 0.1F, -10.0F, 4.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition Left = bone.addOrReplaceChild("Left", CubeListBuilder.create().texOffs(0, 30).addBox(-2.0F, -10.0F, -10.0F, 2.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.7333F, 11.6667F, 0.0F));

        PartDefinition teethleft = Left.addOrReplaceChild("teethleft", CubeListBuilder.create().texOffs(0, 78).addBox(0.0F, 0.0F, -10.0F, 4.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.9F, 0.0F));

        PartDefinition Front = bone.addOrReplaceChild("Front", CubeListBuilder.create().texOffs(100, 12).addBox(-6.0F, -10.0F, -2.0F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.2667F, 11.6667F, -8.0F));

        PartDefinition teethfront = Front.addOrReplaceChild("teethfront", CubeListBuilder.create().texOffs(0, 75).addBox(-6.0F, 0.25F, -0.25F, 12.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.7F, 0.0F));

        PartDefinition Back = bone.addOrReplaceChild("Back", CubeListBuilder.create().texOffs(100, 0).addBox(-6.0F, -10.0F, 0.0F, 12.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.2667F, 11.6667F, 8.0F));

        PartDefinition teethback = Back.addOrReplaceChild("teethback", CubeListBuilder.create().texOffs(0, 72).addBox(-6.0F, 0.55F, -2.75F, 12.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition eyes = bone.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(44, 18).addBox(8.0F, -10.0F, -18.0F, 0.0F, 10.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(44, 48).addBox(-8.0F, -10.0F, -18.0F, 0.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(4.2667F, 11.6667F, 8.0F));

        PartDefinition Bottom = partdefinition.addOrReplaceChild("Bottom", CubeListBuilder.create().texOffs(44, 0).addBox(-6.0F, -2.0F, -8.0F, 12.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(MimicartEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.tounge.visible = entity.getAnimationState().equals(MimicartEntity.MimicartAnimationState.SWING) || entity.getAnimationState().equals(MimicartEntity.MimicartAnimationState.SNATCH);

        this.animate(entity.openAnimationState, MimicartAnimation.OPEN, ageInTicks);
        this.animate(entity.swingAnimationState, MimicartAnimation.SWING, ageInTicks);
        this.animate(entity.snatchAnimationState, MimicartAnimation.SNATCH, ageInTicks);
        this.animate(entity.openAndCloseAnimationState, MimicartAnimation.OPEN_AND_CLOSE, ageInTicks);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
