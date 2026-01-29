package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulRenderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public record SeepingSoulRenderBundle(
        SeepingSoulRenderModel model,
        ResourceLocation baseTexture,
        ResourceLocation eyesTexture
) {
    public RenderType baseRenderType() {
        return RenderType.entityTranslucent(baseTexture);
    }
    public RenderType eyesRenderType() {
        return RenderType.eyes(eyesTexture);
    }
}
