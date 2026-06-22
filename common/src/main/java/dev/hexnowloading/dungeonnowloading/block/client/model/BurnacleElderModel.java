package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.animation.BurnacleElderAnimation;
import dev.hexnowloading.dungeonnowloading.block.entity.BurnacleBlockEntity;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public class BurnacleElderModel extends HierarchicalModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "burnacle_elder"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;
    private final ModelPart bone;

    public BurnacleElderModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -16.0F, -7.0F, 14.0F, 16.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setupAnim(BurnacleBlockEntity blockEntity, float partialTick, float ageInTicks) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animateIdle(ageInTicks);

        if (blockEntity.isSpraying()) {
            this.animateSpray(blockEntity.getSprayAnimationProgress(partialTick));
        }
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animateIdle(ageInTicks);
    }

    private void animateIdle(float ageInTicks) {
        KeyframeAnimations.animate(this, BurnacleElderAnimation.BURNACLE_IDLE4, (long) (ageInTicks * 50.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    private void animateSpray(float progress) {
        long elapsedMillis = (long) (progress * BurnacleBlockEntity.SPRAY_ANIMATION_LENGTH_SECONDS * 1000.0F);
        KeyframeAnimations.animate(this, BurnacleElderAnimation.BURNACLE_SPRAY4, elapsedMillis, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
