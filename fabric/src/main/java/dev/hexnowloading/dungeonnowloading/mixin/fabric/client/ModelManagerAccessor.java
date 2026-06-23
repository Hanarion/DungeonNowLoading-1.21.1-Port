package dev.hexnowloading.dungeonnowloading.mixin.fabric.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelManager.class)
public interface ModelManagerAccessor {

    // 1.21: ModelManager.bakedRegistry is keyed by ModelResourceLocation, not ResourceLocation.
    @Accessor("bakedRegistry")
    Map<ModelResourceLocation, BakedModel> getBakedRegistry();
}
