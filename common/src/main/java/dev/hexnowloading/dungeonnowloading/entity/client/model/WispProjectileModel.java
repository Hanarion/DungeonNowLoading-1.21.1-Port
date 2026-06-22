package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.WispAnimation;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WispProjectileEntity;
import net.minecraft.client.animation.AnimationDefinition;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

public class WispProjectileModel extends HierarchicalModel<WispProjectileEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "wisp_projectile"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private static final float TACKLE_FADE_IN_MS = 500.0F;

    private final ModelPart root;
    private final ModelPart wisp;
    private final ModelPart outerlayer;
    private final AnimationState tackleLoop = new AnimationState();

    public WispProjectileModel(ModelPart root) {
        this.root = root;
        this.wisp = root.getChild("wisp");
        this.outerlayer = root.getChild("outerlayer");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wisp = partdefinition.addOrReplaceChild("wisp", CubeListBuilder.create().texOffs(0, 15).addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 14.5F, 0.5F));

        PartDefinition outerlayer = partdefinition.addOrReplaceChild("outerlayer", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(-0.4F)), PartPose.offset(0.5F, 14.5F, 0.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(WispProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.tackleLoop.startIfStopped(entity.tickCount);
        this.tackleLoop.updateTime(ageInTicks, 1.0F);
        this.animateWeighted(WispAnimation.TACKLE, this.tackleLoop.getAccumulatedTime(), this.getTackleAnimationWeight());
        this.anchorFacingToEntityLook();
    }

    private float getTackleAnimationWeight() {
        float progress = Mth.clamp((float) this.tackleLoop.getAccumulatedTime() / TACKLE_FADE_IN_MS, 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private void animateWeighted(AnimationDefinition animationDefinition, long animationTime, float weight) {
        float clampedWeight = Mth.clamp(weight, 0.0F, 1.0F);
        if (clampedWeight <= 0.0001F) {
            return;
        }

        KeyframeAnimations.animate(this, animationDefinition, animationTime, clampedWeight, ANIMATION_VECTOR_CACHE);
    }

    private void anchorFacingToEntityLook() {
        this.wisp.xRot = 0.0F;
        this.wisp.yRot = 0.0F;
        this.outerlayer.xRot = 0.0F;
        this.outerlayer.yRot = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.wisp.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.outerlayer.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
