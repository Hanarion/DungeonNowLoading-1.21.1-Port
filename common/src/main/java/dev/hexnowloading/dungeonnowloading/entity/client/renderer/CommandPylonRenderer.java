package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.BallistaGolemModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CommandPylonModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SpawnerCarrierModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.CommandPylonEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.BallistaGolemEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BallistaArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CommandPylonRenderer<T extends CommandPylonEntity> extends MobRenderer<T, CommandPylonModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/command_pylon.png");

    public CommandPylonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CommandPylonModel<>(renderManager.bakeLayer(CommandPylonModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public void render(CommandPylonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.canRender()) {
            super.render((T) entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CommandPylonEntity entity) {
        return TEXTURE;
    }
}
