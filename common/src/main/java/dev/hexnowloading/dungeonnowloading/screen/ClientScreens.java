package dev.hexnowloading.dungeonnowloading.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public class ClientScreens {
    private ClientScreens() {}

    public static void openPedestalEditor(BlockPos pos, List<Component> lines, DyeColor color, boolean glowing) {
        Minecraft.getInstance().setScreen(new PedestalEditScreen(pos, lines, color, glowing));
    }

    public static void openWispwardChestConfig(BlockPos pos, ResourceLocation lootTable, int requiredLitLanterns) {
        Minecraft.getInstance().setScreen(new WispwardChestConfigScreen(pos, lootTable, requiredLitLanterns));
    }

    public static void openWispwardLanternConfig(BlockPos pos, int timerSeconds) {
        Minecraft.getInstance().setScreen(new WispwardLanternConfigScreen(pos, timerSeconds));
    }

    public static void openWispwardLanternCartConfig(int entityId, int timerSeconds) {
        Minecraft.getInstance().setScreen(new WispwardLanternCartConfigScreen(entityId, timerSeconds));
    }
}
