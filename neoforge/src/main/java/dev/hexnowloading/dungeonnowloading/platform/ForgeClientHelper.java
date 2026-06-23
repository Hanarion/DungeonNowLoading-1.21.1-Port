package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.ClientHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.ArrayList;
import java.util.List;

public class ForgeClientHelper implements ClientHelper {
    public static final List<ResourceLocation> ITEM_MODELS = new ArrayList<>();

    // 1.21 NeoForge: MenuScreens.register is private; screens must be registered during
    // RegisterMenuScreensEvent. Buffer the registrations and flush them there.
    private record ScreenEntry<M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>>(
            MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> constructor) {}

    private static final List<ScreenEntry<?, ?>> SCREENS = new ArrayList<>();

    @Override
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerMenuScreen(
            MenuType<? extends M> menuType,
            MenuScreenFactory<M, U> factory
    ) {
        SCREENS.add(new ScreenEntry<>(menuType, factory::create));
    }

    /** Flush buffered menu-screen registrations; call from RegisterMenuScreensEvent (mod bus, client). */
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        for (ScreenEntry<?, ?> entry : SCREENS) {
            registerEntry(event, entry);
        }
    }

    private static <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void registerEntry(
            RegisterMenuScreensEvent event, ScreenEntry<M, U> entry) {
        event.register(entry.menuType(), entry.constructor());
    }

    @Override
    public void registerItemModel(ResourceLocation modelLocation) {
        ITEM_MODELS.add(modelLocation);
    }
}
