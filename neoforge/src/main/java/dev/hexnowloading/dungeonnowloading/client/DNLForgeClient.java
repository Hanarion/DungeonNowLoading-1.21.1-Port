package dev.hexnowloading.dungeonnowloading.client;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class DNLForgeClient {
    public static void init(FMLClientSetupEvent event) {
        // Packets are already registered by DungeonNowLoading.init() (common); do NOT re-register
        // them here — doing so doubled the buffered payload list and threw "already registered".
        //
        // Menu screens are registered inside RegisterMenuScreensEvent (see
        // ForgeClientHelper.onRegisterMenuScreens, wired in DNLForge). Registering them from here
        // (onClientSetup/enqueueWork) is too late: RegisterMenuScreensEvent fires first, so the
        // buffer was empty at flush time and screens never registered (Mending Table UI did not open).
    }

}
