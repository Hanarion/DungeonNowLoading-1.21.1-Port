package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.WhimperAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.WhimperEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Arrays;

public class WhimperModel<T extends WhimperEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "whimper"), "main");
    private final ModelPart Whimper;
    private final ModelPart right_hand;
    private final ModelPart Lantern;
    private final ModelPart left_hand;
    private final ModelPart head;
    private final ModelPart right_ear;
    private final ModelPart left_ear;
    private final ModelPart tail;
    private final ModelPart Hat;
    private final ModelPart root;

    public WhimperModel(ModelPart root) {
        this.root = root;
        this.Whimper = root.getChild("Whimper");
        this.right_hand = this.Whimper.getChild("right_hand");
        this.Lantern = this.right_hand.getChild("Lantern");
        this.left_hand = this.Whimper.getChild("left_hand");
        this.head = this.Whimper.getChild("head");
        this.right_ear = this.head.getChild("right_ear");
        this.left_ear = this.head.getChild("left_ear");
        this.tail = this.head.getChild("tail");
        this.Hat = this.head.getChild("Hat");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Whimper = partdefinition.addOrReplaceChild("Whimper", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition right_hand = Whimper.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(46, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 4.5F, -6.5F));

        PartDefinition Lantern = right_hand.addOrReplaceChild("Lantern", CubeListBuilder.create().texOffs(52, 56).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 56).addBox(-1.5F, 5.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.2F))
                .texOffs(36, 60).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition cube_r1 = Lantern.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(27, 60).addBox(0.0F, -1.0F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition left_hand = Whimper.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(34, 43).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 4.5F, -6.5F));

        PartDefinition head = Whimper.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-4.0F, -1.0F, 0.0F));

        PartDefinition right_ear_r1 = right_ear.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(22, 43).addBox(0.0F, 0.0F, -3.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(4.0F, -1.0F, 0.0F));

        PartDefinition left_ear_r1 = left_ear.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(34, 22).addBox(0.0F, 0.0F, -3.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition tail = head.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 4.0F));

        PartDefinition tail_r1 = tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(46, 28).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition Hat = head.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(32, -1).addBox(4.0F, -2.0F, -7.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(21, 29).addBox(-5.0F, -2.0F, -7.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(31, 12).addBox(-4.0F, 0.0F, -7.0F, 8.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(0.0F, -7.0F, -5.0F, 0.0F, 6.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 9.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition cube_r2 = Hat.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(34, 15).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -2.3F, -4.5F, 0.0F, 0.0F, 0.7854F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean hasLanternSkin = entity.getSkin() == WhimperEntity.Skin.LANTERN;

        this.Lantern.visible = hasLanternSkin;
        this.Hat.visible = hasLanternSkin;

        this.animate(entity.attackAnimationState, WhimperAnimation.ATTACK, ageInTicks);
        this.animate(entity.blessingAnimationState, WhimperAnimation.BLESSING, ageInTicks);
        this.animate(entity.idleBreakAnimationState, WhimperAnimation.IDLE_BREAK, ageInTicks);
        this.animate(entity.idleBreakLanternAnimationState, WhimperAnimation.IDLE_BREAK_LANTERN, ageInTicks);


        this.animateHeadLookTarget(netHeadYaw, headPitch);
        this.animateIdlePose(ageInTicks);
    }

    private void animateHeadLookTarget(float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * ((float)Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
    }

    private void animateIdlePose(float ageInTicks) {

        float AGEINTICKS_TO_SECONDS = ageInTicks / 20;

        float UPDOWN_OSCILLATION_SECOND = 2.0F;
        float UPDOWN_DISTANCE_PIXEL = 1.0f;
        float updownRate = UPDOWN_DISTANCE_PIXEL * Mth.sin(AGEINTICKS_TO_SECONDS * Mth.TWO_PI / UPDOWN_OSCILLATION_SECOND);
        ModelPart[] UPDOWN_ANIMATION_PARTS = {this.head, this.right_hand, this.left_hand};
        Arrays.stream(UPDOWN_ANIMATION_PARTS).forEach(modelPart -> modelPart.y += updownRate);

        float FLAPPING_OSCILLATION_SECOND = 2.0F;
        float FLAPPING_ROTATION_DEGREE = 25;
        float flappingRate = Mth.DEG_TO_RAD * FLAPPING_ROTATION_DEGREE * Mth.cos(AGEINTICKS_TO_SECONDS * Mth.TWO_PI / FLAPPING_OSCILLATION_SECOND);
        this.right_ear.zRot += flappingRate;
        this.left_ear.zRot -= flappingRate;
        this.tail.xRot += flappingRate;

        float HAND_OSCILLATION_SECOND = 3.0F;
        float HAND_ROTATION_DEGREE = 10;
        float handRotationRate = Mth.DEG_TO_RAD * HAND_ROTATION_DEGREE * Mth.cos(AGEINTICKS_TO_SECONDS * Mth.TWO_PI / HAND_OSCILLATION_SECOND);
        this.right_hand.xRot += handRotationRate;
        this.left_hand.xRot += handRotationRate;
        this.right_hand.yRot += handRotationRate;
        this.left_hand.yRot -= handRotationRate;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        Whimper.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public ModelPart root() { return root; }
}
