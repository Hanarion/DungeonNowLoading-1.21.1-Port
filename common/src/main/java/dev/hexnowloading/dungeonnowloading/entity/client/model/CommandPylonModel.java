package dev.hexnowloading.dungeonnowloading.entity.client.model;// Made with Blockbench 4.11.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.animation.CommandPylonAnimation;
import dev.hexnowloading.dungeonnowloading.entity.misc.CommandPylonEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CommandPylonModel<T extends CommandPylonEntity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "command_pylon"), "main");
	private final ModelPart root;
	private final ModelPart base;
	private final ModelPart gear1;
	private final ModelPart gear2;
	private final ModelPart coil;
	private final ModelPart antenna;
	private final ModelPart legs;
	private final ModelPart leg_nw;
	private final ModelPart leg_ne;
	private final ModelPart leg_sw;
	private final ModelPart leg_se;
	private float antennaRotation;
	private float gearRotation;

	public CommandPylonModel(ModelPart root) {
		this.root = root.getChild("root");
		this.base = this.root.getChild("base");
		this.gear1 = this.base.getChild("gear1");
		this.gear2 = this.base.getChild("gear2");
		this.coil = this.base.getChild("coil");
		this.antenna = this.base.getChild("antenna");
		this.legs = this.root.getChild("legs");
		this.leg_nw = this.legs.getChild("leg_nw");
		this.leg_ne = this.legs.getChild("leg_ne");
		this.leg_sw = this.legs.getChild("leg_sw");
		this.leg_se = this.legs.getChild("leg_se");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 106).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(41, 114).mirror().addBox(-2.0F, -5.0F, -7.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(40, 10).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = base.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(41, 114).addBox(-2.0F, -2.5F, -1.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -2.5F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r2 = base.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(41, 114).addBox(-2.0F, -2.5F, -0.5F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 6.5F, 0.0F, 3.1416F, 0.0F));

		PartDefinition gear1 = base.addOrReplaceChild("gear1", CubeListBuilder.create().texOffs(0, 89).addBox(-3.5F, -3.5F, 0.0F, 7.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -3.0F, -1.0F));

		PartDefinition gear2 = base.addOrReplaceChild("gear2", CubeListBuilder.create().texOffs(0, 89).addBox(-3.5F, -3.5F, 0.0F, 7.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -3.0F, 1.0F));

		PartDefinition coil = base.addOrReplaceChild("coil", CubeListBuilder.create(), PartPose.offset(6.5F, -2.5F, 0.0F));

		PartDefinition cube_r3 = coil.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(33, 84).addBox(-2.0F, -3.5F, -0.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition antenna = base.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -5.5F, -8.0F, 0.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.5F, 0.0F));

		PartDefinition cube_r4 = antenna.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, -1).addBox(0.0F, -5.5F, -8.0F, 0.0F, 11.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(56, 0).addBox(-4.1F, -2.4656F, -5.7F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.9F, -2.5344F, 0.7F));

		PartDefinition leg_nw = legs.addOrReplaceChild("leg_nw", CubeListBuilder.create().texOffs(0, 96).addBox(-7.0F, -4.0F, -1.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(9.9F, 1.5344F, -7.7F));

		PartDefinition leg_ne = legs.addOrReplaceChild("leg_ne", CubeListBuilder.create(), PartPose.offset(9.9F, 1.5344F, -7.7F));

		PartDefinition cube_r5 = leg_ne.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -2.0F, 2.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition leg_sw = legs.addOrReplaceChild("leg_sw", CubeListBuilder.create(), PartPose.offset(9.9F, 1.5344F, -7.7F));

		PartDefinition cube_r6 = leg_sw.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -2.0F, 12.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition leg_se = legs.addOrReplaceChild("leg_se", CubeListBuilder.create(), PartPose.offset(9.9F, 1.5344F, -7.7F));

		PartDefinition cube_r7 = leg_se.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 96).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, -2.0F, 12.0F, 0.0F, 3.1416F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(CommandPylonEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);

		this.animate(entity.setupAnimState, CommandPylonAnimation.SETUP, ageInTicks);
		this.animate(entity.idleAnimState, CommandPylonAnimation.IDLE, ageInTicks);
		this.animate(entity.baseDownAnimState, CommandPylonAnimation.BASE_DOWN, ageInTicks);
		this.animate(entity.baseUpAnimState, CommandPylonAnimation.BASE_UP, ageInTicks);

		float deltaTime = ageInTicks - entity.tickCount;
		float pylonHealthRatio = entity.getShieldHealth() / CommandPylonEntity.SHIELD_MAX_HEALTH;
		float setupTimeRatio = Mth.clamp(entity.getAge() / CommandPylonEntity.SETUP_DURATION_TICKS, 0.0f, 1.0f);

		antennaRotation += entity.getAntennaRotationSpeed() * setupTimeRatio * pylonHealthRatio * deltaTime;
		antenna.yRot = antennaRotation;

		gearRotation -= entity.getGearRotationSpeed() * deltaTime;
		gear1.zRot = gearRotation;
		gear2.zRot = gearRotation;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}