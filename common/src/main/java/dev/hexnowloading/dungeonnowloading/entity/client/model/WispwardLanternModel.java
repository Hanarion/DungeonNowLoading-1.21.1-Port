package dev.hexnowloading.dungeonnowloading.entity.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispwardLanternCartEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class WispwardLanternModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(DungeonNowLoading.MOD_ID, "wispward_lantern_cart"), "main");
    private static final float INCLINE_CART_Z_ROTATION = (float) Math.toRadians(-45.0D);
    private static final float INCLINE_LANTERN_X_OFFSET = -4.0F;
    private final ModelPart root;
    private final ModelPart wispward_lantern_cart;
    private final ModelPart cart;
    private final ModelPart lantern;
    private final ModelPart light;

    public WispwardLanternModel(ModelPart root) {
        this.root = root;
        this.wispward_lantern_cart = root.getChild("wispward_lantern_cart");
        this.cart = this.wispward_lantern_cart.getChild("cart");
        this.lantern = this.wispward_lantern_cart.getChild("lantern");
        this.light = this.lantern.getChild("light");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wispward_lantern_cart = partdefinition.addOrReplaceChild("wispward_lantern_cart", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cart = wispward_lantern_cart.addOrReplaceChild("cart", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(40, 18).addBox(3.0F, -1.0F, -7.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 24).addBox(3.0F, -1.0F, 5.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 27).addBox(-7.0F, -1.0F, 5.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 21).addBox(-7.0F, -1.0F, -7.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 38).addBox(-3.0F, -6.0F, -2.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lantern = wispward_lantern_cart.addOrReplaceChild("lantern", CubeListBuilder.create().texOffs(24, 38).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 17.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-5.0F, -29.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition light = lantern.addOrReplaceChild("light", CubeListBuilder.create().texOffs(0, 38).addBox(-3.0F, -27.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (entity instanceof WispwardLanternCartEntity lanternCart) {
            float partialTick = ageInTicks - entity.tickCount;
            float incline = lanternCart.getInclineAnimationProgress(partialTick);
            this.cart.zRot += INCLINE_CART_Z_ROTATION * incline;
            this.lantern.x += INCLINE_LANTERN_X_OFFSET * incline;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.wispward_lantern_cart.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void renderBase(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        this.light.visible = false;
        this.wispward_lantern_cart.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        this.light.visible = true;
    }

    public void renderLight(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        this.wispward_lantern_cart.translateAndRotate(poseStack);
        this.lantern.translateAndRotate(poseStack);
        this.light.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
