package dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.seeping_soul.SeepingSoulChaosSpawnerAnimation;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class SeepingSoulChaosSpawnerModel<T extends SeepingSoulEntity> extends HierarchicalModel<T> implements SeepingSoulRenderModel {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "seeping_soul_chaos_spawner"), "main");
    private final ModelPart Skullsoul;
    private final ModelPart bone;
    private final ModelPart eye;
    private final ModelPart eye2;
    private final ModelPart jaw;
    private final ModelPart root;

    public SeepingSoulChaosSpawnerModel(ModelPart root) {
        this.root = root;
        this.Skullsoul = root.getChild("Skullsoul");
        this.bone = this.Skullsoul.getChild("bone");
        this.eye = this.bone.getChild("eye");
        this.eye2 = this.bone.getChild("eye2");
        this.jaw = this.bone.getChild("jaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Skullsoul = partdefinition.addOrReplaceChild("Skullsoul", CubeListBuilder.create(), PartPose.offset(0.0F, 13.875F, 0.225F));

        PartDefinition bone = Skullsoul.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -5.875F, -1.225F, 11.0F, 5.0F, 5.0F, new CubeDeformation(0.01F))
                .texOffs(20, 10).addBox(-2.5F, -0.875F, -1.225F, 5.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(16, 17).addBox(-6.5F, -1.875F, -1.225F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(3.5F, -1.875F, -1.225F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition eye = bone.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(30, 12).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(-4.0F, -2.375F, -1.325F));

        PartDefinition eye2 = bone.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(30, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.01F)), PartPose.offset(4.0F, -2.375F, -1.325F));

        PartDefinition jaw = bone.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -0.25F, -1.25F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(20, 12).addBox(-2.5F, -1.25F, -1.25F, 5.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.375F, 0.025F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(SeepingSoulEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        float yaw = entity.getYRot() * ((float)Math.PI / 180F);
        this.Skullsoul.yRot = yaw;

        this.animate(entity.idleAnimation, SeepingSoulChaosSpawnerAnimation.IDLE, ageInTicks);
        this.animate(entity.idleBreakAnimation, SeepingSoulChaosSpawnerAnimation.IDLE_BREAK, ageInTicks);
        this.animate(entity.spawnAnimation, SeepingSoulChaosSpawnerAnimation.SPAWN, ageInTicks);
        this.animate(entity.hurtLeftAnimation, SeepingSoulChaosSpawnerAnimation.HURT_LEFT, ageInTicks);
        this.animate(entity.hurtRightAnimation, SeepingSoulChaosSpawnerAnimation.HURT_RIGHT, ageInTicks);
        this.animate(entity.recallingAnimation, SeepingSoulChaosSpawnerAnimation.RECALLING, ageInTicks);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        Skullsoul.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public ModelPart root() {
        return root;
    }

}
