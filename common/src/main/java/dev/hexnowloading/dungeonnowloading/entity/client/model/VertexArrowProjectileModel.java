package dev.hexnowloading.dungeonnowloading.entity.client.model;// Made with Blockbench 4.11.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class VertexArrowProjectileModel<T extends VertexArrowProjectileEntity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "vertex_arrow"), "main");
	private final ModelPart bone;
	private final ModelPart root;
	private final ModelPart frame4;
	private final ModelPart frame3;
	private final ModelPart frame2;
	private final ModelPart frame1;

	public VertexArrowProjectileModel(ModelPart root) {
		this.root = root;
		this.bone = root.getChild("bone");
		this.frame4 = this.bone.getChild("frame4");
		this.frame3 = this.bone.getChild("frame3");
		this.frame2 = this.bone.getChild("frame2");
		this.frame1 = this.bone.getChild("frame1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition frame4 = bone.addOrReplaceChild("frame4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = frame4.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r2 = frame4.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition frame3 = bone.addOrReplaceChild("frame3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = frame3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(14, 16).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r4 = frame3.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(14, 0).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition frame2 = bone.addOrReplaceChild("frame2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r5 = frame2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(28, 16).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r6 = frame2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(28, 0).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition frame1 = bone.addOrReplaceChild("frame1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r7 = frame1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(42, 16).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r8 = frame1.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(42, 0).addBox(-3.5F, -16.0F, 0.0F, 7.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}