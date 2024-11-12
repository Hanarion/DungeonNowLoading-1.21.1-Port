package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SealedChaosModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
//import net.minecraft.client.renderer.entity.layers.CopperCreepPowerLayer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Blocks;

public class CopperCreepRenderer extends MobRenderer<CopperCreepEntity, CopperCreepModel<CopperCreepEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep.png");

    public CopperCreepRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperCreepModel<>(context.bakeLayer(CopperCreepModel.LAYER_LOCATION)), 0.5F);
        // Match type parameters exactly here
        this.addLayer(new CopperCreepPowerLayer<>(this, getModel()));
    }

    @Override
    protected void scale(CopperCreepEntity entity, PoseStack poseStack, float partialTick) {
        float swelling = entity.getSwelling(partialTick);
        float scaleFactor = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Mth.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float finalScale = (1.0F + swelling * 0.4F) * scaleFactor;
        float inverseScale = (1.0F + swelling * 0.1F) / scaleFactor;
        poseStack.scale(finalScale, inverseScale, finalScale);
    }

    @Override
    public void render(CopperCreepEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isAlreadySummoned()) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    @Override
    protected float getWhiteOverlayProgress(CopperCreepEntity entity, float partialTicks) {
        float swelling = entity.getSwelling(partialTicks);
        return (int)(swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CopperCreepEntity entity) {
        return TEXTURE;
    }
}
