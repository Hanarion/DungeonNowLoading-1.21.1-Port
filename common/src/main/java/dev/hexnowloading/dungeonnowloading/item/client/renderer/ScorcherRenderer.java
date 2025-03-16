package dev.hexnowloading.dungeonnowloading.item.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.item.ScorcherItem;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ScorcherRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation TEXTURE_EMISSIVE_FLAME = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/item/scorcher/scorcher_emissive_flame.png");
    private static final ResourceLocation TEXTURE_EMISSIVE_SOUL_FLAME = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/item/scorcher/scorcher_emissive_soul_flame.png");
    private static final ResourceLocation TEXTURE_EMISSIVE_HEAT = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/item/scorcher/scorcher_emissive_heat.png");
    private static final RenderType RENDER_TYPE_EMISSIVE_FLAME = RenderType.entityTranslucent(TEXTURE_EMISSIVE_FLAME);
    private static final RenderType RENDER_TYPE_EMISSIVE_SOUL_FLAME = RenderType.entityTranslucent(TEXTURE_EMISSIVE_SOUL_FLAME);
    private static final RenderType RENDER_TYPE_EMISSIVE_HEAT = RenderType.entityTranslucent(TEXTURE_EMISSIVE_HEAT);

    private ScorcherModel model;

    private static long lastProcessedTick = -1;

    private static final String LAST_USE_TAG = "LastScorcherUseTime";
    private static final String HEAT_LEVEL_TAG = "ScorcherHeatLevel";

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

        float flameAlpha = 0.0f;
        float heatAlpha = 0.0f;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(ScorcherModel.TEXTURE));
        if (itemStack.getItem() instanceof ScorcherItem scorcherItem) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            this.model.setUpAnim(scorcherItem, player, itemStack, getPartialTick());
            flameAlpha = getFlameAlpha(player, itemStack);
            int playerHeat = ScorcherItem.getPlayerHeat(player);
            heatAlpha = Math.min((float) playerHeat / 120, 1.0F);
        }
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);

        if (flameAlpha > 0.0F) {
            VertexConsumer emissiveFlame = bufferSource.getBuffer(RENDER_TYPE_EMISSIVE_FLAME);
            if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
                emissiveFlame = bufferSource.getBuffer(RENDER_TYPE_EMISSIVE_SOUL_FLAME);
            }
            this.model.renderToBuffer(poseStack, emissiveFlame, LightTexture.FULL_BRIGHT, packedOverlay, 1.0F, 1.0F, 1.0F, flameAlpha);
        }

        if (heatAlpha > 0.0F) {
            VertexConsumer emissiveHeat = bufferSource.getBuffer(RENDER_TYPE_EMISSIVE_HEAT);
            this.model.renderToBuffer(poseStack, emissiveHeat, LightTexture.FULL_BRIGHT, packedOverlay, 1.0F, 1.0F, 1.0F, heatAlpha);
        }

        poseStack.popPose();
    }

    private float getFlameAlpha(Player player, ItemStack itemStack) {
        long gameTime = player.level().getGameTime();
        float partialTick = getPartialTick();
        if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime)) {
            return Math.min(ItemAnimationState.getProgress(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime, getPartialTick()), 1.0F);
        } else if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime)) {
            return 1.0F - ItemAnimationState.getProgress(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime, getPartialTick());
        } else if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
            return 1.0F;
        } else if (ItemAnimationState.isAnimating(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)) {
            float totalOverheatDuration = 8.0f * 20;
            float fadeStartTime = (8.0f - 1.25f) * 20;

            float progress = ItemAnimationState.getProgress(itemStack, ScorcherItem.ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime, partialTick);
            float totalTicksElapsed = progress * totalOverheatDuration;

            if (totalTicksElapsed < fadeStartTime) {
                return 1.0F;
            }

            float fadeProgress = (totalTicksElapsed - fadeStartTime) / (totalOverheatDuration - fadeStartTime);
            return Math.max(0.0F, 1.0F - fadeProgress);
        }
        return 0;
    }

    private float getPartialTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return (minecraft.level != null) ? minecraft.getFrameTime() : 0;
    }

    public static ScorcherRenderer getInstance() {
        return new ScorcherRenderer();
    }
}
