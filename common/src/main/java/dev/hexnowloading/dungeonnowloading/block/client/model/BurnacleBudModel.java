package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.animation.BurnacleBudAnimation;
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

public class BurnacleBudModel extends HierarchicalModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "burnacle_bud"), "main");
    private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

    private final ModelPart root;
    private final ModelPart burnacle;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart bone3;
    private final ModelPart bone4;

    public BurnacleBudModel(ModelPart root) {
        this.root = root;
        this.burnacle = root.getChild("burnacle");
        this.bone = this.burnacle.getChild("bone");
        this.bone2 = this.burnacle.getChild("bone2");
        this.bone3 = this.burnacle.getChild("bone3");
        this.bone4 = this.burnacle.getChild("bone4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition burnacle = partdefinition.addOrReplaceChild("burnacle", CubeListBuilder.create(), PartPose.offset(-6.0F, 24.0F, -5.0F));

        PartDefinition bone = burnacle.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(20, 8).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        PartDefinition bone2 = burnacle.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(20, 0).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(11.5F, 0.0F, 8.5F));

        PartDefinition bone3 = burnacle.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5F, 0.0F, 1.5F));

        PartDefinition bone4 = burnacle.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(0, 12).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 0.0F, 8.5F));

        return LayerDefinition.create(meshdefinition, 32, 32);
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
        KeyframeAnimations.animate(this, BurnacleBudAnimation.BURNACLE_IDLE3, (long) (ageInTicks * 50.0F), 1.0F, ANIMATION_VECTOR_CACHE);
    }

    private void animateSpray(float progress) {
        long elapsedMillis = (long) (progress * BurnacleBlockEntity.SPRAY_ANIMATION_LENGTH_SECONDS * 1000.0F);
        KeyframeAnimations.animate(this, BurnacleBudAnimation.BURNACLE_SPRAY3, elapsedMillis, 1.0F, ANIMATION_VECTOR_CACHE);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        burnacle.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
