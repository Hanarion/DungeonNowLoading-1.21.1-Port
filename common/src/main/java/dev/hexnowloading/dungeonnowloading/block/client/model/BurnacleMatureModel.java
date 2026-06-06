package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.animation.BurnacleMatureAnimation;
import dev.hexnowloading.dungeonnowloading.block.entity.BurnacleBlockEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.animation.KeyframeAnimations;
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

public class BurnacleMatureModel extends HierarchicalModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "burnacle_mature"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;
    private final ModelPart burnacle;
    private final ModelPart big;
    private final ModelPart small;

    public BurnacleMatureModel(ModelPart root) {
        this.root = root;
        this.burnacle = root.getChild("burnacle");
        this.big = this.burnacle.getChild("big");
        this.small = this.burnacle.getChild("small");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition burnacle = partdefinition.addOrReplaceChild("burnacle", CubeListBuilder.create(), PartPose.offset(1.5F, 24.0F, -1.5F));

        PartDefinition big = burnacle.addOrReplaceChild("big", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -11.0F, -4.5F, 9.0F, 11.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition small = burnacle.addOrReplaceChild("small", CubeListBuilder.create().texOffs(0, 20).addBox(-2.5F, -6.001F, -1.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 0.0F, 5.0F));

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
        KeyframeAnimations.animate(this, BurnacleMatureAnimation.BURNACLE_IDLE, (long) (ageInTicks * 50.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    private void animateSpray(float progress) {
        long elapsedMillis = (long) (progress * BurnacleBlockEntity.SPRAY_ANIMATION_LENGTH_SECONDS * 1000.0F);
        KeyframeAnimations.animate(this, BurnacleMatureAnimation.BURNACLE_SPRAY, elapsedMillis, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        burnacle.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public ModelPart burnacle() {
        return this.burnacle;
    }

    public ModelPart big() {
        return this.big;
    }

    public ModelPart small() {
        return this.small;
    }
}
