package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface ReloadListenerPlatform {
    void registerDataReloadListener(ResourceLocation id, PreparableReloadListener listener);
}