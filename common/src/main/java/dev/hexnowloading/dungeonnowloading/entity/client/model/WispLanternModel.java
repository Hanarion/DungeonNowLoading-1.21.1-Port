package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.WispLanternAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispLanternEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class WispLanternModel<T extends WispLanternEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "wisp_lantern"), "main");
    private final ModelPart root;
    private final ModelPart lantern;
    private final ModelPart uppart;
    private final ModelPart downpart;
    private final ModelPart candle;
    private final ModelPart cloth1;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart cloth2;
    private final ModelPart bone;
    private final ModelPart bone5;

    public WispLanternModel(ModelPart root) {
        this.root = root;
        this.lantern = root.getChild("lantern");
        this.uppart = this.lantern.getChild("uppart");
        this.downpart = this.lantern.getChild("downpart");
        this.candle = this.downpart.getChild("candle");
        this.cloth1 = this.downpart.getChild("cloth1");
        this.bone3 = this.cloth1.getChild("bone3");
        this.bone4 = this.bone3.getChild("bone4");
        this.cloth2 = this.downpart.getChild("cloth2");
        this.bone = this.cloth2.getChild("bone");
        this.bone5 = this.bone.getChild("bone5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition lantern = partdefinition.addOrReplaceChild("lantern", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 7.0F, 0.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition uppart = lantern.addOrReplaceChild("uppart", CubeListBuilder.create().texOffs(68, -14).addBox(0.0F, -7.0F, -7.5F, 0.0F, 4.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(68, 8).addBox(-7.5F, -7.0F, 0.0F, 15.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(30, 62).addBox(-5.5F, -4.0F, -5.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(25, 62).addBox(-5.5F, -4.0F, 4.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 61).addBox(4.5F, -4.0F, 4.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(15, 61).addBox(4.5F, -4.0F, -5.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -5.0F, -6.5F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(53, 18).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r1 = uppart.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(53, 0).addBox(-3.5F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r2 = uppart.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(63, 42).addBox(-1.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8F, -5.0F, 6.8F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r3 = uppart.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(62, 62).addBox(-1.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1F, -5.0F, -6.1F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r4 = uppart.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(63, 54).addBox(-2.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -5.0F, 7.5F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r5 = uppart.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(63, 51).addBox(-2.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4F, -5.0F, -5.4F, 0.0F, -2.3562F, 0.0F));

        PartDefinition downpart = lantern.addOrReplaceChild("downpart", CubeListBuilder.create().texOffs(10, 61).addBox(4.5F, -4.0F, 4.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(5, 61).addBox(4.5F, -4.0F, -5.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(0, 61).addBox(-5.5F, -4.0F, -5.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(60, 9).addBox(-5.5F, -4.0F, 4.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.001F))
                .texOffs(0, 15).addBox(-6.5F, 2.0F, -6.5F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(37, 30).addBox(-4.5F, 0.0F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(-0.001F))
                .texOffs(41, 51).addBox(0.0F, 1.0F, -7.5F, 0.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(41, 70).addBox(-7.5F, 1.0F, 0.0F, 15.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(13, 56).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r6 = downpart.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(63, 48).addBox(-1.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.8F, 3.0F, 6.8F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r7 = downpart.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(63, 45).addBox(-1.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1F, 3.0F, -6.1F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r8 = downpart.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(35, 64).addBox(-2.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, 3.0F, 7.5F, 0.0F, -2.3562F, 0.0F));

        PartDefinition cube_r9 = downpart.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(63, 57).addBox(-2.5F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4F, 3.0F, -5.4F, 0.0F, -2.3562F, 0.0F));

        PartDefinition candle = downpart.addOrReplaceChild("candle", CubeListBuilder.create().texOffs(39, 58).addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -1.0F));

        PartDefinition cube_r10 = candle.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(33, 45).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.0F, -0.7418F, 0.0F));

        PartDefinition cloth1 = downpart.addOrReplaceChild("cloth1", CubeListBuilder.create(), PartPose.offset(0.2F, 3.5F, -0.2F));

        PartDefinition bone3 = cloth1.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r11 = bone3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(55, 62).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone4 = bone3.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition cube_r12 = bone4.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(26, 45).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cloth2 = downpart.addOrReplaceChild("cloth2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.5F, 0.0F));

        PartDefinition bone = cloth2.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r13 = bone.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(48, 62).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition cube_r14 = bone5.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(53, 5).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(WispLanternEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        entity.idleAnimationState.startIfStopped(entity.tickCount);
        this.animate(entity.idleAnimationState, WispLanternAnimation.IDLE, ageInTicks);
        this.animate(entity.lookAroundAnimationState, WispLanternAnimation.LOOK_AROUND, ageInTicks);
        this.animate(entity.summonWispAnimationState, WispLanternAnimation.SUMMON_WISP, ageInTicks);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        lantern.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
