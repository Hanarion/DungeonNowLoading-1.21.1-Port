package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.particle.MendstonePickaxeParticleLogic;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class MendstonePickaxeParticleHandlerFabric {
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(MendstonePickaxeParticleLogic::handleClientTick);
    }
}

