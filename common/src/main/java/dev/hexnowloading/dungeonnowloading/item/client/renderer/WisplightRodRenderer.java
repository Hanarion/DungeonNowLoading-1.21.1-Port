package dev.hexnowloading.dungeonnowloading.item.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.WisplightRodItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.WisplightRodModel;
import dev.hexnowloading.dungeonnowloading.network.ClientUtil;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class WisplightRodRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE_EMISSIVE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/item/wisplight_rod/wisplight_rod_handheld_emissive.png");
    private static final RenderType RENDER_TYPE_EMISSIVE = RenderType.entityTranslucent(TEXTURE_EMISSIVE);

    private WisplightRodModel model;

    public WisplightRodRenderer() {
        super(ClientUtil.getClient().getBlockEntityRenderDispatcher(), ClientUtil.getClient().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!isHandheld(itemDisplayContext)) {
            return;
        }

        poseStack.pushPose();

        if (this.model == null) {
            this.model = new WisplightRodModel(ClientUtil.getClient().getEntityModels().bakeLayer(WisplightRodModel.LAYER_LOCATION));
        }

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        if (itemStack.getItem() instanceof WisplightRodItem wisplightRodItem) {
            Player player = ClientUtil.getClientPlayer();
            if (player != null) {
                this.model.setupAnim(wisplightRodItem, player, itemStack, getPartialTick());
            }
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(WisplightRodModel.TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        VertexConsumer emissiveConsumer = bufferSource.getBuffer(RENDER_TYPE_EMISSIVE);
        this.model.renderToBuffer(poseStack, emissiveConsumer, LightTexture.FULL_BRIGHT, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static boolean isHandheld(ItemDisplayContext itemDisplayContext) {
        return itemDisplayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || itemDisplayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || itemDisplayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private float getPartialTick() {
        return (ClientUtil.getClientLevel() != null) ? ClientUtil.getClient().getFrameTime() : 0;
    }

    public static WisplightRodRenderer getInstance() {
        return new WisplightRodRenderer();
    }
}
