//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;

public class CopperCreepPowerLayer extends EnergySwirlLayer<CopperCreepEntity, CopperCreepModel<CopperCreepEntity>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/copper_creep_armor.png");
    private final CopperCreepModel<CopperCreepEntity> model;

    public CopperCreepPowerLayer(RenderLayerParent<CopperCreepEntity, CopperCreepModel<CopperCreepEntity>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new CopperCreepModel<>(modelSet.bakeLayer(ModelLayers.CREEPER_ARMOR));
    }

    @Override
    protected float xOffset(float partialTick) {
        return partialTick * 0.01F;
    }

    @Override
    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    @Override
    protected EntityModel<CopperCreepEntity> model() {
        return this.model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CopperCreepEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Only render the layer if the condition is met
        if (entity.isPowered()) {
            super.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}