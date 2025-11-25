package dev.hexnowloading.dungeonnowloading.entity.client.model;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.monster.ReaperSpiderEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ReaperSpiderModel <T extends ReaperSpiderEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "reaper_spider"), "main");

    private static final String BODY_0 = "body0";
    private static final String BODY_1 = "body1";
    private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
    private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
    private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
    private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightMiddleHindLeg;
    private final ModelPart leftMiddleHindLeg;
    private final ModelPart rightMiddleFrontLeg;
    private final ModelPart leftMiddleFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public ReaperSpiderModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightMiddleHindLeg = modelPart.getChild("right_middle_hind_leg");
        this.leftMiddleHindLeg = modelPart.getChild("left_middle_hind_leg");
        this.rightMiddleFrontLeg = modelPart.getChild("right_middle_front_leg");
        this.leftMiddleFrontLeg = modelPart.getChild("left_middle_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
    }

    public static LayerDefinition createSpiderBodyLayer() {
        MeshDefinition $$0 = new MeshDefinition();
        PartDefinition $$1 = $$0.getRoot();
        $$1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 15.0F, -3.0F));
        $$1.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        $$1.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 9.0F));
        CubeListBuilder $$3 = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        CubeListBuilder $$4 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
        $$1.addOrReplaceChild("right_hind_leg", $$3, PartPose.offset(-4.0F, 15.0F, 2.0F));
        $$1.addOrReplaceChild("left_hind_leg", $$4, PartPose.offset(4.0F, 15.0F, 2.0F));
        $$1.addOrReplaceChild("right_middle_hind_leg", $$3, PartPose.offset(-4.0F, 15.0F, 1.0F));
        $$1.addOrReplaceChild("left_middle_hind_leg", $$4, PartPose.offset(4.0F, 15.0F, 1.0F));
        $$1.addOrReplaceChild("right_middle_front_leg", $$3, PartPose.offset(-4.0F, 15.0F, 0.0F));
        $$1.addOrReplaceChild("left_middle_front_leg", $$4, PartPose.offset(4.0F, 15.0F, 0.0F));
        $$1.addOrReplaceChild("right_front_leg", $$3, PartPose.offset(-4.0F, 15.0F, -1.0F));
        $$1.addOrReplaceChild("left_front_leg", $$4, PartPose.offset(4.0F, 15.0F, -1.0F));
        return LayerDefinition.create($$0, 64, 32);
    }

    public ModelPart root() {
        return this.root;
    }

    public void setupAnim(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5) {
        this.head.yRot = $$4 * 0.017453292F;
        this.head.xRot = $$5 * 0.017453292F;
        float $$6 = 0.7853982F;
        this.rightHindLeg.zRot = -0.7853982F;
        this.leftHindLeg.zRot = 0.7853982F;
        this.rightMiddleHindLeg.zRot = -0.58119464F;
        this.leftMiddleHindLeg.zRot = 0.58119464F;
        this.rightMiddleFrontLeg.zRot = -0.58119464F;
        this.leftMiddleFrontLeg.zRot = 0.58119464F;
        this.rightFrontLeg.zRot = -0.7853982F;
        this.leftFrontLeg.zRot = 0.7853982F;
        float $$7 = -0.0F;
        float $$8 = 0.3926991F;
        this.rightHindLeg.yRot = 0.7853982F;
        this.leftHindLeg.yRot = -0.7853982F;
        this.rightMiddleHindLeg.yRot = 0.3926991F;
        this.leftMiddleHindLeg.yRot = -0.3926991F;
        this.rightMiddleFrontLeg.yRot = -0.3926991F;
        this.leftMiddleFrontLeg.yRot = 0.3926991F;
        this.rightFrontLeg.yRot = -0.7853982F;
        this.leftFrontLeg.yRot = 0.7853982F;
        float $$9 = -(Mth.cos($$1 * 0.6662F * 2.0F + 0.0F) * 0.4F) * $$2;
        float $$10 = -(Mth.cos($$1 * 0.6662F * 2.0F + 3.1415927F) * 0.4F) * $$2;
        float $$11 = -(Mth.cos($$1 * 0.6662F * 2.0F + 1.5707964F) * 0.4F) * $$2;
        float $$12 = -(Mth.cos($$1 * 0.6662F * 2.0F + 4.712389F) * 0.4F) * $$2;
        float $$13 = Math.abs(Mth.sin($$1 * 0.6662F + 0.0F) * 0.4F) * $$2;
        float $$14 = Math.abs(Mth.sin($$1 * 0.6662F + 3.1415927F) * 0.4F) * $$2;
        float $$15 = Math.abs(Mth.sin($$1 * 0.6662F + 1.5707964F) * 0.4F) * $$2;
        float $$16 = Math.abs(Mth.sin($$1 * 0.6662F + 4.712389F) * 0.4F) * $$2;
        ModelPart var10000 = this.rightHindLeg;
        var10000.yRot += $$9;
        var10000 = this.leftHindLeg;
        var10000.yRot += -$$9;
        var10000 = this.rightMiddleHindLeg;
        var10000.yRot += $$10;
        var10000 = this.leftMiddleHindLeg;
        var10000.yRot += -$$10;
        var10000 = this.rightMiddleFrontLeg;
        var10000.yRot += $$11;
        var10000 = this.leftMiddleFrontLeg;
        var10000.yRot += -$$11;
        var10000 = this.rightFrontLeg;
        var10000.yRot += $$12;
        var10000 = this.leftFrontLeg;
        var10000.yRot += -$$12;
        var10000 = this.rightHindLeg;
        var10000.zRot += $$13;
        var10000 = this.leftHindLeg;
        var10000.zRot += -$$13;
        var10000 = this.rightMiddleHindLeg;
        var10000.zRot += $$14;
        var10000 = this.leftMiddleHindLeg;
        var10000.zRot += -$$14;
        var10000 = this.rightMiddleFrontLeg;
        var10000.zRot += $$15;
        var10000 = this.leftMiddleFrontLeg;
        var10000.zRot += -$$15;
        var10000 = this.rightFrontLeg;
        var10000.zRot += $$16;
        var10000 = this.leftFrontLeg;
        var10000.zRot += -$$16;
    }
}
