package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosPartEntity;
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
    private final ModelPart ouros;
    private final ModelPart tail;
    private final ModelPart body;
    private final ModelPart shield;
    private final ModelPart dispenser;
    private final ModelPart root;

    private static final float TILT_SPEED = 0.05F;

    public FairkeeperOurosBodyModel(ModelPart root) {
        this.root = root;
        this.ouros = root.getChild("ouros");
        this.tail = this.ouros.getChild("tail");
        this.body = this.ouros.getChild("body");
        this.shield = this.body.getChild("shield");
        this.dispenser = this.body.getChild("dispenser");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ouros = partdefinition.addOrReplaceChild("ouros", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = ouros.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(-60, -30).addBox(-16.0F, -32.0F, -8.0F, 32.0F, 32.0F, 32.0F, new CubeDeformation(0.0F))
                .texOffs(-27, -13).addBox(-8.0F, -16.0F, -23.0F, 16.0F, 16.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = ouros.addOrReplaceChild("body", CubeListBuilder.create().texOffs(-84, -44).addBox(-21.0F, -33.0F, -23.0F, 42.0F, 33.0F, 46.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition shield = body.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -36.0F, -24.0F, 48.0F, 24.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition dispenser = body.addOrReplaceChild("dispenser", CubeListBuilder.create().texOffs(-62, -10).addBox(-27.0F, -18.0F, -6.0F, 54.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(FairkeeperOurosPartEntity entity, float var2, float var3, float var4, float var5, float var6) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.tail.visible = entity.isTail();
        this.body.visible = !this.tail.visible;
        if (this.body.visible) {
            this.shield.visible = entity.hasArmor();
            this.dispenser.visible = !entity.isArmoredSegment();
        }

        this.ouros.visible = entity.isModelVisible();

        float targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        entity.setPreviousTilt(Mth.lerp(TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.ouros.xRot = entity.getPreviousTilt();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ouros.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
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