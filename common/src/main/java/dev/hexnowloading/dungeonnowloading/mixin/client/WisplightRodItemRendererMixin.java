package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class WisplightRodItemRendererMixin {
    private static final ResourceLocation WISPLIGHT_ROD_GUI_MODEL = DungeonNowLoading.id("item/wisplight_rod_gui");
    private static final ResourceLocation WISPLIGHT_ROD_HANDHELD_MODEL = DungeonNowLoading.id("item/wisplight_rod_handheld");

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private BakedModel dungeonnowloading$useWisplightRodContextModel(BakedModel model, ItemStack stack,
                                                                     ItemDisplayContext displayContext, boolean leftHand,
                                                                     com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                                     MultiBufferSource bufferSource, int packedLight,
                                                                     int packedOverlay) {
        if (!stack.is(DNLItems.WISPLIGHT_ROD.get())) {
            return model;
        }

        ModelManager modelManager = Minecraft.getInstance().getModelManager();
        ResourceLocation modelLocation = isGuiLike(displayContext) ? WISPLIGHT_ROD_GUI_MODEL : WISPLIGHT_ROD_HANDHELD_MODEL;
        return ((ModelManagerAccessor) modelManager).getBakedRegistry()
                .getOrDefault(modelLocation, modelManager.getMissingModel());
    }

    private static boolean isGuiLike(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.GUI
                || displayContext == ItemDisplayContext.GROUND
                || displayContext == ItemDisplayContext.FIXED;
    }
}
