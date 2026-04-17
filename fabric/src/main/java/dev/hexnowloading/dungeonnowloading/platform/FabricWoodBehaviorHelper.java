package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.platform.services.WoodBehaviorHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.world.level.block.Block;

public class FabricWoodBehaviorHelper implements WoodBehaviorHelper {
    @Override
    public void registerFlammable(Block block, int encouragement, int flammability) {
        FlammableBlockRegistry.getDefaultInstance().add(block, encouragement, flammability);
    }

    @Override
    public void registerStrippable(Block input, Block stripped) {
        StrippableBlockRegistry.register(input, stripped);
    }
}
