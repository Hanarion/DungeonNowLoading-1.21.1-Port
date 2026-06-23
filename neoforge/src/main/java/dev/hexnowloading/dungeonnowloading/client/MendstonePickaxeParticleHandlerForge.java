package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.particle.MendstonePickaxeParticleLogic;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class MendstonePickaxeParticleHandlerForge {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MendstonePickaxeParticleLogic.handleClientTick(Minecraft.getInstance());
    }
}

