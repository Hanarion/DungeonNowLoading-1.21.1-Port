//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;

//public class ChaosSpawnerLayer<T extends ChaosSpawnerEntity, M extends ChaosSpawnerModel<T>> extends RenderLayer<T, M> {
public class CopperCreepPowerLayer<T extends CopperCreepEntity, M extends CopperCreepModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep_armor.png");
    private final CopperCreepModel<CopperCreepEntity> model;

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

    public void render(PoseStack $$0, MultiBufferSource $$1, int $$2, CopperCreepEntity $$3, float $$4, float $$5, float $$6, float $$7, float $$8, float $$9) {
        if (((PowerableMob)$$3).isPowered()) {
            float $$10 = (float)$$3.tickCount + $$6;
            EntityModel<CopperCreepEntity> $$11 = this.model();
            $$11.prepareMobModel($$3, $$4, $$5, $$6);
            this.getParentModel().copyPropertiesTo((EntityModel<T>) $$11);
            VertexConsumer $$12 = $$1.getBuffer(RenderType.energySwirl(POWER_LOCATION, this.xOffset($$10) % 1.0F, $$10 * 0.01F % 1.0F));
            $$11.setupAnim($$3, $$4, $$5, $$7, $$8, $$9);
            $$11.renderToBuffer($$0, $$12, $$2, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
        }
    }

    protected EntityModel<CopperCreepEntity> model() { return this.model; }


    protected float xOffset(float $$0) {
        return $$0 * 0.01F;
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