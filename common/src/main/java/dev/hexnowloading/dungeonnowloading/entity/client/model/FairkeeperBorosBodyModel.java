package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosPartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FairkeeperBorosBodyModel<T extends FairkeeperBorosPartEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "fairkeeper_boros_body"), "main");
    private final ModelPart boros_segment;
    private final ModelPart body;
    private final ModelPart dispenser;
    private final ModelPart stone_shield;
    private final ModelPart tail;
    private final ModelPart shield;
    private final ModelPart root;

    private float TILT_SPEED = 0.05F;

    public FairkeeperBorosBodyModel(ModelPart root) {
        this.root = root;
        this.boros_segment = root.getChild("boros_segment");
        this.body = this.boros_segment.getChild("body");
        this.dispenser = this.body.getChild("dispenser");
        this.stone_shield = this.body.getChild("stone_shield");
        this.tail = this.boros_segment.getChild("tail");
        this.shield = this.tail.getChild("shield");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition boros_segment = partdefinition.addOrReplaceChild("boros_segment", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = boros_segment.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 284).addBox(-15.0F, -18.0F, -23.5F, 30.0F, 11.0F, 47.0F, new CubeDeformation(0.0F))
                .texOffs(0, 230).addBox(-14.0F, -7.0F, -23.5F, 28.0F, 7.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition dispenser = body.addOrReplaceChild("dispenser", CubeListBuilder.create().texOffs(196, 167).addBox(15.0F, -3.0F, -4.5F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(196, 167).mirror().addBox(-17.0F, -3.0F, -4.5F, 2.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -13.0F, 0.0F));

        PartDefinition stone_shield = body.addOrReplaceChild("stone_shield", CubeListBuilder.create().texOffs(0, 183).addBox(-3.0F, -3.1F, -2.5F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.1F))
                .texOffs(0, 0).addBox(-18.0F, -0.1F, -23.5F, 36.0F, 18.0F, 47.0F, new CubeDeformation(0.1F))
                .texOffs(1, 196).addBox(-3.0F, -6.1F, -5.5F, 6.0F, 6.0F, 11.0F, new CubeDeformation(0.1F))
                .texOffs(64, 167).addBox(-3.0F, -4.1F, -19.5F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.1F))
                .texOffs(64, 167).addBox(-3.0F, -4.1F, 11.5F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, -24.9F, 0.0F));

        PartDefinition tail = boros_segment.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(238, 26).addBox(-9.0F, -4.25F, -21.7F, 18.0F, 13.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(146, 65).addBox(-12.0F, -13.25F, -0.8F, 24.0F, 22.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.75F, -2.2F));

        PartDefinition shield = tail.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(166, 0).addBox(-12.0F, -0.2143F, -28.7143F, 24.0F, 11.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(144, 167).addBox(-3.0F, -4.2143F, -23.7143F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(138, 119).addBox(-15.0F, -7.2143F, -10.7143F, 30.0F, 18.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(32, 167).addBox(-3.0F, -13.2143F, -0.7143F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(172, 167).addBox(-3.0F, -3.2143F, 19.2857F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 213).addBox(15.0F, -1.2143F, -0.7143F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 213).mirror().addBox(-21.0F, -1.2143F, -0.7143F, 6.0F, 6.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -9.0357F, 6.9143F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }


    @Override
    public void setupAnim(FairkeeperBorosPartEntity entity, float var2, float var3, float var4, float var5, float var6) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.tail.visible = entity.isTail();
        this.body.visible = !this.tail.visible;
        if (this.body.visible) {
            this.stone_shield.visible = entity.hasArmor();
            //this.dispenser.visible = !entity.isArmoredSegment();
            this.body.visible = entity.isModelVisible();
        }
        if (this.tail.visible) {
            this.shield.visible = entity.hasArmor();
            this.tail.visible = entity.isModelVisible();
        }


        float targetTilt = (float) Math.toRadians(90.0F);
        if (entity.isRotatable()) {
            targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        }
        entity.setPreviousTilt(Mth.lerp(this.TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.boros_segment.xRot = entity.getPreviousTilt();
    }

    public float getTiltAngle(FairkeeperBorosPartEntity entity) {
        Vec3 motion = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        if (!entity.isHeadEntityMoving()) {
            return (float) Math.toDegrees(entity.getPreviousTilt());
        }
        if (motion.y * motion.y > 0.01) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalSpeed));
            return pitch;
        }
        return 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        boros_segment.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
