package dev.hexnowloading.dungeonnowloading.network;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ClientUtil {
    public static boolean onClient() {
        return Services.PLATFORM.getEnvironmentSide() == EnvironmentSide.CLIENT;
    }

    @Nullable
    public static Minecraft getClient() {
        return onClient() ? Minecraft.getInstance() : null;
    }

    @Nullable
    public static Player getClientPlayer() {
        return onClient() ? getClient().player : null;
    }

    @Nullable
    public static Level getClientLevel() {
        return onClient() ? getClient().level : null;
    }
}
