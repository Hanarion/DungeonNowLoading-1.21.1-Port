package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletVaultBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GauntletVaultItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static GauntletVaultBlockEntity blockEntity;

    public GauntletVaultItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack ps,
                             MultiBufferSource mb, int light, int overlay) {
        if (blockEntity == null) {
            blockEntity = new GauntletVaultBlockEntity(BlockPos.ZERO, DNLBlocks.GAUNTLET.get().defaultBlockState());
        }

        ps.pushPose();
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, ps, mb, light, overlay);
        ps.popPose();
    }


    public static GauntletVaultItemRenderer getInstance() {
        return new GauntletVaultItemRenderer();
    }
}
