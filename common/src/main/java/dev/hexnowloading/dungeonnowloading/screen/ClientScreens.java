package dev.hexnowloading.dungeonnowloading.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public class ClientScreens {
    private ClientScreens() {}

    public static void openPedestalEditor(BlockPos pos, List<Component> lines, DyeColor color, boolean glowing) {
        Minecraft.getInstance().setScreen(new PedestalEditScreen(pos, lines, color, glowing));
    }

    public static void openGauntletEditor(BlockPos pos,
                                          int wavesTotal, int wavesCurrent, boolean active,
                                          int relX, int relY, int relZ,
                                          int sizeX, int sizeY, int sizeZ,
                                          int activationRange, String lootTable, String gauntletName) {
        Minecraft.getInstance().setScreen(new GauntletScreen(pos, wavesTotal, wavesCurrent, active,
                relX, relY, relZ, sizeX, sizeY, sizeZ, activationRange, lootTable, gauntletName));
    }

    public static void openGauntletWaves() {
        Minecraft.getInstance().setScreen(new GauntletWavesScreen(Component.translatable("screen.dnl.gauntlet_waves")));
    }
}
