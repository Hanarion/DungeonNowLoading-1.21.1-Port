package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.FairkeeperBorosAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FairkeeperBorosModel<T extends FairkeeperBorosEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_boros_head"), "main");
    private final ModelPart boros;
    private final ModelPart head;
    private final ModelPart upper_jaw;
    private final ModelPart fuel_gland;
    private final ModelPart stone_helmet;
    private final ModelPart eye_socket;
    private final ModelPart fang_sheath;
    private final ModelPart small_teeth;
    private final ModelPart fang;
    private final ModelPart lower_jaw;
    private final ModelPart glottis;
    private final ModelPart tongue_sheath;
    private final ModelPart tongue;
    private final ModelPart teeth;
    private final ModelPart root;

    private float TILT_SPEED = 0.05F;

    public FairkeeperBorosModel(ModelPart root) {
        this.root = root;
        this.boros = root.getChild("boros");
        this.head = this.boros.getChild("head");
        this.upper_jaw = this.head.getChild("upper_jaw");
        this.fuel_gland = this.upper_jaw.getChild("fuel_gland");
        this.stone_helmet = this.upper_jaw.getChild("stone_helmet");
        this.eye_socket = this.stone_helmet.getChild("eye_socket");
        this.fang_sheath = this.upper_jaw.getChild("fang_sheath");
        this.small_teeth = this.upper_jaw.getChild("small_teeth");
        this.fang = this.upper_jaw.getChild("fang");
        this.lower_jaw = this.head.getChild("lower_jaw");
        this.glottis = this.lower_jaw.getChild("glottis");
        this.tongue_sheath = this.lower_jaw.getChild("tongue_sheath");
        this.tongue = this.lower_jaw.getChild("tongue");
        this.teeth = this.lower_jaw.getChild("teeth");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition boros = partdefinition.addOrReplaceChild("boros", CubeListBuilder.create(), PartPose.offset(0.0F, 27.0F, -3.0F));

        PartDefinition head = boros.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.3713F));

        PartDefinition upper_jaw = head.addOrReplaceChild("upper_jaw", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -9.0F, -41.0F, 30.0F, 11.0F, 44.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 20.0F));

        PartDefinition fuel_gland = upper_jaw.addOrReplaceChild("fuel_gland", CubeListBuilder.create().texOffs(154, 225).addBox(-15.0F, -19.0F, 12.0F, 4.0F, 7.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(154, 225).mirror().addBox(-49.0F, -19.0F, 12.0F, 4.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(30.0F, 12.0F, -20.0F));

        PartDefinition stone_helmet = upper_jaw.addOrReplaceChild("stone_helmet", CubeListBuilder.create().texOffs(138, 135).addBox(-17.0F, -24.0F, 2.0F, 34.0F, 14.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(148, 36).addBox(-17.0F, -22.0F, 18.0F, 34.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(122, 200).addBox(17.0F, -22.0F, 11.0F, 4.0F, 12.0F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(122, 200).mirror().addBox(-21.0F, -22.0F, 11.0F, 4.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 242).addBox(17.0F, -25.0F, 11.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(24, 242).mirror().addBox(-21.0F, -25.0F, 11.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(30, 226).addBox(17.0F, -17.0F, 2.0F, 4.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(30, 226).mirror().addBox(-21.0F, -17.0F, 2.0F, 4.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 253).addBox(17.0F, -27.0F, 1.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 253).mirror().addBox(-18.0F, -27.0F, 1.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 266).addBox(17.0F, -25.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 266).mirror().addBox(-18.0F, -25.0F, -12.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 104).addBox(-18.0F, -32.0F, -15.0F, 36.0F, 8.0F, 33.0F, new CubeDeformation(0.0F))
                .texOffs(240, 181).addBox(-3.0F, -35.0F, -15.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(122, 225).addBox(-3.0F, -38.0F, -1.0F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(82, 236).addBox(8.0F, -34.0F, 13.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(240, 159).addBox(8.0F, -34.0F, 18.0F, 10.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(238, 135).addBox(-18.0F, -34.0F, 13.0F, 10.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(240, 170).addBox(-18.0F, -34.0F, 18.0F, 10.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(139, 105).addBox(-17.0F, -24.0F, -17.0F, 34.0F, 11.0F, 19.0F, new CubeDeformation(0.0F))
                .texOffs(222, 196).addBox(9.0F, -24.0F, -24.0F, 8.0F, 16.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(220, 92).addBox(5.0F, -27.0F, -24.0F, 9.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(82, 226).addBox(-14.0F, -27.0F, -24.0F, 9.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(222, 219).addBox(-17.0F, -24.0F, -24.0F, 8.0F, 16.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(80, 145).addBox(-9.0F, -24.0F, -24.0F, 18.0F, 13.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -20.0F));

        PartDefinition cube_r1 = stone_helmet.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(208, 165).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 17.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(190, 200).addBox(22.0F, -5.0F, -4.0F, 8.0F, 17.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -35.0F, 22.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = stone_helmet.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(232, 16).addBox(-3.0F, 1.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(232, 0).addBox(23.0F, 1.0F, -3.0F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -46.0F, 33.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition eye_socket = stone_helmet.addOrReplaceChild("eye_socket", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fang_sheath = upper_jaw.addOrReplaceChild("fang_sheath", CubeListBuilder.create().texOffs(112, 241).addBox(9.0F, 2.0F, -37.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 242).addBox(-13.0F, 2.0F, -37.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition small_teeth = upper_jaw.addOrReplaceChild("small_teeth", CubeListBuilder.create().texOffs(148, 160).addBox(13.0F, 2.0F, -29.0F, 0.0F, 10.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(0, 191).addBox(-13.0F, 2.0F, -29.0F, 0.0F, 5.0F, 30.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fang = upper_jaw.addOrReplaceChild("fang", CubeListBuilder.create().texOffs(0, 278).addBox(11.0F, -17.0F, -36.0F, 0.0F, 12.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 278).mirror().addBox(-11.0F, -17.0F, -36.0F, 0.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition lower_jaw = head.addOrReplaceChild("lower_jaw", CubeListBuilder.create().texOffs(0, 55).addBox(-14.0F, 2.0F, -44.0F, 28.0F, 7.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 24.0F));

        PartDefinition glottis = lower_jaw.addOrReplaceChild("glottis", CubeListBuilder.create().texOffs(80, 165).addBox(-3.0F, -8.0F, -30.0F, 6.0F, 6.0F, 28.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r3 = glottis.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(220, 54).addBox(-3.0F, -3.0F, -6.5F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -4.75F, -8.5F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r4 = glottis.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(220, 73).addBox(-3.0F, -3.0F, -6.5F, 6.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -4.75F, -8.5F, 0.0F, 0.3927F, 0.0F));

        PartDefinition tongue_sheath = lower_jaw.addOrReplaceChild("tongue_sheath", CubeListBuilder.create().texOffs(148, 0).addBox(-5.0F, -2.0F, -34.0F, 10.0F, 4.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tongue = lower_jaw.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(60, 199).addBox(-3.0F, -11.0F, -35.0F, 6.0F, 2.0F, 25.0F, new CubeDeformation(0.0F))
                .texOffs(102, 243).addBox(1.0F, -16.0F, -35.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(178, 243).addBox(-3.0F, -16.0F, -35.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(154, 243).addBox(1.0F, -16.0F, -32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(166, 243).addBox(-3.0F, -16.0F, -32.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(208, 196).addBox(2.0F, -14.0F, -30.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(214, 196).addBox(-3.0F, -14.0F, -30.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, -24.0F));

        PartDefinition teeth = lower_jaw.addOrReplaceChild("teeth", CubeListBuilder.create().texOffs(206, 190).addBox(10.0F, -6.0F, -3.0F, 28.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(140, 46).mirror().addBox(38.0F, -15.0F, -3.0F, 0.0F, 15.0F, 40.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(140, 46).mirror().addBox(10.0F, -15.0F, -3.0F, 0.0F, 15.0F, 40.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-24.0F, 2.0F, -41.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        boros.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(FairkeeperBorosEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.stone_helmet.visible = entity.getHealth() / entity.getMaxHealth() > 0.5F;

        float targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        entity.setPreviousTilt(Mth.lerp(this.TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.boros.xRot = entity.getPreviousTilt();

        this.animate(entity.idleAnimationState, FairkeeperBorosAnimation.IDLE, ageInTicks);
        this.animate(entity.pursueOpenMouthAnimationState, FairkeeperBorosAnimation.PURSUE_OPEN_MOUTH, ageInTicks);
        this.animate(entity.pursueOpenedMouthAnimationState, FairkeeperBorosAnimation.PURSUE_OPENED_MOUTH, ageInTicks);
        this.animate(entity.pursueCloseMouthAnimationState, FairkeeperBorosAnimation.PURSUE_CLOSE_MOUTH, ageInTicks);

    }

    public float getTiltAngle(FairkeeperBorosEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y * motion.y > 0.01) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalSpeed));
            return pitch;
        }
        return 0.0F;
    }
}
