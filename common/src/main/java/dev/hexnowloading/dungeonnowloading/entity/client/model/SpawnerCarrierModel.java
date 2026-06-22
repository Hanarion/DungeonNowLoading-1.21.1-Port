package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.SpawnerCarrierAnimation;
import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SpawnerCarrierModel<T extends SpawnerCarrierEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "spawner_carrier"), "main");
    private final ModelPart root;
    private final ModelPart all;
    private final ModelPart Right_frontleg;
    private final ModelPart Right_backleg;
    private final ModelPart Left_backleg;
    private final ModelPart Left_frontleg;
    private final ModelPart body;
    private final ModelPart Spawner;
    private final ModelPart eye;

    public SpawnerCarrierModel(ModelPart root) {
        this.root = root;
        this.all = root.getChild("all");
        this.Right_frontleg = this.all.getChild("Right_frontleg");
        this.Right_backleg = this.all.getChild("Right_backleg");
        this.Left_backleg = this.all.getChild("Left_backleg");
        this.Left_frontleg = this.all.getChild("Left_frontleg");
        this.body = this.all.getChild("body");
        this.Spawner = this.body.getChild("Spawner");
        this.eye = this.body.getChild("eye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(12.5F, 9.0F, -12.5F));

        PartDefinition Right_frontleg = all.addOrReplaceChild("Right_frontleg", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Right_backleg = all.addOrReplaceChild("Right_backleg", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 25.0F));

        PartDefinition cube_r1 = Right_backleg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(81, 29).mirror().addBox(-3.5F, -9.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

        PartDefinition Left_backleg = all.addOrReplaceChild("Left_backleg", CubeListBuilder.create(), PartPose.offset(-25.0F, 0.0F, 25.0F));

        PartDefinition cube_r2 = Left_backleg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(81, 29).addBox(-3.5F, -9.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition Left_frontleg = all.addOrReplaceChild("Left_frontleg", CubeListBuilder.create().texOffs(81, 29).mirror().addBox(-3.5F, -3.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-25.0F, 0.0F, 0.0F));

        PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(56, 98).addBox(-9.0F, -8.5F, -8.5F, 18.0F, 12.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-10.0F, -8.625F, -9.5F, 20.0F, 3.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-11.0F, -6.625F, -10.5F, 22.0F, 14.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.5F, -0.375F, 12.0F));

        PartDefinition Spawner = body.addOrReplaceChild("Spawner", CubeListBuilder.create().texOffs(0, 59).addBox(-8.0F, -4.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.625F, 0.5F));

        PartDefinition eye = body.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(24, 105).addBox(-6.5F, -4.5F, -0.5F, 13.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.375F, -9.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(SpawnerCarrierEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.Spawner.visible = !entity.isSpawnerBroken();
        entity.idleAnimationState.startIfStopped(entity.tickCount);

        float raw = limbSwingAmount * 2.5F;
        float target = raw < 0.08F ? 0.0F : Mth.clamp(raw, 0.0F, 1.0F);

        float fadeIn  = 0.12F;
        float fadeOut = 0.06F;

        float rate = (target > entity.clientWalkBlend) ? fadeIn : fadeOut;
        entity.clientWalkBlend += (target - entity.clientWalkBlend) * rate;

        float fakeLimbSwingAmount = entity.clientWalkBlend / 2.5F;
        this.animateWalk(SpawnerCarrierAnimation.WALK, limbSwing, fakeLimbSwingAmount, 2.0F, 2.5F);

        // Animations

        this.animate(entity.idleAnimationState, SpawnerCarrierAnimation.IDLE, ageInTicks);
        this.animate(entity.groundSmashAnimationState, SpawnerCarrierAnimation.GROUND_SMASH, ageInTicks);
        this.animate(entity.summonAnimationState, SpawnerCarrierAnimation.SUMMON, ageInTicks);
        this.animate(entity.summonEndAnimationState, SpawnerCarrierAnimation.SUMMON_END, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        all.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public ModelPart getSpawnerPart() {
        return this.Spawner;
    }

    public ModelPart getAllPart() {
        return this.all;
    }

    public ModelPart getBodyPart() {
        return this.body;
    }
}
