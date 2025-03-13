package dev.hexnowloading.dungeonnowloading.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.mixin.fabric.client.ModelManagerAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DifferentProspectiveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private final ResourceLocation model3d;
    private final ResourceLocation model2d;

    public DifferentProspectiveItemRenderer(ResourceLocation model2d, ResourceLocation model3d){
        this.model2d = model2d;
        this.model3d = model3d;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext transform, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        if (!stack.isEmpty()) {
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            matrixStack.pushPose();
            boolean gui = transform == ItemDisplayContext.GUI || transform == ItemDisplayContext.GROUND || transform == ItemDisplayContext.FIXED;

            BakedModel model;
            if (gui) {
                model = getModel(itemRenderer.getItemModelShaper().getModelManager(), model2d);
            } else {
                model = getModel(itemRenderer.getItemModelShaper().getModelManager(), model3d);
            }
            RenderType rendertype = ItemBlockRenderTypes.getRenderType(stack, true);
            VertexConsumer vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, stack.hasFoil());
            itemRenderer.renderModelLists(model, stack, light, overlay, matrixStack, vertexconsumer);
            matrixStack.popPose();
        }
    }

    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return ((ModelManagerAccessor) modelManager).getBakedRegistry().getOrDefault(modelLocation, modelManager.getMissingModel());
    }
}
