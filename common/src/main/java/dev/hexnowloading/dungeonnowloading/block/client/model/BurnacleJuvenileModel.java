package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.animation.BurnacleJuvenileAnimation;
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

public class BurnacleJuvenileModel extends HierarchicalModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "burnacle_juvenile"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;
    private final ModelPart burnacle;
    private final ModelPart group3;
    private final ModelPart group2;
    private final ModelPart group;

    public BurnacleJuvenileModel(ModelPart root) {
        this.root = root;
        this.burnacle = root.getChild("burnacle");
        this.group3 = this.burnacle.getChild("group3");
        this.group2 = this.burnacle.getChild("group2");
        this.group = this.burnacle.getChild("group");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition burnacle = partdefinition.addOrReplaceChild("burnacle", CubeListBuilder.create(), PartPose.offset(4.0F, 24.0F, -5.0F));

        PartDefinition group3 = burnacle.addOrReplaceChild("group3", CubeListBuilder.create().texOffs(0, 16).addBox(-3.5F, -6.0F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, 0.0F, 5.5F));

        PartDefinition group2 = burnacle.addOrReplaceChild("group2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -9.0F, -3.5F, 7.0F, 9.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 1.5F));

        PartDefinition group = burnacle.addOrReplaceChild("group", CubeListBuilder.create().texOffs(28, 0).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 9.5F));

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
        KeyframeAnimations.animate(this, BurnacleJuvenileAnimation.BURNACLE_IDLE2, (long) (ageInTicks * 50.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    private void animateSpray(float progress) {
        long elapsedMillis = (long) (progress * BurnacleBlockEntity.SPRAY_ANIMATION_LENGTH_SECONDS * 1000.0F);
        KeyframeAnimations.animate(this, BurnacleJuvenileAnimation.BURNACLE_SPRAY2, elapsedMillis, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        burnacle.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
