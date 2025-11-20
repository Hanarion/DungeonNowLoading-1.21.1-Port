package dev.hexnowloading.dungeonnowloading.entity.client.model;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.misc.WaywardLanternMinecartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class WaywardLanternMinecartModel<T extends WaywardLanternMinecartEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "wayward_lantern_minecart"), "main");
    private final ModelPart root;

    public WaywardLanternMinecartModel(ModelPart modelPart) {
        this.root = modelPart;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        int i = 20;
        int j = 8;
        int k = 16;
        int l = 4;
        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 10).addBox(-10.0f, -8.0f, -1.0f, 20.0f, 16.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, 0.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(-9.0f, 4.0f, 0.0f, 0.0f, 4.712389f, 0.0f));
        partDefinition.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(9.0f, 4.0f, 0.0f, 0.0f, 1.5707964f, 0.0f));
        partDefinition.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -7.0f, 0.0f, (float)Math.PI, 0.0f));
        partDefinition.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -9.0f, -1.0f, 16.0f, 8.0f, 2.0f), PartPose.offset(0.0f, 4.0f, 7.0f));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
