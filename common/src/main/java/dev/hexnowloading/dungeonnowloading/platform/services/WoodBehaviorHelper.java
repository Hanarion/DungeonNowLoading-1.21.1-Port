package dev.hexnowloading.dungeonnowloading.platform.services;

import net.minecraft.world.level.block.Block;

public interface WoodBehaviorHelper {
    void registerFlammable(Block block, int encouragement, int flammability);

    void registerStrippable(Block input, Block stripped);
}
