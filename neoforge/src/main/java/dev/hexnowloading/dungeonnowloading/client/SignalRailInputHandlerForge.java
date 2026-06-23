package dev.hexnowloading.dungeonnowloading.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class SignalRailInputHandlerForge {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        SignalRailInputHandler.handleClientTick(Minecraft.getInstance());
    }
}
