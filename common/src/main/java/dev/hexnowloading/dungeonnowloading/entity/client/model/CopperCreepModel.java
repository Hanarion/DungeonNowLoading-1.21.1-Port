package dev.hexnowloading.dungeonnowloading.entity.client.model;// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CopperCreepAnimation;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class CopperCreepModel<T extends CopperCreepEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "copper_creep"), "main");
	private final ModelPart coppercreep;
	private final ModelPart body;
	private final ModelPart rod;
	private final ModelPart bodycube;
	private final ModelPart legs;
	private final ModelPart left_leg;
	private final ModelPart right_leg;
	private final ModelPart root;
//	private final ModelPart bb_main;

	public CopperCreepModel(ModelPart root) {
		this.coppercreep = root.getChild("coppercreep");
		this.body = this.coppercreep.getChild("body");
		this.rod = this.body.getChild("rod");
		this.bodycube = this.body.getChild("bodycube");
		this.legs = this.coppercreep.getChild("legs");
		this.left_leg = this.legs.getChild("left_leg");
		this.right_leg = this.legs.getChild("right_leg");
		this.root = root;
//		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition coppercreep = partdefinition.addOrReplaceChild("coppercreep", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition body = coppercreep.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, 0.5F));

		PartDefinition rod = body.addOrReplaceChild("rod", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(-2.0F, -6.0F, -1.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.5F, -0.5F));

		PartDefinition bodycube = body.addOrReplaceChild("bodycube", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -13.0F, -4.0F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 8.5F, -0.5F));

		PartDefinition legs = coppercreep.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition left_leg = legs.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 18).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, -4.0F, 0.5F));

		PartDefinition right_leg = legs.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -4.0F, 0.5F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(CopperCreepEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);

		this.animate(entity.idleAnimationState, CopperCreepAnimation.IDLE, ageInTicks);
//		this.animate(copperCreepEntity.walkingAnimationState, CopperCreepAnimation.WALKING, ageInTicks);
//		this.animate(copperCreepEntity.runningAnimationState, CopperCreepAnimation.RUNNING, ageInTicks);
		this.animate(entity.summonAnimationState, CopperCreepAnimation.SUMMON, ageInTicks);
		this.animate(entity.detonationAnimationState, CopperCreepAnimation.DETONATION, ageInTicks);
		this.animate(entity.sitAnimationState, CopperCreepAnimation.SIT, ageInTicks);
		this.animate(entity.standAniamtionState, CopperCreepAnimation.STAND, ageInTicks);
		this.animate(entity.wrongOwnerAnimationState, CopperCreepAnimation.WRONG_OWNER, ageInTicks);
		this.animate(entity.sittingAnimationState, CopperCreepAnimation.SITTING, ageInTicks);

		if (entity.getState() == CopperCreepEntity.State.IDLE || entity.getState() == CopperCreepEntity.State.WALKING_TOWARDS_PLAYER) {
			this.animateWalk(CopperCreepAnimation.WALKING, limbSwing, limbSwingAmount, 4.0f, 4.5f);
		} else if (entity.getState() == CopperCreepEntity.State.FOLLOWING || entity.getState() == CopperCreepEntity.State.RUNNING_TOWARDS_PLAYER) {
			this.animateWalk(CopperCreepAnimation.RUNNING, limbSwing, limbSwingAmount, 2f, 2.25f);
		}
		this.animateHeadLookTarget(netHeadYaw, headPitch);
	}

	private void animateHeadLookTarget(float netHeadYaw, float headPitch) {
		this.coppercreep.yRot = netHeadYaw * ((float)Math.PI / 180F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		coppercreep.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
//		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}