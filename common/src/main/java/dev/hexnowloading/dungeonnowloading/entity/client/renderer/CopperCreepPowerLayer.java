//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PowerableMob;

//public class ChaosSpawnerLayer<T extends ChaosSpawnerEntity, M extends ChaosSpawnerModel<T>> extends RenderLayer<T, M> {
public class CopperCreepPowerLayer<T extends CopperCreepEntity, M extends HierarchicalModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation POWER_LOCATION = ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep/copper_creep_armor.png");
    private final CopperCreepModel<CopperCreepEntity> model;
    private final float OFFSET_Y_BY_PIXEL = 0.75F;

    public CopperCreepPowerLayer(CopperCreepRenderer renderer, CopperCreepModel<CopperCreepEntity> model) {
        super((RenderLayerParent<T, M>) renderer);
        this.model = model;
    }
//    public CopperCreepPowerLayer(RenderLayerParent<CopperCreepEntity, CopperCreepModel<CopperCreepEntity>> renderer, EntityModelSet modelSet) {
//        super(renderer);
//        this.model = new CopperCreepModel<>(modelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
//    }
//
//    @Override
//    protected float xOffset(float partialTick) {
//        return partialTick * 0.01F;
//    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CopperCreepEntity copperCreepEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (((PowerableMob)copperCreepEntity).isPowered()) {
            float tickCount = (float)copperCreepEntity.tickCount + partialTicks;

            poseStack.pushPose();
            poseStack.scale(1.05F, 1.05F, 1.05F);
            poseStack.translate(0, -(OFFSET_Y_BY_PIXEL / 16), 0);
            EntityModel<CopperCreepEntity> entityModel = this.model();
            entityModel.prepareMobModel(copperCreepEntity, limbSwing, limbSwingAmount, partialTicks);
            this.getParentModel().copyPropertiesTo((EntityModel<T>) entityModel);
            VertexConsumer $$12 = bufferSource.getBuffer(RenderType.energySwirl(POWER_LOCATION, this.xOffset(tickCount) % 1.0F, tickCount * 0.01F % 1.0F));
            entityModel.setupAnim(copperCreepEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            entityModel.renderToBuffer(poseStack, $$12, packedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
            poseStack.popPose();
        }
    }

    protected EntityModel<CopperCreepEntity> model() { return this.model; }


    protected float xOffset(float $$0) {
        return $$0 * 0.00F;
    }


//    @Override
//    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CopperCreepEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//        if (entitylivingbaseIn.isPowered()) {
////            super.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
//            VertexConsumer shockwaveVertexConsumer = bufferIn.getBuffer(RenderType.entityTranslucentEmissive(POWER_LOCATION, true));
//            this.getParentModel().renderToBuffer(matrixStackIn, shockwaveVertexConsumer, packedLightIn, LivingEntityRenderer.getOverlayCoords(entitylivingbaseIn, 0), 1.0F, 1.0F, 1.0F, 1.0F);
//        }
//    }
}