package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.LargeWispAnimation;
import dev.hexnowloading.dungeonnowloading.entity.projectile.LargeWispProjectileEntity;
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

public class LargeWispProjectileModel extends HierarchicalModel<LargeWispProjectileEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "large_wisp_projectile"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private static final float TACKLE_FADE_IN_MS = 500.0F;

    private final ModelPart root;
    private final ModelPart bigWisp;
    private final AnimationState tackleLoop = new AnimationState();

    public LargeWispProjectileModel(ModelPart root) {
        this.root = root;
        this.bigWisp = root.getChild("Big_Wisp");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bigWisp = partdefinition.addOrReplaceChild("Big_Wisp", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        bigWisp.addOrReplaceChild("Wisp", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, -5.0F, -4.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        bigWisp.addOrReplaceChild("OutLayer", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -5.0F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LargeWispProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.tackleLoop.startIfStopped(entity.tickCount);
        this.tackleLoop.updateTime(ageInTicks, 1.0F);
        this.animateWeighted(LargeWispAnimation.TACKLE, this.tackleLoop.getAccumulatedTime(), this.getTackleAnimationWeight());
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
        this.bigWisp.xRot = 0.0F;
        this.bigWisp.yRot = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.bigWisp.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
