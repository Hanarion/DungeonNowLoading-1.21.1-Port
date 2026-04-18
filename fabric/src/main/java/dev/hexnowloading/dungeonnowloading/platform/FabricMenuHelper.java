package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.platform.services.MenuHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class FabricMenuHelper implements MenuHelper {
    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        return Services.REGISTRY.register(
                BuiltInRegistries.MENU,
                name,
                () -> new MenuType<>(factory::create, FeatureFlags.VANILLA_SET)
        );
    }
}
