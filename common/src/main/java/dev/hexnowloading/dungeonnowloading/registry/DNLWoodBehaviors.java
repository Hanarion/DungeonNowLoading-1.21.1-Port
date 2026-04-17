package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;

public final class DNLWoodBehaviors {
    private DNLWoodBehaviors() {}

    public static void register() {
        if (!DNLBlocks.blocksRegistered) return;

        registerFlammable(DNLBlocks.AZURO_LEAVES.get(), 30, 60);
        registerFlammable(DNLBlocks.AZURO_HANGING_LEAVES.get(), 30, 60);
        registerFlammable(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get(), 30, 60);

        registerFlammable(DNLBlocks.AZURO_OAK_LOG.get(), 5, 5);
        registerFlammable(DNLBlocks.STRIPPED_AZURO_OAK_LOG.get(), 5, 5);

        registerFlammable(DNLBlocks.AZURO_OAK_PLANKS.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_PLANK_SLAB.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_PLANK_STAIRS.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_PLANK_FENCE.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_PLANK_FENCE_GATE.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_BUTTON.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_PRESSURE_PLATE.get(), 5, 20);
        registerFlammable(DNLBlocks.AZURO_OAK_DOOR.get(), 5, 20);

        Services.WOOD_BEHAVIORS.registerStrippable(DNLBlocks.AZURO_OAK_LOG.get(), DNLBlocks.STRIPPED_AZURO_OAK_LOG.get());
    }

    private static void registerFlammable(net.minecraft.world.level.block.Block block, int encouragement, int flammability) {
        Services.WOOD_BEHAVIORS.registerFlammable(block, encouragement, flammability);
    }
}
