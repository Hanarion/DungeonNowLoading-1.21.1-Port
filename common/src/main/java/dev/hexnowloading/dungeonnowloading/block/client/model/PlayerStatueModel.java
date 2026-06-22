package dev.hexnowloading.dungeonnowloading.block.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class PlayerStatueModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "player_statue"), "main");
    private final ModelPart Body;
    private final ModelPart Head;
    private final ModelPart RightArmThick;
    private final ModelPart LeftArmThick;
    private final ModelPart RightArmThin;
    private final ModelPart LeftArmThin;
    private final ModelPart RightLeg;
    private final ModelPart LeftLeg;

    public PlayerStatueModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.Body = root.getChild("Body");
        this.Head = this.Body.getChild("Head");
        this.RightArmThick = this.Body.getChild("RightArmThick");
        this.LeftArmThick = this.Body.getChild("LeftArmThick");
        this.RightArmThin = this.Body.getChild("RightArmThin");
        this.LeftArmThin = this.Body.getChild("LeftArmThin");
        this.RightLeg = this.Body.getChild("RightLeg");
        this.LeftLeg = this.Body.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -22.3636F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(16, 32).addBox(-4.0F, -22.3636F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 18.3636F, 0.0F));

        PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -22.3636F, 0.0F, -0.1047F, 0.0873F, 0.0F));

        PartDefinition RightArm = Body.addOrReplaceChild("RightArmThick", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.0F, -20.3636F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition LeftArmThick = Body.addOrReplaceChild("LeftArmThick", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(5.0F, -20.3636F, 0.0F, 0.2094F, 0.0F, 0.0F));

        PartDefinition RightArmThin = Body.addOrReplaceChild("RightArmThin", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.0F, -20.3636F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition LeftArmThin = Body.addOrReplaceChild("LeftArmThin", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
                .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -20.3636F, 0.0F, 0.2094F, 0.0F, 0.0F));

        PartDefinition RightLeg = Body.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.9F, -10.3636F, 0.0F, 0.192F, 0.0F, 0.0349F));

        PartDefinition LeftLeg = Body.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(1.9F, -10.3636F, 0.0F, -0.1745F, 0.0F, -0.0349F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void useSlimArms(boolean slim) {
        RightArmThin.visible = slim;
        LeftArmThin.visible  = slim;
        RightArmThick.visible = !slim;
        LeftArmThick.visible  = !slim;
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, int color) {
        Body.render(ps, vc, light, overlay, color);
    }
}
