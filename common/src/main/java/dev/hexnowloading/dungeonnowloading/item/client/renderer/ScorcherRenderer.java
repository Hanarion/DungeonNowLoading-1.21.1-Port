package dev.hexnowloading.dungeonnowloading.item.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ScorcherRenderer extends BlockEntityWithoutLevelRenderer {

    private ScorcherModel model;

    public ScorcherRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        if (this.model == null) {
            this.model = new ScorcherModel(Minecraft.getInstance().getEntityModels().bakeLayer(ScorcherModel.LAYER_LOCATION));
        }

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.translate(0, 0, 1.5 / 16);
        poseStack.scale(-1.0F, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(ScorcherModel.TEXTURE));
        if (itemStack.getItem() instanceof ScorcherItem scorcherItem) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            this.model.setUpAnim(scorcherItem, player, itemStack, getPartialTick());

        }
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
    }

    private float getPartialTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return (minecraft.level != null) ? minecraft.getFrameTime() : 0;
    }

    public static ScorcherRenderer getInstance() {
        return new ScorcherRenderer();
    }
}
