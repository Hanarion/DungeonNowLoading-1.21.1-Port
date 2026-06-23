package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.particle.MendstonePickaxeParticleLogic;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class MendstonePickaxeParticleHandlerForge {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        MendstonePickaxeParticleLogic.handleClientTick(Minecraft.getInstance());
    }
}

