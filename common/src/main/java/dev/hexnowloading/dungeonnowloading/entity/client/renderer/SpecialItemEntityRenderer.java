package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class SpecialItemEntityRenderer extends ItemEntityRenderer {

    private final ItemRenderer itemRenderer;

    public SpecialItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }
}
