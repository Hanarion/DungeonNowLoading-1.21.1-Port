package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.ReaperSpiderAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
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

public class ReaperSpiderModel <T extends ReaperSpiderEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "reaper_spider"), "main");

    private final ModelPart root;
    private final ModelPart spider;
    private final ModelPart torso;
    private final ModelPart onlytorso;
    private final ModelPart uppertorso;
    private final ModelPart head;
    private final ModelPart fang2;
    private final ModelPart fang;
    private final ModelPart scythearm;
    private final ModelPart bone;
    private final ModelPart scythe;
    private final ModelPart scythearm2;
    private final ModelPart bone2;
    private final ModelPart scythe2;
    private final ModelPart frontleg1;
    private final ModelPart frontleg2;
    private final ModelPart back;
    private final ModelPart backleg;
    private final ModelPart backleg3;
    private final ModelPart backleg4;
    private final ModelPart backleg2;

    public ReaperSpiderModel(ModelPart root) {
        this.root = root;
        this.spider = root.getChild("spider");
        this.torso = this.spider.getChild("torso");
        this.onlytorso = this.torso.getChild("onlytorso");
        this.uppertorso = this.onlytorso.getChild("uppertorso");
        this.head = this.uppertorso.getChild("head");
        this.fang2 = this.head.getChild("fang2");
        this.fang = this.head.getChild("fang");
        this.scythearm = this.uppertorso.getChild("scythearm");
        this.bone = this.scythearm.getChild("bone");
        this.scythe = this.bone.getChild("scythe");
        this.scythearm2 = this.uppertorso.getChild("scythearm2");
        this.bone2 = this.scythearm2.getChild("bone2");
        this.scythe2 = this.bone2.getChild("scythe2");
        this.frontleg1 = this.uppertorso.getChild("frontleg1");
        this.frontleg2 = this.uppertorso.getChild("frontleg2");
        this.back = this.onlytorso.getChild("back");
        this.backleg = this.spider.getChild("backleg");
        this.backleg3 = this.spider.getChild("backleg3");
        this.backleg4 = this.spider.getChild("backleg4");
        this.backleg2 = this.spider.getChild("backleg2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition spider = partdefinition.addOrReplaceChild("spider", CubeListBuilder.create(), PartPose.offset(3.0F, 11.0F, -3.5858F));

        PartDefinition torso = spider.addOrReplaceChild("torso", CubeListBuilder.create(), PartPose.offset(-3.0F, -5.3033F, 8.4749F));

        PartDefinition onlytorso = torso.addOrReplaceChild("onlytorso", CubeListBuilder.create(), PartPose.offset(0.0F, 5.6861F, -10.6813F));

        PartDefinition uppertorso = onlytorso.addOrReplaceChild("uppertorso", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 11.0F));

        PartDefinition cube_r1 = uppertorso.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.6172F, -10.2078F, 0.7854F, 0.0F, 0.0F));

        PartDefinition head = uppertorso.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 44).addBox(-4.0F, -5.4887F, -7.3057F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(54, 59).addBox(-4.0F, 2.5113F, -7.3057F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 61).addBox(4.0F, 2.5113F, -7.3057F, 0.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -11.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(64, 76).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.4887F, -7.3057F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(22, 81).addBox(-4.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -1.4887F, -5.3057F, 0.0F, 0.9599F, 0.0F));

        PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(11, 79).addBox(-1.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -1.4887F, -5.3057F, 0.0F, -0.9599F, 0.0F));

        PartDefinition fang2 = head.addOrReplaceChild("fang2", CubeListBuilder.create().texOffs(81, 20).addBox(-1.5F, 2.75F, -0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(80, 69).addBox(-1.5F, -0.25F, -2.05F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -0.2387F, -7.2557F, 0.0F, 0.0F, -0.2182F));

        PartDefinition fang = head.addOrReplaceChild("fang", CubeListBuilder.create().texOffs(17, 61).addBox(-1.5F, 3.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(40, 64).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -0.4887F, -7.3057F, 0.0F, 0.0F, 0.2182F));

        PartDefinition scythearm = uppertorso.addOrReplaceChild("scythearm", CubeListBuilder.create().texOffs(71, 64).addBox(-0.5545F, -2.0693F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7545F, 4.3225F, -5.5154F, 0.0F, 0.0F, 0.6981F));

        PartDefinition bone = scythearm.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(4.9455F, -1.3693F, 0.0F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(59, 71).addBox(-6.1756F, -1.7373F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 0.3F, 0.0F, 0.0F, 0.0F, 0.7418F));

        PartDefinition scythe = bone.addOrReplaceChild("scythe", CubeListBuilder.create(), PartPose.offset(-4.0F, -4.0F, 0.0F));

        PartDefinition cube_r6 = scythe.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(77, 116).addBox(-9.1756F, -8.7373F, 0.0F, 24.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(77, 116).addBox(-9.1756F, -8.7373F, 0.0F, 24.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

        PartDefinition scythearm2 = uppertorso.addOrReplaceChild("scythearm2", CubeListBuilder.create().texOffs(11, 74).addBox(-6.4455F, -2.0693F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.7545F, 4.3225F, -5.5154F, 0.0F, 0.0F, -0.6981F));

        PartDefinition bone2 = scythearm2.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(-4.9455F, -1.3693F, 0.0F));

        PartDefinition cube_r7 = bone2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(71, 59).addBox(-1.8244F, -1.7373F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.3F)), PartPose.offsetAndRotation(0.0F, 0.3F, 0.0F, 0.0F, 0.0F, -0.7418F));

        PartDefinition scythe2 = bone2.addOrReplaceChild("scythe2", CubeListBuilder.create(), PartPose.offset(4.0F, -4.0F, 0.0F));

        PartDefinition cube_r8 = scythe2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(77, 102).mirror().addBox(-14.3023F, -4.7715F, 0.0F, 24.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.1F, 0.4F, 0.0F, 0.0F, 0.0F, -0.7418F));

        PartDefinition frontleg1 = uppertorso.addOrReplaceChild("frontleg1", CubeListBuilder.create(), PartPose.offsetAndRotation(5.0F, 3.6172F, -9.7936F, 0.7681F, 0.1841F, 1.4272F));

        PartDefinition cube_r9 = frontleg1.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(17, 64).addBox(0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.5F))
                .texOffs(30, 76).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 2.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r10 = frontleg1.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(51, 20).addBox(6.0F, -1.0F, 1.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, 2.0F, 0.8F, 0.7854F, 0.0F, 0.0F));

        PartDefinition frontleg2 = uppertorso.addOrReplaceChild("frontleg2", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 3.6172F, -9.7936F, 0.7681F, -0.1841F, -1.4272F));

        PartDefinition cube_r11 = frontleg2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(64, 44).addBox(-9.0F, -1.0F, -1.0F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.5F))
                .texOffs(47, 76).addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 2.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r12 = frontleg2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(66, 20).addBox(-13.0F, -1.0F, 1.0F, 7.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, 2.0F, 0.8F, 0.7854F, 0.0F, 0.0F));

        PartDefinition back = onlytorso.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, -6.8828F, 10.2922F));

        PartDefinition cube_r13 = back.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -6.5F, -0.5F, 14.0F, 12.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r14 = back.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(33, 49).addBox(0.0F, 0.0795F, -4.6781F, 0.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 3.9523F, 5.5437F, 0.0865F, 0.0114F, -0.1304F));

        PartDefinition cube_r15 = back.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(51, 0).addBox(0.0F, 0.0795F, -4.6781F, 0.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, 3.9523F, 5.5437F, 0.0865F, -0.0114F, 0.1304F));

        PartDefinition cube_r16 = back.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(72, 0).addBox(0.0F, -3.2821F, -0.0095F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -2.8639F, 7.2876F, 0.0876F, 0.0869F, 0.0076F));

        PartDefinition cube_r17 = back.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 73).addBox(0.0F, -3.2821F, -0.0095F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.0F, -2.8639F, 7.2876F, 0.0876F, -0.0869F, -0.0076F));

        PartDefinition backleg = spider.addOrReplaceChild("backleg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.7F, -4.7407F, 5.9014F, -0.7936F, -0.2157F, 1.2789F));

        PartDefinition cube_r18 = backleg.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(54, 49).addBox(10.0F, 2.0F, 10.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 3.7407F, -10.3156F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r19 = backleg.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(17, 69).addBox(3.0F, 1.0F, 10.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 6.7407F, -8.3156F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r20 = backleg.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(51, 15).addBox(9.0F, 1.0F, 10.0F, 13.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-3.0F, 5.3407F, -9.7156F, 0.7854F, 0.0F, 0.0F));

        PartDefinition backleg3 = spider.addOrReplaceChild("backleg3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.7F, -1.7407F, 14.9014F, -0.7936F, -0.2157F, 1.2789F));

        PartDefinition cube_r21 = backleg3.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(-3, 106).addBox(11.1246F, -1.8286F, 9.7222F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1451F, 5.904F, -8.2813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r22 = backleg3.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 113).addBox(4.1246F, -2.8286F, 9.7222F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.1451F, 8.904F, -6.2813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r23 = backleg3.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(4, 121).addBox(13.1246F, -2.8286F, 9.7222F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(-7.1451F, 7.504F, -7.6813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition backleg4 = spider.addOrReplaceChild("backleg4", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.7F, -1.7407F, 14.9014F, -0.7936F, 0.2157F, -1.2789F));

        PartDefinition cube_r24 = backleg4.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(-3, 106).mirror().addBox(-20.1246F, -1.8286F, 9.7222F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(6.1451F, 5.904F, -8.2813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r25 = backleg4.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(0, 113).mirror().addBox(-15.1246F, -2.8286F, 9.7222F, 11.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.1451F, 8.904F, -6.2813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r26 = backleg4.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(4, 121).mirror().addBox(-23.1246F, -2.8286F, 9.7222F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(7.1451F, 7.504F, -7.6813F, 0.7854F, 0.0F, 0.0F));

        PartDefinition backleg2 = spider.addOrReplaceChild("backleg2", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.7F, -4.7407F, 5.9014F, -0.7936F, 0.2157F, -1.2789F));

        PartDefinition cube_r27 = backleg2.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(54, 54).addBox(-19.0F, 2.0F, 10.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 3.7407F, -10.3156F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r28 = backleg2.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(38, 71).addBox(-11.0F, 1.0F, 10.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 6.7407F, -8.3156F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r29 = backleg2.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(33, 44).addBox(-22.0F, 1.0F, 10.0F, 13.0F, 2.0F, 2.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(3.0F, 5.3407F, -9.7156F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        spider.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private final AnimationState idleLoop = new AnimationState();
    private static final Vector3f ANIM_VEC_CACHE = new Vector3f();

    protected void animateWalkWeighted(AnimationDefinition animationDefinition, float limbSwing, float limbSwingAmount, float maxAnimationSpeed, float animationScaleFactor, float weight) {
        float blend = Mth.clamp(weight, 0.0F, 1.0F);
        if (blend <= 0.0001F) {
            return;
        }

        long time = (long) (limbSwing * 50.0F * maxAnimationSpeed);
        float scale = Math.min(limbSwingAmount * animationScaleFactor, 1.0F) * blend;
        if (scale <= 0.0001F) {
            return;
        }

        KeyframeAnimations.animate(this, animationDefinition, time, scale, ANIM_VEC_CACHE);
    }

    private static float partialTickFromAge(float ageInTicks) {
        return ageInTicks - (float) Math.floor(ageInTicks);
    }

    @Override
    public void setupAnim(ReaperSpiderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.idleLoop.startIfStopped(entity.tickCount);
        this.animate(idleLoop, ReaperSpiderAnimation.IDLE, ageInTicks);

        this.animate(entity.doubleSlashAnimationState, ReaperSpiderAnimation.DOUBLE_SLASH, ageInTicks);
        this.animate(entity.singleSlashAnimationState, ReaperSpiderAnimation.SINGLE_SLASH, ageInTicks);
        this.animate(entity.tacklingAnimationState, ReaperSpiderAnimation.TACKLING, ageInTicks);
        this.animate(entity.recoveryAnimationState, ReaperSpiderAnimation.RECOVERY, ageInTicks);
        this.animate(entity.windUpAnimationState, ReaperSpiderAnimation.WIND_UP, ageInTicks);

        float pt = partialTickFromAge(ageInTicks);
        float walkBack = Mth.lerp(pt, entity.clientWalkBackBlendO, entity.clientWalkBackBlend);
        walkBack = walkBack * walkBack * (3.0F - 2.0F * walkBack);
        this.animateWalkWeighted(ReaperSpiderAnimation.WALK, limbSwing, limbSwingAmount, 1.0F, 1.0F, 1.0F - walkBack);
        this.animateWalkWeighted(ReaperSpiderAnimation.WALK_BACK, limbSwing, limbSwingAmount, 1.5F, 1.0F, walkBack);
    }
}
