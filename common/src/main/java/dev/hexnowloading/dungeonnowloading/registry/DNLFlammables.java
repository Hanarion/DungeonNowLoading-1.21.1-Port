package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.mixin.FireBlockAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

/**
 * Registers flammability values for Dungeon Now Loading blocks by invoking the
 * private FireBlock#setFlammable through a Mixin @Invoker accessor.
 * This is called after all mod blocks are registered (common setup / onInitialize).
 */
public final class DNLFlammables {
    private DNLFlammables() {}

    public static void register() {
        // Ensure blocks have been registered (multi-loader safety)
        if (!DNLBlocks.blocksRegistered) return;

        FireBlock fire = (FireBlock) Blocks.FIRE;
        FireBlockAccessor accessor = (FireBlockAccessor) fire;

        // Leaves & hanging leaves
        safe(accessor, DNLBlocks.AZURO_LEAVES.get(), 30, 60);
        safe(accessor, DNLBlocks.AZURO_HANGING_LEAVES.get(), 30, 60);
        safe(accessor, DNLBlocks.AZURO_HANGING_LEAVES_TIP.get(), 30, 60);

        // Logs / stripped logs
        safe(accessor, DNLBlocks.AZURO_OAK_LOG.get(), 5, 5);
        safe(accessor, DNLBlocks.STRIPPED_AZURO_OAK_LOG.get(), 5, 5);

        // Plank family
        safe(accessor, DNLBlocks.AZURO_OAK_PLANKS.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_PLANK_SLAB.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_PLANK_STAIRS.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_PLANK_FENCE.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_PLANK_FENCE_GATE.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_BUTTON.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_PRESSURE_PLATE.get(), 5, 20);
        safe(accessor, DNLBlocks.AZURO_OAK_DOOR.get(), 5, 20);
    }

    private static void safe(FireBlockAccessor accessor, Block block, int encouragement, int flammability) {
        if (block != null) {
            accessor.dungeonnowloading$setFlammable(block, encouragement, flammability);
        }
    }
}

