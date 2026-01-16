package dev.hexnowloading.dungeonnowloading.gauntlet;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record GauntletDefinition(String id,
                                 int activationRange,
                                 int relX, int relY, int relZ,
                                 int sizeX, int sizeY, int sizeZ,
                                 List<ResourceLocation> waves) {
}
