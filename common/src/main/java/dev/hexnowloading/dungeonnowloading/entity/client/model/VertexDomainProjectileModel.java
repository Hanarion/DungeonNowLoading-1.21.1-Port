package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.VertexDomainProjectileAnimation;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexDomainProjectileEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VertexDomainProjectileModel<T extends VertexDomainProjectileEntity> extends HierarchicalModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "vertex_domain_projectile"), "main");
    private final ModelPart fairkeeper_domain_projectile;
    private final ModelPart orb;
    private final ModelPart wave;
    private final ModelPart whoosh;
    private final ModelPart big_whoosh;
    private final ModelPart small_whoosh_below;
    private final ModelPart small_whoosh_above;
    private final ModelPart root;

    private float waveAlpha;

    public VertexDomainProjectileModel(ModelPart root) {
        this.root = root;
        this.fairkeeper_domain_projectile = root.getChild("fairkeeper_domain_projectile");
        this.orb = this.fairkeeper_domain_projectile.getChild("orb");
        this.wave = this.fairkeeper_domain_projectile.getChild("wave");
        this.whoosh = this.fairkeeper_domain_projectile.getChild("whoosh");
        this.big_whoosh = this.whoosh.getChild("big_whoosh");
        this.small_whoosh_below = this.whoosh.getChild("small_whoosh_below");
        this.small_whoosh_above = this.whoosh.getChild("small_whoosh_above");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition fairkeeper_domain_projectile = partdefinition.addOrReplaceChild("fairkeeper_domain_projectile", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition orb = fairkeeper_domain_projectile.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition wave = fairkeeper_domain_projectile.addOrReplaceChild("wave", CubeListBuilder.create().texOffs(-112, 144).addBox(-56.0F, 0.0F, -56.0F, 112.0F, 0.0F, 112.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.1F, 0.0F));

        PartDefinition whoosh = fairkeeper_domain_projectile.addOrReplaceChild("whoosh", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition big_whoosh = whoosh.addOrReplaceChild("big_whoosh", CubeListBuilder.create().texOffs(-42, 32).addBox(-21.0F, 0.0F, -21.0F, 42.0F, 0.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition small_whoosh_below = whoosh.addOrReplaceChild("small_whoosh_below", CubeListBuilder.create().texOffs(-9, 85).addBox(-10.0F, 0.0F, -10.0F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition small_whoosh_above = whoosh.addOrReplaceChild("small_whoosh_above", CubeListBuilder.create().texOffs(-9, 85).addBox(-10.0F, 0.0F, -10.0F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(VertexDomainProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.getImpactAnimationTimeOut() > 0) {
            waveAlpha = Math.max((float) entity.getImpactAnimationTimeOut() / VertexDomainProjectileEntity.EXPANSION_DURATION, 0.0F);
        }

        float partialTicks = ageInTicks - entity.tickCount;
        float currentAge = VertexDomainProjectileEntity.EXPANSION_DURATION - entity.getExpansionTick() + partialTicks;
        float progress = Math.min(currentAge / VertexDomainProjectileEntity.EXPANSION_DURATION, 1.0F);

        float scale = Mth.clamp((float) (1.0F - Math.pow(1.0F - progress, 5.0F)), 0.0F, 1.0F);

        if (entity.getLife() > 0) {
            orb.visible = entity.getDyingTick() <= 0;
            wave.visible = true;
            waveAlpha = 0.8F + 0.2F * (float) Math.sin(ageInTicks * 0.2F);

            if (progress < 1.0F) {
                wave.xScale = scale;
                wave.zScale = scale;
            } else {
                wave.xScale = 1.0F;
                wave.zScale = 1.0F;
            }
        } else {
            orb.visible = true;
            wave.visible = false;
        }
        /*if (entity.getLife() <= 0) {
            fairkeeper_domain_projectile.setRotation(0, Mth.DEG_TO_RAD * 20 * ageInTicks, 0);
        }*/

        this.animate(entity.idleAnimationState, VertexDomainProjectileAnimation.IDLE, ageInTicks);
        this.animate(entity.spinAnimationState, VertexDomainProjectileAnimation.SPIN, ageInTicks);
        this.animate(entity.impactAnimationState, VertexDomainProjectileAnimation.IMPACT, ageInTicks);
    }

    public void renderToBufferWithEntity(VertexDomainProjectileEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // 1.21 ModelPart.render takes a packed ARGB int instead of r/g/b/a floats.
        float waveA = entity.getDyingTick() > 0 || (entity.getLife() > 0 && entity.getLife() < 20) ? alpha : waveAlpha;
        float whooshA = entity.getDyingTick() > 0 || (entity.getImpactAnimationTimeOut() > 0 && entity.getImpactAnimationTimeOut() < VertexDomainProjectileEntity.IMPACT_ANIMATION_DURATION) ? waveAlpha : alpha;
        orb.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
        wave.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(waveA, red, green, blue));
        whoosh.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.FastColor.ARGB32.colorFromFloat(whooshA, red, green, blue));
    }
}
