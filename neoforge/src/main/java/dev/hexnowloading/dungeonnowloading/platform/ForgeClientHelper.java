package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.ClientHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.ArrayList;
import java.util.List;

public class ForgeClientHelper implements ClientHelper {
    public static final List<ResourceLocation> ITEM_MODELS = new ArrayList<>();

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(
            MenuType<? extends M> menuType,
            MenuScreenFactory<M, U> factory
    ) {
        MenuScreens.register(menuType, factory::create);
    }

    @Override
    public void registerItemModel(ResourceLocation modelLocation) {
        ITEM_MODELS.add(modelLocation);
    }
}
