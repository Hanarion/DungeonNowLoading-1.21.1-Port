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

public class DungeonBannerBlockModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "dungeon_banner"), "main");
    private final ModelPart BANNER;
    private final ModelPart Flag;
    private final ModelPart Stick;

    public DungeonBannerBlockModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.BANNER = root.getChild("BANNER");
        this.Flag = this.BANNER.getChild("Flag");
        this.Stick = this.BANNER.getChild("Stick");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition BANNER = partdefinition.addOrReplaceChild("BANNER", CubeListBuilder.create(), PartPose.offset(0.0F, -16.0F, 7.0F));

        PartDefinition Flag = BANNER.addOrReplaceChild("Flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, 0.0F, 20.0F, 40.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Stick = BANNER.addOrReplaceChild("Stick", CubeListBuilder.create().texOffs(0, 40).addBox(-10.0F, -40.0F, 7.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 38.0F, -7.9F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        BANNER.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public void setWave(float xRotRad) {
        this.Flag.xRot = xRotRad;
        // optional: match vanilla "hang point" feel (tweak if needed)
        // this.Flag.y = -32.0F;
    }
}
