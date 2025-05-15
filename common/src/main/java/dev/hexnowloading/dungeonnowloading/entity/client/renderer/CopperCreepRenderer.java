package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepButlerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
//import net.minecraft.client.renderer.entity.layers.CopperCreepPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CopperCreepRenderer extends MobRenderer<CopperCreepEntity, HierarchicalModel<CopperCreepEntity>> {

    //private final CopperCreepModel<CopperCreepEntity> defaultModel;
    private final CopperCreepButlerModel<CopperCreepEntity> butlerModel;

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep/copper_creep.png");
    private static final ResourceLocation TEXTURE_BUTLER = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep/copper_creep_butler.png");

    public CopperCreepRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperCreepModel<>(context.bakeLayer(CopperCreepModel.LAYER_LOCATION)), 0.5F);
        // Match type parameters exactly here
        this.addLayer(new CopperCreepPowerLayer<>(this, (CopperCreepModel<CopperCreepEntity>) getModel()));
        butlerModel = new CopperCreepButlerModel<>(context.bakeLayer(CopperCreepButlerModel.LAYER_LOCATION));
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
        if (entity.getHealth() < entity.getMaxHealth()) {
            this.model = butlerModel;
        }
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
        return entity.getHealth() < entity.getMaxHealth() ? TEXTURE_BUTLER : TEXTURE;
    }
}
