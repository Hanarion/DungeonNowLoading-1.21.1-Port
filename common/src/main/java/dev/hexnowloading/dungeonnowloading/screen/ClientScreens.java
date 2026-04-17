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
}
