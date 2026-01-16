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
import net.minecraft.world.entity.Entity;

public class GauntletModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "gauntlet"), "main");
    public final ModelPart gauntlet;
    public final ModelPart brazier;
    public final ModelPart fingers;
    public final ModelPart finger_m;
    public final ModelPart finger_fl;
    public final ModelPart finger_fr;
    public final ModelPart finger_bl;
    public final ModelPart finger_br;
    public final ModelPart pit;

    public GauntletModel(ModelPart root) {
        super(RenderType::armorCutoutNoCull);
        this.gauntlet = root.getChild("gauntlet");
        this.fingers = this.gauntlet.getChild("fingers");
        this.finger_m = this.fingers.getChild("finger_m");
        this.finger_fl = this.fingers.getChild("finger_fl");
        this.finger_fr = this.fingers.getChild("finger_fr");
        this.finger_bl = this.fingers.getChild("finger_bl");
        this.finger_br = this.fingers.getChild("finger_br");
        this.brazier = this.gauntlet.getChild("brazier");
        this.pit = this.brazier.getChild("pit");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition gauntlet = partdefinition.addOrReplaceChild("gauntlet", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition fingers = gauntlet.addOrReplaceChild("fingers", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition finger_m = fingers.addOrReplaceChild("finger_m", CubeListBuilder.create(), PartPose.offsetAndRotation(-7.0F, -22.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r1 = finger_m.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(12, 56).addBox(0.0F, -3.0F, -2.0F, 0.0F, 20.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 1.0F, -7.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition finger_fl = fingers.addOrReplaceChild("finger_fl", CubeListBuilder.create(), PartPose.offset(16.0F, 0.0F, 0.0F));

        PartDefinition cube_r2 = finger_fl.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, 56).addBox(0.0F, -19.0F, -2.0F, 0.0F, 20.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -3.0F, -5.0F, 0.0F, 1.9635F, 0.0F));

        PartDefinition finger_fr = fingers.addOrReplaceChild("finger_fr", CubeListBuilder.create(), PartPose.offset(-8.3827F, -28.0F, 0.9239F));

        PartDefinition cube_r3 = finger_fr.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(48, 41).addBox(0.0F, -19.0F, -2.0F, 0.0F, 20.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3827F, 25.0F, -5.9239F, 0.0F, -1.9635F, 0.0F));

        PartDefinition finger_bl = fingers.addOrReplaceChild("finger_bl", CubeListBuilder.create(), PartPose.offset(16.0F, 0.0F, 0.0F));

        PartDefinition cube_r4 = finger_bl.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 56).addBox(0.0F, -19.0F, -2.0F, 0.0F, 20.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, -4.0F, 5.0F, 0.0F, 1.1781F, 0.0F));

        PartDefinition finger_br = fingers.addOrReplaceChild("finger_br", CubeListBuilder.create(), PartPose.offset(16.0F, 0.0F, 0.0F));

        PartDefinition cube_r5 = finger_br.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(56, 0).addBox(0.0F, -19.0F, -2.0F, 0.0F, 20.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, -4.0F, 5.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition brazier = gauntlet.addOrReplaceChild("brazier", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -13.0F, -7.0F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 41).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition pit = brazier.addOrReplaceChild("pit", CubeListBuilder.create().texOffs(0, 27).addBox(-7.0F, 21.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -21.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

    }
}
