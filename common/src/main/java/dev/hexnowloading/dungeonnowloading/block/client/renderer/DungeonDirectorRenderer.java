package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.entity.DungeonDirectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class DungeonDirectorRenderer implements BlockEntityRenderer<DungeonDirectorBlockEntity> {

    public DungeonDirectorRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(DungeonDirectorBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // ✅ creative only
        if (!player.getAbilities().instabuild) return;

        BlockState state = be.getBlockState();

        // Get baked model + correct render layer (cutout/solid/translucent)
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        RenderType layer = ItemBlockRenderTypes.getChunkRenderType(state);

        // Render baked model directly (ignores RenderShape.INVISIBLE)
        dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(layer),
                state,
                model,
                1.0F, 1.0F, 1.0F,
                packedLight,
                packedOverlay
        );
    }
}
