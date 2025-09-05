package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.particle.MendstonePickaxeParticleLogic;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MendstonePickaxeParticleHandlerForge {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MendstonePickaxeParticleLogic.handleClientTick(Minecraft.getInstance());
    }
}

