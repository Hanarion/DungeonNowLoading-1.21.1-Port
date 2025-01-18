package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperOurosEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FairkeeperOurosModel<T extends FairkeeperOurosEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_ouros_head"), "main");
    private final ModelPart ouros;
    private final ModelPart head;
    private final ModelPart tongue;
    private final ModelPart eye;
    private final ModelPart root;

    private static final float TILT_SPEED = 0.05F;

    public FairkeeperOurosModel(ModelPart root) {
        this.root = root;
        this.ouros = root.getChild("ouros");
        this.head = this.ouros.getChild("head");
        this.tongue = this.ouros.getChild("tongue");
        this.eye = this.ouros.getChild("eye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ouros = partdefinition.addOrReplaceChild("ouros", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = ouros.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 72).addBox(-21.0F, -12.0F, -21.0F, 42.0F, 12.0F, 42.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-24.0F, -36.0F, -24.0F, 48.0F, 24.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(41, 109).addBox(-19.0F, -47.0F, -19.0F, 38.0F, 11.0F, 38.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition tongue = ouros.addOrReplaceChild("tongue", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = tongue.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(142, 126).addBox(-3.0F, 0.0F, -16.0F, 6.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -21.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition eye = ouros.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(142, 142).addBox(24.0F, -18.0F, -13.0F, 2.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(92, 153).addBox(-26.0F, -18.0F, -13.0F, 2.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(FairkeeperOurosEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float targetTilt = (float) Math.toRadians(this.getTiltAngle(entity));
        entity.setPreviousTilt(Mth.lerp(TILT_SPEED, entity.getPreviousTilt(), targetTilt));
        this.ouros.xRot = entity.getPreviousTilt();
    }

    public float getTiltAngle(FairkeeperOurosEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y * motion.y > 0.01) {
            double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalSpeed));
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
