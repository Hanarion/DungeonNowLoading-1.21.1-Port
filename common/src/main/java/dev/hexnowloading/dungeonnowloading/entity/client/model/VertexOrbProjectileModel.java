package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexOrbProjectileEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VertexOrbProjectileModel<T extends VertexOrbProjectileEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "vertex_orb_projectile"), "main");
    private final ModelPart vertex_orb;
    private final ModelPart orb;
    private final ModelPart wave;
    private final ModelPart root;

    private float waveAlpha;
    private float landedAgeInTicks;
    private VertexOrbProjectileEntity entity;

    public VertexOrbProjectileModel(ModelPart root) {
        this.root = root;
        this.vertex_orb = root.getChild("vertex_orb");
        this.orb = this.vertex_orb.getChild("orb");
        this.wave = this.vertex_orb.getChild("wave");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition vertex_orb = partdefinition.addOrReplaceChild("vertex_orb", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition orb = vertex_orb.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 80).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition wave = vertex_orb.addOrReplaceChild("wave", CubeListBuilder.create().texOffs(0, 0).addBox(-40.0F, 0.4F, -40.0F, 80.0F, 0.0F, 80.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }

    @Override
    public void setupAnim(VertexOrbProjectileEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        final float EXPANSION_DURATION = 40.0F;
        float partialTicks = ageInTicks - entity.tickCount;
        float currentAge = EXPANSION_DURATION - entity.getExpansionTick() + partialTicks;
        float progress = Math.min(currentAge / EXPANSION_DURATION, 1.0F);

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

            orb.setRotation(Mth.DEG_TO_RAD * 10 * ageInTicks, Mth.DEG_TO_RAD * 10 * ageInTicks, Mth.DEG_TO_RAD * 10 * ageInTicks);
        }
    }

    /*@Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
         // Value between 0 and 1

        orb.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        wave.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, waveAlpha);
        //vertex_orb.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }*/

    public void renderToBufferWithEntity(VertexOrbProjectileEntity entity, PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        orb.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        wave.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, entity.getDyingTick() > 0 || (entity.getLife() > 0 && entity.getLife() < 20) ? alpha : waveAlpha);
    }

    @Override
    public ModelPart root() { return this.root; }
}
