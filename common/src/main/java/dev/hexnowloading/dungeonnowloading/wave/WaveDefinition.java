package dev.hexnowloading.dungeonnowloading.wave;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record WaveDefinition(String id,
                             String name,
                             List<ResourceLocation> mobNodes) {
}
