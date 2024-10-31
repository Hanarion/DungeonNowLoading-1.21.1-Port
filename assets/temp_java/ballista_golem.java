// Made with Blockbench 4.11.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class ballista_golem<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "ballista_golem"), "main");
	private final ModelPart BallistaGolem;
	private final ModelPart Upperbody;
	private final ModelPart basket;
	private final ModelPart arrows;
	private final ModelPart Bl;
	private final ModelPart Bm;
	private final ModelPart Br;
	private final ModelPart Fl;
	private final ModelPart Fm;
	private final ModelPart Fr;
	private final ModelPart cannon;
	private final ModelPart body;
	private final ModelPart Innerbody;
	private final ModelPart frame;
	private final ModelPart legs;
	private final ModelPart right_front_leg;
	private final ModelPart left_front_leg;
	private final ModelPart right_back_leg;
	private final ModelPart left_back_leg;

	public ballista_golem(ModelPart root) {
		this.BallistaGolem = root.getChild("BallistaGolem");
		this.Upperbody = this.BallistaGolem.getChild("Upperbody");
		this.basket = this.Upperbody.getChild("basket");
		this.arrows = this.basket.getChild("arrows");
		this.Bl = this.arrows.getChild("Bl");
		this.Bm = this.arrows.getChild("Bm");
		this.Br = this.arrows.getChild("Br");
		this.Fl = this.arrows.getChild("Fl");
		this.Fm = this.arrows.getChild("Fm");
		this.Fr = this.arrows.getChild("Fr");
		this.cannon = this.Upperbody.getChild("cannon");
		this.body = this.Upperbody.getChild("body");
		this.Innerbody = this.body.getChild("Innerbody");
		this.frame = this.body.getChild("frame");
		this.legs = this.BallistaGolem.getChild("legs");
		this.right_front_leg = this.legs.getChild("right_front_leg");
		this.left_front_leg = this.legs.getChild("left_front_leg");
		this.right_back_leg = this.legs.getChild("right_back_leg");
		this.left_back_leg = this.legs.getChild("left_back_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition BallistaGolem = partdefinition.addOrReplaceChild("BallistaGolem", CubeListBuilder.create(), PartPose.offset(0.0F, 26.0F, 0.0F));

		PartDefinition Upperbody = BallistaGolem.addOrReplaceChild("Upperbody", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 20.0F));

		PartDefinition basket = Upperbody.addOrReplaceChild("basket", CubeListBuilder.create().texOffs(0, 192).addBox(-17.0F, -5.0F, -6.0F, 34.0F, 5.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -49.0F, -31.0F));

		PartDefinition arrows = basket.addOrReplaceChild("arrows", CubeListBuilder.create(), PartPose.offset(-0.5F, 14.5F, 10.0F));

		PartDefinition Bl = arrows.addOrReplaceChild("Bl", CubeListBuilder.create(), PartPose.offset(-0.5208F, 13.5154F, 1.0F));

		PartDefinition cube_r1 = Bl.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(200, 200).addBox(-16.0F, -58.0F, 12.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 4.0F, -6.0F, -0.1772F, -0.1719F, 0.0306F));

		PartDefinition Bm = arrows.addOrReplaceChild("Bm", CubeListBuilder.create(), PartPose.offset(-0.5208F, 14.5154F, 1.0F));

		PartDefinition cube_r2 = Bm.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(200, 200).addBox(-5.0F, -48.0F, 12.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, -11.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition Br = arrows.addOrReplaceChild("Br", CubeListBuilder.create(), PartPose.offset(-0.5208F, 13.5154F, 1.0F));

		PartDefinition cube_r3 = Br.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(200, 200).addBox(7.0F, -58.0F, 9.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -11.0F, -0.2451F, -0.4102F, 0.1726F));

		PartDefinition Fl = arrows.addOrReplaceChild("Fl", CubeListBuilder.create(), PartPose.offset(-0.5208F, 14.5154F, 1.0F));

		PartDefinition cube_r4 = Fl.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(200, 200).addBox(-8.0F, -53.0F, -1.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.3054F, 0.6981F, 0.0F));

		PartDefinition Fm = arrows.addOrReplaceChild("Fm", CubeListBuilder.create(), PartPose.offset(-0.5208F, 15.5154F, 1.0F));

		PartDefinition cube_r5 = Fm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(200, 200).addBox(-16.0F, -48.0F, -10.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.3855F, -0.8607F, 0.2119F));

		PartDefinition Fr = arrows.addOrReplaceChild("Fr", CubeListBuilder.create().texOffs(200, 200).addBox(5.0F, -49.0F, -10.0F, 11.0F, 31.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5208F, 10.5154F, 1.0F));

		PartDefinition cannon = Upperbody.addOrReplaceChild("cannon", CubeListBuilder.create().texOffs(144, 9).addBox(-8.0F, -13.0F, -3.0F, 16.0F, 16.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, -45.0F));

		PartDefinition body = Upperbody.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, -43.0F));

		PartDefinition Innerbody = body.addOrReplaceChild("Innerbody", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -24.0F, -24.0F, 48.0F, 48.0F, 48.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.0F, -24.0F, 23.0F));

		PartDefinition frame = body.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(0, 96).addBox(-24.0F, -64.0F, -24.0F, 48.0F, 48.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 23.0F));

		PartDefinition legs = BallistaGolem.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(6.0F, -2.0F, -6.0F));

		PartDefinition right_front_leg = legs.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(136, 199).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.0F, -15.0F, -7.0F));

		PartDefinition left_front_leg = legs.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(136, 199).addBox(25.0F, -16.0F, -15.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-26.0F, 0.0F, 0.0F));

		PartDefinition right_back_leg = legs.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(136, 199).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.0F, -14.0F, 19.0F));

		PartDefinition left_back_leg = legs.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(136, 199).addBox(-8.0F, -16.4227F, -10.0179F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 0.0F, 21.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		BallistaGolem.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}