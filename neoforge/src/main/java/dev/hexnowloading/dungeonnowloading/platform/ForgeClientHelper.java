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

    // 1.21 NeoForge: MenuScreens.register is private; screens MUST be registered directly inside
    // RegisterMenuScreensEvent. Previously this was buffered in onClientSetup/enqueueWork and flushed
    // in onRegisterMenuScreens — but RegisterMenuScreensEvent fires BEFORE onClientSetup, so the
    // buffer was empty at flush time and the screen never registered (menu opened server-side but no
    // client screen appeared). Register directly in the event instead.
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

    /**
     * Register menu screens. Called from RegisterMenuScreensEvent (mod bus, client).
     * Populate the buffer INSIDE this event (before flushing) — RegisterMenuScreensEvent fires
     * earlier in the client lifecycle than onClientSetup/enqueueWork, so buffering from the latter
     * left the buffer empty here and screens never registered.
     */
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        dev.hexnowloading.dungeonnowloading.DNLClient.registerMenuScreens();
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
