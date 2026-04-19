package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.SilkSpiderAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.SilkSpiderEntity;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

public class SilkSpiderModel <T extends SilkSpiderEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "silk_spider"), "main");

    private final ModelPart root;
    private final ModelPart Spider;
    private final ModelPart head;
    private final ModelPart fang;
    private final ModelPart fang2;
    private final ModelPart backpart;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart leg1;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart leg2;

    public SilkSpiderModel(ModelPart root) {
        this.root = root;
        this.Spider = root.getChild("Spider");
        this.head = this.Spider.getChild("head");
        this.fang = this.head.getChild("fang");
        this.fang2 = this.head.getChild("fang2");
        this.backpart = this.Spider.getChild("backpart");
        this.bone = this.Spider.getChild("bone");
        this.bone2 = this.Spider.getChild("bone2");
        this.leg1 = this.Spider.getChild("leg1");
        this.bone3 = this.Spider.getChild("bone3");
        this.bone4 = this.Spider.getChild("bone4");
        this.leg2 = this.Spider.getChild("leg2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Spider = partdefinition.addOrReplaceChild("Spider", CubeListBuilder.create().texOffs(33, 25).addBox(-3.0F, -2.0324F, -3.2814F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0887F, 16.9034F, 3.2814F));

        PartDefinition head = Spider.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 25).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0324F, -7.2813F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(50, 56).addBox(-3.0F, 0.0F, -4.0F, 3.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.5672F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, -4.0F, 3.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, -0.5672F));

        PartDefinition fang = head.addOrReplaceChild("fang", CubeListBuilder.create(), PartPose.offset(2.5F, 1.0F, -4.0F));

        PartDefinition cube_r3 = fang.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(11, 71).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition fang2 = head.addOrReplaceChild("fang2", CubeListBuilder.create(), PartPose.offset(-2.5F, 1.0F, -4.0F));

        PartDefinition cube_r4 = fang2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 71).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition backpart = Spider.addOrReplaceChild("backpart", CubeListBuilder.create(), PartPose.offset(0.0F, -1.4811F, 3.8051F));

        PartDefinition cube_r5 = backpart.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(49, 0).addBox(0.0F, 0.0588F, -6.034F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -6.0212F, 2.7097F, 0.4971F, -0.1719F, 0.3053F));

        PartDefinition cube_r6 = backpart.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(25, 56).addBox(0.0F, 0.0588F, -6.034F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -1.0212F, 5.6097F, 0.4971F, -0.1719F, 0.3053F));

        PartDefinition cube_r7 = backpart.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 42).addBox(0.0F, 0.0588F, -6.034F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -6.0212F, 2.7097F, 0.4971F, 0.1719F, -0.3053F));

        PartDefinition cube_r8 = backpart.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(33, 39).addBox(0.0F, 0.0588F, -6.034F, 0.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -1.0212F, 5.6097F, 0.4971F, 0.1719F, -0.3053F));

        PartDefinition cube_r9 = backpart.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, 0.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5513F, -1.0865F, 0.5236F, 0.0F, 0.0F));

        PartDefinition bone = Spider.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(3.0F, -0.0324F, -0.2814F));

        PartDefinition cube_r10 = bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(58, 49).addBox(0.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0229F, 0.1289F, 0.6123F));

        PartDefinition bone2 = Spider.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(3.0F, -0.0324F, 1.7186F));

        PartDefinition cube_r11 = bone2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(58, 44).addBox(0.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0465F, -0.2577F, 0.6169F));

        PartDefinition leg1 = Spider.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(50, 68).addBox(0.0F, -3.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 32).addBox(5.0F, -2.0F, -3.5F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 35).addBox(5.0F, -2.0F, 1.5F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 22).addBox(5.0F, -3.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.0F, 1.9676F, -1.2814F, 0.3107F, 0.6358F, 0.4957F));

        PartDefinition bone3 = Spider.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(-3.0F, -0.0324F, -0.2814F));

        PartDefinition cube_r12 = bone3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(49, 17).addBox(-11.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0229F, -0.1289F, -0.6123F));

        PartDefinition bone4 = Spider.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(-3.0F, -0.0324F, 1.7186F));

        PartDefinition cube_r13 = bone4.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(58, 39).addBox(-11.0F, -1.0F, -1.0F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0465F, 0.2577F, -0.6169F));

        PartDefinition leg2 = Spider.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 68).addBox(-13.0F, -2.0F, 1.5F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 65).addBox(-13.0F, -2.0F, -3.5F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(65, 68).addBox(-5.0F, -3.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 27).addBox(-14.0F, -3.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-3.0F, 1.9676F, -1.2814F, 0.3107F, -0.6358F, -0.4957F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    private final AnimationState idleLoop = new AnimationState();
    private static final Vector3f ANIM_VEC_CACHE = new Vector3f();

    protected void animateWalkWeighted(AnimationDefinition animationDefinition, float limbSwing, float limbSwingAmount, float maxAnimationSpeed, float animationScaleFactor, float weight) {
        float blend = Mth.clamp(weight, 0.0F, 1.0F);
        if (blend <= 0.0001F) return;

        long time = (long)(limbSwing * 50.0F * maxAnimationSpeed);
        float scale = Math.min(limbSwingAmount * animationScaleFactor, 1.0F) * blend;
        if (scale <= 0.0001F) return;

        KeyframeAnimations.animate(this, animationDefinition, time, scale, ANIM_VEC_CACHE);
    }

    private static float partialTickFromAge(float ageInTicks) {
        return ageInTicks - (float)Math.floor(ageInTicks);
    }

    @Override
    public void setupAnim(SilkSpiderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.idleLoop.startIfStopped(entity.tickCount);
        this.animate(idleLoop, SilkSpiderAnimation.IDLE, ageInTicks);

        float pt = partialTickFromAge(ageInTicks);
        float walkBack = Mth.lerp(pt, entity.clientWalkBackBlendO, entity.clientWalkBackBlend);
        walkBack = walkBack * walkBack * (3.0F - 2.0F * walkBack);
        this.animateWalkWeighted(SilkSpiderAnimation.WALK, limbSwing, limbSwingAmount, 2.0F, 1.0F, 1.0F - walkBack);
        this.animateWalkWeighted(SilkSpiderAnimation.WALK_BACK, limbSwing, limbSwingAmount, 3.0F, 1.0F, walkBack);
        this.animate(entity.shootAnimationState, SilkSpiderAnimation.SHOOT, ageInTicks);

        this.animateHeadLookTarget(netHeadYaw, headPitch);
    }

    private void animateHeadLookTarget(float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * (float) (Math.PI / 180.0);
        this.head.xRot = headPitch * (float) (Math.PI / 180.0);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Spider.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
