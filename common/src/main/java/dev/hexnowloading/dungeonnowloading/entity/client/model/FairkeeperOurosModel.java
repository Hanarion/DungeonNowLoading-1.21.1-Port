package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.FairkeeperOurosAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FairkeeperOurosModel<T extends FairkeeperOurosEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "fairkeeper_ouros_head"), "main");
    private final ModelPart ouros;
    private final ModelPart upper_jaw;
    private final ModelPart horns;
    private final ModelPart right_horn;
    private final ModelPart left_horn;
    private final ModelPart center;
    private final ModelPart left_side_horn;
    private final ModelPart right_side_horn;
    private final ModelPart fang;
    private final ModelPart small_teeth;
    private final ModelPart fang_sheath;
    private final ModelPart lower_jaw;
    private final ModelPart tongue;
    private final ModelPart teeth;
    private final ModelPart mouth_flap_right;
    private final ModelPart mouth_flap_left;
    private final ModelPart tongue_sheath;
    private final ModelPart root;

    private static final float TILT_SPEED = 0.05F;

    public FairkeeperOurosModel(ModelPart root) {
        this.root = root;
        this.ouros = root.getChild("ouros");
        this.upper_jaw = this.ouros.getChild("upper_jaw");
        this.horns = this.upper_jaw.getChild("horns");
        this.right_horn = this.horns.getChild("right_horn");
        this.left_horn = this.horns.getChild("left_horn");
        this.center = this.horns.getChild("center");
        this.left_side_horn = this.horns.getChild("left_side_horn");
        this.right_side_horn = this.horns.getChild("right_side_horn");
        this.fang = this.upper_jaw.getChild("fang");
        this.small_teeth = this.upper_jaw.getChild("small_teeth");
        this.fang_sheath = this.upper_jaw.getChild("fang_sheath");
        this.lower_jaw = this.ouros.getChild("lower_jaw");
        this.tongue = this.lower_jaw.getChild("tongue");
        this.teeth = this.lower_jaw.getChild("teeth");
        this.mouth_flap_right = this.teeth.getChild("mouth_flap_right");
        this.mouth_flap_left = this.teeth.getChild("mouth_flap_left");
        this.tongue_sheath = this.lower_jaw.getChild("tongue_sheath");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ouros = partdefinition.addOrReplaceChild("ouros", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition upper_jaw = ouros.addOrReplaceChild("upper_jaw", CubeListBuilder.create().texOffs(76, 125).addBox(-6.0F, -13.0F, -33.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-15.0F, -11.0F, -43.0F, 30.0F, 11.0F, 44.0F, new CubeDeformation(0.0F))
                .texOffs(0, 150).addBox(8.0F, -15.0F, -43.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(80, 153).addBox(-15.0F, -15.0F, -43.0F, 7.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(164, 134).addBox(-4.0F, -13.0F, -36.0F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(40, 131).addBox(-4.0F, -13.0F, -21.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 104).addBox(15.0F, -11.0F, -15.0F, 3.0F, 11.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(40, 139).addBox(14.0F, 0.0F, -15.0F, 4.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 131).addBox(-18.0F, 0.0F, -15.0F, 4.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(80, 139).addBox(-18.0F, -13.0F, -11.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(140, 55).addBox(8.0F, -13.0F, -11.0F, 10.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(164, 122).addBox(-18.0F, -13.0F, -15.0F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(164, 128).addBox(11.0F, -13.0F, -15.0F, 7.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(38, 104).addBox(-18.0F, -11.0F, -15.0F, 3.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 21.0F));

        PartDefinition horns = upper_jaw.addOrReplaceChild("horns", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, -21.6287F));

        PartDefinition right_horn = horns.addOrReplaceChild("right_horn", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = right_horn.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(80, 167).addBox(1.0F, 0.0F, -4.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.0F, -27.0F, 19.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = right_horn.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(148, 0).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -27.0F, 22.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r3 = right_horn.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(34, 158).addBox(-3.0F, 1.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -38.0F, 33.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition left_horn = horns.addOrReplaceChild("left_horn", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r4 = left_horn.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 164).addBox(1.0F, 0.0F, -4.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, -27.0F, 19.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r5 = left_horn.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(140, 69).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -27.0F, 22.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r6 = left_horn.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(156, 145).addBox(-3.0F, 1.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -38.0F, 33.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition center = horns.addOrReplaceChild("center", CubeListBuilder.create(), PartPose.offset(1.0F, -26.0F, -3.0F));

        PartDefinition cube_r7 = center.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(156, 161).addBox(-2.0F, -2.5F, -3.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.4749F, -6.4749F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r8 = center.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(160, 107).addBox(-2.0F, -1.5F, -4.0F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.4749F, 5.5251F, -0.4363F, 0.0F, 0.0F));

        PartDefinition cube_r9 = center.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(124, 145).addBox(-4.0F, -8.5F, -4.0F, 8.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.4749F, -2.4749F, -0.4363F, 0.0F, 0.0F));

        PartDefinition left_side_horn = horns.addOrReplaceChild("left_side_horn", CubeListBuilder.create().texOffs(76, 104).addBox(-2.5F, -2.5F, -4.5F, 6.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(148, 42).addBox(1.5F, 2.5F, -5.5F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(140, 94).addBox(-1.5F, -1.5F, 10.5F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.5F, -17.5F, 15.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r10 = left_side_horn.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(148, 25).addBox(-1.5F, -2.5F, -4.5F, 5.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition right_side_horn = horns.addOrReplaceChild("right_side_horn", CubeListBuilder.create().texOffs(118, 104).addBox(-3.5F, -2.5F, -4.5F, 6.0F, 6.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(160, 94).addBox(-4.5F, 2.5F, -5.5F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 164).addBox(-2.5F, -1.5F, 10.5F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.5F, -17.5F, 15.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r11 = right_side_horn.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(127, 128).addBox(-3.5F, -2.5F, -4.5F, 5.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition fang = upper_jaw.addOrReplaceChild("fang", CubeListBuilder.create().texOffs(242, -7).addBox(36.0F, -11.0F, -15.6287F, 0.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(242, -7).mirror().addBox(14.0F, -11.0F, -15.6287F, 0.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-25.0F, 1.0F, -22.0F));

        PartDefinition small_teeth = upper_jaw.addOrReplaceChild("small_teeth", CubeListBuilder.create().texOffs(196, -18).addBox(38.0F, -1.0F, -8.6287F, 0.0F, 6.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(196, -18).addBox(12.0F, -1.0F, -8.6287F, 0.0F, 6.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(-25.0F, 1.0F, -22.0F));

        PartDefinition fang_sheath = upper_jaw.addOrReplaceChild("fang_sheath", CubeListBuilder.create().texOffs(232, 18).addBox(34.0F, -1.0F, -16.6287F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(232, 18).addBox(12.0F, -1.0F, -16.6287F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-25.0F, 1.0F, -22.0F));

        PartDefinition lower_jaw = ouros.addOrReplaceChild("lower_jaw", CubeListBuilder.create().texOffs(0, 55).addBox(-14.0F, -7.0F, -20.6287F, 28.0F, 7.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tongue = lower_jaw.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(0, 179).addBox(-3.0F, -9.0F, -33.6287F, 6.0F, 2.0F, 25.0F, new CubeDeformation(0.0F))
                .texOffs(0, 206).addBox(1.0F, -14.0F, -33.6287F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(10, 206).addBox(-3.0F, -14.0F, -33.6287F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(20, 206).addBox(1.0F, -14.0F, -30.6287F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 206).addBox(-3.0F, -14.0F, -30.6287F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(44, 206).addBox(2.0F, -12.0F, -28.6287F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 206).addBox(-3.0F, -12.0F, -28.6287F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition teeth = lower_jaw.addOrReplaceChild("teeth", CubeListBuilder.create().texOffs(0, 214).addBox(-14.0F, -13.0F, -20.6287F, 28.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 180).addBox(14.0F, -22.0F, -20.6287F, 0.0F, 15.0F, 40.0F, new CubeDeformation(0.0F))
                .texOffs(0, 180).mirror().addBox(-14.0F, -22.0F, -20.6287F, 0.0F, 15.0F, 40.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mouth_flap_right = teeth.addOrReplaceChild("mouth_flap_right", CubeListBuilder.create().texOffs(0, 223).addBox(-14.0F, -22.0F, 7.3713F, 0.0F, 15.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, 0.0F));

        PartDefinition mouth_flap_left = teeth.addOrReplaceChild("mouth_flap_left", CubeListBuilder.create().texOffs(0, 223).addBox(13.5F, -22.0F, 7.3713F, 0.0F, 15.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tongue_sheath = lower_jaw.addOrReplaceChild("tongue_sheath", CubeListBuilder.create().texOffs(71, 220).addBox(-5.0F, -11.0F, -10.6287F, 10.0F, 4.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(FairkeeperOurosEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        entity.setPreviousTilt(Mth.lerp(TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.ouros.xRot = entity.getPreviousTilt();

        this.animate(entity.idleAnimationState, FairkeeperOurosAnimation.IDLE, ageInTicks);
        this.animate(entity.openMouthAnimationState, FairkeeperOurosAnimation.MOUTH_OPEN, ageInTicks);
        this.animate(entity.openedMouthAnimationState, FairkeeperOurosAnimation.MOUTH_OPENED, ageInTicks);
        this.animate(entity.closeMouthAnimationState, FairkeeperOurosAnimation.MOUTH_CLOSED, ageInTicks);
    }

    public float getTiltAngle(FairkeeperOurosEntity entity) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        Vec3 motion = entity.getDeltaMovement();
        if (motion.y * motion.y > 0.01) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalSpeed));
            return pitch;
        }
        return 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ouros.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
