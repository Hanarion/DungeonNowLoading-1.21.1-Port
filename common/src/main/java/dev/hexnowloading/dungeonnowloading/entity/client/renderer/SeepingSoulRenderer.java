package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulRenderModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulSerpentCallerModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SeepingSoulRenderer extends EntityRenderer<SeepingSoulEntity> {

    private final Map<ResourceLocation, SeepingSoulRenderBundle> bundles = new HashMap<>();
    private final SeepingSoulRenderBundle fallback;

    public SeepingSoulRenderer(EntityRendererProvider.Context context) {
        super(context);

        // Create bundles
        SeepingSoulRenderBundle chaos = makeBundle(
                new SeepingSoulChaosSpawnerModel<>(context.bakeLayer(SeepingSoulChaosSpawnerModel.LAYER_LOCATION)),
                tex("textures/entity/seeping_soul/seeping_soul_chaos_spawner.png"),
                tex("textures/entity/seeping_soul/seeping_soul_chaos_spawner_eyes.png")
        );

        SeepingSoulRenderBundle serpent = makeBundle(
                new SeepingSoulSerpentCallerModel<>(context.bakeLayer(SeepingSoulSerpentCallerModel.LAYER_LOCATION)),
                tex("textures/entity/seeping_soul/seeping_soul_fairkeeper_serpent_caller.png"),
                tex("textures/entity/seeping_soul/seeping_soul_fairkeeper_serpent_caller_eyes.png")
        );

        // Map bossId -> bundle
        put("chaos_spawner", chaos);
        put("fairkeeper_serpent_caller", serpent);

        // fallback
        this.fallback = chaos;
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, path);
    }

    private static SeepingSoulRenderBundle makeBundle(SeepingSoulRenderModel model, ResourceLocation base, ResourceLocation eyes) {
        return new SeepingSoulRenderBundle(model, base, eyes);
    }

    private void put(String bossIdPath, SeepingSoulRenderBundle bundle) {
        this.bundles.put(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, bossIdPath), bundle);
    }

    @Override
    public void render(SeepingSoulEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        if (entity.isNoAnimation()) return;

        SeepingSoulRenderBundle bundle = bundles.getOrDefault(entity.getBossId(), fallback);
        SeepingSoulRenderModel model = bundle.model();

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(-1.0f, -1.0F, 1.0F);
        poseStack.translate(0.0f, -1.5f, 0.0f);

        boolean bl = entity.getHurtTicks() > 0;

        float alpha = 0.5F * entity.getHp() / entity.getMaxHp() + 0.1F;
        int emissiveLight = 0xF000F0;

        // Base
        VertexConsumer baseVc = buffer.getBuffer(bundle.baseRenderType());
        model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, entityYaw, 0);
        model.renderToBuffer(
                poseStack,
                baseVc,
                emissiveLight,
                OverlayTexture.pack(0.0f, bl),
                1.0F, 1.0F, 1.0F,
                alpha
        );

        // Eyes
        VertexConsumer eyesVc = buffer.getBuffer(bundle.eyesRenderType());
        model.renderToBuffer(
                poseStack,
                eyesVc,
                emissiveLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F,
                1.0F
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SeepingSoulEntity entity) {
        // Not used by your manual render pipeline, but must return something non-null.
        // Returning the fallback base texture is a safe choice.
        return fallback.baseTexture();
    }
}
