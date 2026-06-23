package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.platform.services.MenuHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import java.util.function.Supplier;

public class ForgeMenuHelper implements MenuHelper {
    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuFactory<T> factory) {
        return Services.REGISTRY.register(
                BuiltInRegistries.MENU,
                name,
                () -> IMenuTypeExtension.create((containerId, inventory, buffer) -> factory.create(containerId, inventory))
        );
    }
}
