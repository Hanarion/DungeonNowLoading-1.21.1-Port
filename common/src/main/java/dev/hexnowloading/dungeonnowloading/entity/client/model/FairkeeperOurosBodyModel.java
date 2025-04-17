package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.FairkeeperOurosBodyAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FairkeeperOurosBodyModel<T extends FairkeeperOurosPartEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_ouros_body"), "main");
    private final ModelPart root;
    private final ModelPart ouros_segment;
    private final ModelPart body;
    private final ModelPart cannon;
    private final ModelPart barrel;
    private final ModelPart trap_door_front;
    private final ModelPart trap_door_back;
    private final ModelPart horn2;
    private final ModelPart horn5;
    private final ModelPart horn4;
    private final ModelPart horn6;
    private final ModelPart horn;
    private final ModelPart horn3;
    private final ModelPart tail;

    private static final float TILT_SPEED = 0.05F;

    public FairkeeperOurosBodyModel(ModelPart root) {
        this.root = root;
        this.cannon = root.getChild("cannon");
        this.barrel = this.cannon.getChild("barrel");
        this.ouros_segment = root.getChild("ouros_segment");
        this.body = this.ouros_segment.getChild("body");
        this.trap_door_front = this.body.getChild("trap_door_front");
        this.trap_door_back = this.body.getChild("trap_door_back");
        this.horn2 = this.body.getChild("horn2");
        this.horn5 = this.body.getChild("horn5");
        this.horn4 = this.body.getChild("horn4");
        this.horn6 = this.body.getChild("horn6");
        this.horn = this.body.getChild("horn");
        this.horn3 = this.body.getChild("horn3");
        this.tail = this.ouros_segment.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cannon = partdefinition.addOrReplaceChild("cannon", CubeListBuilder.create().texOffs(118, 138).addBox(5.0F, -0.2F, -6.0F, 2.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(24, 144).mirror().addBox(-7.0F, -2.2F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(118, 138).mirror().addBox(-7.0F, -0.2F, -6.0F, 2.0F, 5.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 144).addBox(5.0F, -2.2F, -3.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(78, 138).addBox(-8.0F, -4.2F, -2.0F, 16.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.2F, 0.0F));

        PartDefinition barrel = cannon.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(80, 124).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(40, 124).addBox(-5.0F, -13.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 124).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.2F, 0.0F));

        PartDefinition ouros_segment = partdefinition.addOrReplaceChild("ouros_segment", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = ouros_segment.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 58).addBox(-14.0F, -7.0F, -23.5F, 28.0F, 7.0F, 47.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-15.0F, -18.0F, -23.5F, 30.0F, 11.0F, 47.0F, new CubeDeformation(0.0F))
                .texOffs(112, 112).addBox(15.0F, -18.0F, -7.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(112, 112).addBox(15.0F, -18.0F, 8.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(112, 112).addBox(15.0F, -18.0F, -23.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(112, 112).mirror().addBox(-17.0F, -18.0F, -7.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(112, 112).mirror().addBox(-17.0F, -18.0F, -23.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(112, 112).mirror().addBox(-17.0F, -18.0F, 8.5F, 2.0F, 11.0F, 15.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 144).addBox(10.0F, -22.0F, 11.5F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(146, 112).addBox(10.0F, -22.0F, -19.5F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(78, 146).addBox(-14.0F, -22.0F, 11.5F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(146, 124).addBox(-14.0F, -22.0F, -19.5F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition trap_door_front = body.addOrReplaceChild("trap_door_front", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, -10.0F));

        PartDefinition cube_r1 = trap_door_front.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 112).addBox(-9.0F, -1.5F, -9.5F, 18.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.5F, 3.1416F, 0.0F, 3.1416F));

        PartDefinition trap_door_back = body.addOrReplaceChild("trap_door_back", CubeListBuilder.create().texOffs(0, 112).addBox(-9.0F, -2.0F, -10.0F, 18.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -18.0F, 10.0F));

        PartDefinition horn2 = body.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(146, 136).addBox(-4.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(146, 146).addBox(-7.0F, -1.5F, -5.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -12.5F, -13.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition horn5 = body.addOrReplaceChild("horn5", CubeListBuilder.create().texOffs(146, 136).addBox(-4.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(146, 146).addBox(-7.0F, -1.5F, 2.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, -12.5F, -13.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition horn4 = body.addOrReplaceChild("horn4", CubeListBuilder.create().texOffs(146, 136).addBox(-4.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(146, 146).addBox(-7.0F, -1.5F, 2.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, -12.5F, 19.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition horn6 = body.addOrReplaceChild("horn6", CubeListBuilder.create().texOffs(146, 136).addBox(-4.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(146, 146).addBox(-7.0F, -1.5F, -5.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -12.5F, 19.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition horn = body.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(40, 138).addBox(-6.0F, -3.5F, -3.5F, 12.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(146, 136).addBox(6.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0F, -12.5F, 1.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition horn3 = body.addOrReplaceChild("horn3", CubeListBuilder.create().texOffs(40, 138).addBox(-6.0F, -3.5F, -3.5F, 12.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(146, 136).addBox(6.0F, -2.5F, -2.5F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0F, -12.5F, 1.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition tail = ouros_segment.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 204).addBox(-9.0F, -13.0F, -23.9F, 18.0F, 13.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 158).addBox(-12.0F, -22.0F, -3.0F, 24.0F, 22.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(FairkeeperOurosPartEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.tail.visible = entity.isTail();
        this.body.visible = !this.tail.visible;

        this.ouros_segment.visible = entity.isModelVisible();

        float targetTilt = (float) Math.toRadians(90.0F);
        if (entity.isRotatable()) {
            targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        }
        entity.setPreviousTilt(Mth.lerp(TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.ouros_segment.xRot = entity.getPreviousTilt();

        this.animate(entity.idleAnimationState, FairkeeperOurosBodyAnimation.IDLE, ageInTicks);
        this.animate(entity.scuttleOpenAnimationState, FairkeeperOurosBodyAnimation.SCUTTLE_OPEN, ageInTicks);
        this.animate(entity.scuttleCloseAnimationState, FairkeeperOurosBodyAnimation.SCUTTLE_CLOSE, ageInTicks);
        this.animate(entity.cannonOpenAnimationState, FairkeeperOurosBodyAnimation.CANNON_OPEN, ageInTicks);
        this.animate(entity.cannonCloseAnimationState, FairkeeperOurosBodyAnimation.CANNON_CLOSE, ageInTicks);
        this.animate(entity.cannonIdleAnimationState, FairkeeperOurosBodyAnimation.CANNON_IDLE, ageInTicks);

        float cannonYaw = entity.getCannonTargetYaw();
        float bodyYaw = entity.getYRot();
        float relativeYaw = Mth.wrapDegrees(-cannonYaw + bodyYaw);
        float pitch = Mth.wrapDegrees(entity.getCannonTargetPitch() + 90);

        this.cannon.yRot = (float) Math.toRadians(relativeYaw);
        this.barrel.xRot = (float) Math.toRadians(pitch);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ouros_segment.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        cannon.render(poseStack, vertexConsumer, 0xF000F0, packedOverlay, red, green, blue, alpha);
    }

    public float getTiltAngle(FairkeeperOurosPartEntity entity) {
        Vec3 motion = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        if (!entity.isHeadEntityMoving()) {
            return (float) Math.toDegrees(entity.getPreviousTilt());
        }
        if (motion.y * motion.y > 0.01) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(motion.y, horizontalSpeed));
            return pitch;
        }
        return 0.0F;
    }

    @Override
    public ModelPart root() {
        return root;
    }

}