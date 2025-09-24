package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.entity.GauntletBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GauntletItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static GauntletBlockEntity blockEntity;

    public GauntletItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack ps,
                             MultiBufferSource mb, int light, int overlay) {
        if (blockEntity == null) {
            blockEntity = new GauntletBlockEntity(BlockPos.ZERO, DNLBlocks.GAUNTLET.get().defaultBlockState());
        }

        // Handheld/GUI default look: 3 total, 0 lit, inactive
        blockEntity.setWaves(3, 0, false);

        ps.pushPose();

        // Do NOT reuse the world/BER transforms here.
        // Apply small, context-specific transforms only:
        /*switch (ctx) {
            case GUI -> {
                ps.translate(0.0, 0.0, 0.0);
                ps.scale(0.9f, 0.9f, 0.9f);
            }
            case GROUND -> {
                ps.translate(0.0, 0.0625, 0.0);
                ps.scale(0.6f, 0.6f, 0.6f);
            }
            case FIXED -> {
                ps.scale(0.8f, 0.8f, 0.8f);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                ps.scale(0.75f, 0.75f, 0.75f);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                ps.translate(0.0, 0.15, 0.0);
                ps.scale(0.7f, 0.7f, 0.7f);
            }
            default -> {}
        }*/

        // Important: no extra 0.5/1.5 translate or 180° X flip here.
        // The item pipeline already supplies the correct basis.
        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockEntity, ps, mb, light, overlay);
        ps.popPose();
    }


    public static GauntletItemRenderer getInstance() {
        return new GauntletItemRenderer();
    }
}
