package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.network.packets.C2SSignalRailInputPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class SignalRailInputHandler {
    private static boolean previousLeft;
    private static boolean previousRight;
    private static boolean lastPressedLeft = true;

    private SignalRailInputHandler() {
    }

    public static void handleClientTick(Minecraft minecraft) {
        boolean left = minecraft.options.keyLeft.isDown();
        boolean right = minecraft.options.keyRight.isDown();

        if (left && !previousLeft) {
            lastPressedLeft = true;
        }
        if (right && !previousRight) {
            lastPressedLeft = false;
        }

        if (minecraft.player != null && minecraft.player.getVehicle() instanceof AbstractMinecart) {
            if (left || right) {
                boolean useLeft = left && right ? lastPressedLeft : left;
                Services.NETWORK.sendToServer(new C2SSignalRailInputPacket(useLeft));
            }
        }

        previousLeft = left;
        previousRight = right;
    }
}
