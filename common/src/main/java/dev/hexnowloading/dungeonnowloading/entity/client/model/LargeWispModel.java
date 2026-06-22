package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.LargeWispAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.LargeWispEntity;
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

public class LargeWispModel<T extends LargeWispEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "large_wisp"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();
    private static final float IDLE_FADE_OUT_MS = 500.0F;
    private static final float TACKLE_START_FADE_IN_MS = 140.0F;
    private final ModelPart bigWisp;
    private final ModelPart wisp;
    private final ModelPart outLayer;
    private final ModelPart root;

    public LargeWispModel(ModelPart root) {
        this.root = root;
        this.bigWisp = root.getChild("Big_Wisp");
        this.wisp = this.bigWisp.getChild("Wisp");
        this.outLayer = this.bigWisp.getChild("OutLayer");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Big_Wisp = partdefinition.addOrReplaceChild("Big_Wisp", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition Wisp = Big_Wisp.addOrReplaceChild("Wisp", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, -5.0F, -4.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition OutLayer = Big_Wisp.addOrReplaceChild("OutLayer", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -5.0F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        entity.idleAnimationState.updateTime(ageInTicks, 1.0F);
        this.animateWeighted(LargeWispAnimation.IDLE, entity.idleAnimationState.getAccumulatedTime(), this.getIdleAnimationWeight(entity));

        this.animate(entity.flareUpAnimationState, LargeWispAnimation.FLARE_UP, ageInTicks);
        entity.tackleStartAnimationState.updateTime(ageInTicks, 1.0F);
        this.animateWeighted(LargeWispAnimation.TACKLE_START, entity.tackleStartAnimationState.getAccumulatedTime(), this.getTackleStartAnimationWeight(entity));
        this.animate(entity.tackleAnimationState, LargeWispAnimation.TACKLE, ageInTicks);

        this.anchorFacingToEntityLook();
    }

    private float getIdleAnimationWeight(T entity) {
        float fadeOut = this.getStartedFade(entity.flareUpAnimationState, IDLE_FADE_OUT_MS);
        if (entity.tackleStartAnimationState.isStarted() || entity.tackleAnimationState.isStarted()) {
            fadeOut = 1.0F;
        }
        return 1.0F - fadeOut;
    }

    private float getTackleStartAnimationWeight(T entity) {
        return this.getStartedFade(entity.tackleStartAnimationState, TACKLE_START_FADE_IN_MS);
    }

    private float getStartedFade(AnimationState animationState, float fadeDurationMs) {
        if (!animationState.isStarted()) {
            return 0.0F;
        }

        float progress = Mth.clamp((float) animationState.getAccumulatedTime() / fadeDurationMs, 0.0F, 1.0F);
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
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.bigWisp.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
