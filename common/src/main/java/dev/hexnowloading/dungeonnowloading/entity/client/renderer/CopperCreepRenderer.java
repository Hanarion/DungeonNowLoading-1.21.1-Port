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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Blocks;

public class CopperCreepRenderer<T extends CopperCreepEntity> extends MobRenderer<T, CopperCreepModel<T>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/copper_creep.png");

    public CopperCreepRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperCreepModel<>(context.bakeLayer(CopperCreepModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    protected void scale(CopperCreepEntity $$0, PoseStack $$1, float $$2) {
        float $$3 = $$0.getSwelling($$2);
        float $$4 = 1.0F + Mth.sin($$3 * 100.0F) * $$3 * 0.01F;
        $$3 = Mth.clamp($$3, 0.0F, 1.0F);
        $$3 *= $$3;
        $$3 *= $$3;
        float $$5 = (1.0F + $$3 * 0.4F) * $$4;
        float $$6 = (1.0F + $$3 * 0.1F) / $$4;
        $$1.scale($$5, $$6, $$5);
    }

    @Override
    public void render(T entity, float $$1, float $$2, PoseStack $$3, MultiBufferSource $$4, int $$5) {
        if (entity.isAlreadySummoned()) {
            super.render(entity, $$1, $$2, $$3, $$4, $$5);
        }
    }

    @Override
    protected float getWhiteOverlayProgress(CopperCreepEntity $$0, float $$1) {
        float $$2 = $$0.getSwelling($$1);
        return (int)($$2 * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp($$2, 0.5F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(T t) {
        return TEXTURE;
    }
}
