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

public class GauntletVaultModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "gauntlet_vault"), "main");
    private final ModelPart gauntlet;
    private final ModelPart base;
    private final ModelPart lid;
    private final ModelPart lid_right;
    private final ModelPart lid_left;

    public GauntletVaultModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.gauntlet = root.getChild("gauntlet");
        this.base = this.gauntlet.getChild("base");
        this.lid = this.base.getChild("lid");
        this.lid_right = this.lid.getChild("lid_right");
        this.lid_left = this.lid.getChild("lid_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition gauntlet = partdefinition.addOrReplaceChild("gauntlet", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition base = gauntlet.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -2.0F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(56, 0).addBox(-7.0F, -4.0F, -2.0F, 14.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 16).addBox(-2.0F, -4.0F, -7.0F, 4.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 16).addBox(-5.0F, -13.0F, -5.0F, 10.0F, 11.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lid = base.addOrReplaceChild("lid", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition lid_right = lid.addOrReplaceChild("lid_right", CubeListBuilder.create().texOffs(36, 37).addBox(-6.0F, -5.0F, -6.0F, 6.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(36, 54).addBox(-6.0F, 0.0F, -6.0F, 6.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lid_left = lid.addOrReplaceChild("lid_left", CubeListBuilder.create().texOffs(0, 37).addBox(0.0F, -5.0F, -6.0F, 6.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(0.0F, 0.0F, -6.0F, 6.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        gauntlet.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
