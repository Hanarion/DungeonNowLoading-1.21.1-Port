package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface ClientHelper {
    <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(
            MenuType<? extends M> menuType,
            MenuScreenFactory<M, U> factory
    );

    void registerItemModel(ResourceLocation modelLocation);

    @FunctionalInterface
    interface MenuScreenFactory<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> {
        U create(M menu, Inventory inventory, Component title);
    }
}
