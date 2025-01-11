package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperBorosEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class FairkeeperBorosModel<T extends FairkeeperBorosEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_boros_head"), "main");
    private final ModelPart boros;
    private final ModelPart head;
    private final ModelPart tongue;
    private final ModelPart eye;
    private final ModelPart root;

    public FairkeeperBorosModel(ModelPart root) {
        this.root = root;
        this.boros = root.getChild("boros");
        this.head = this.boros.getChild("head");
        this.tongue = this.boros.getChild("tongue");
        this.eye = this.boros.getChild("eye");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition boros = partdefinition.addOrReplaceChild("boros", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = boros.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 72).addBox(-21.0F, -12.0F, -21.0F, 42.0F, 12.0F, 42.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-24.0F, -36.0F, -24.0F, 48.0F, 24.0F, 48.0F, new CubeDeformation(0.0F))
                .texOffs(92, 126).addBox(-2.0F, -42.0F, 1.0F, 4.0F, 6.0F, 21.0F, new CubeDeformation(0.0F))
                .texOffs(0, 126).addBox(-2.0F, -59.0F, -20.0F, 4.0F, 17.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tongue = boros.addOrReplaceChild("tongue", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = tongue.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(142, 126).addBox(-3.0F, 0.0F, -16.0F, 6.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, -21.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition eye = boros.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(142, 142).addBox(24.0F, -18.0F, -13.0F, 2.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(92, 153).addBox(-26.0F, -18.0F, -13.0F, 2.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        boros.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float var2, float var3, float var4, float var5, float var6) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
    }
}
