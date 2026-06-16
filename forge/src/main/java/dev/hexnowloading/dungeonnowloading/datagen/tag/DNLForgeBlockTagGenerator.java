package dev.hexnowloading.dungeonnowloading.datagen.tag;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DNLForgeBlockTagGenerator extends BlockTagsProvider {
    private static final TagKey<Block> MINEABLE_WITH_SHEARS = BlockTags.create(new ResourceLocation("minecraft", "mineable/shears"));
    private static final TagKey<Block> MINEABLE_WITH_SWORD = BlockTags.create(new ResourceLocation("minecraft", "mineable/sword"));

    public DNLForgeBlockTagGenerator(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), lookupProvider, DungeonNowLoading.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                DNLBlocks.COILING_STONE_PILLAR.get(),
                DNLBlocks.CHISELED_COILING_STONE_PILLAR.get(),
                DNLBlocks.COILING_STONE_PILLAR_CAPITAL.get(),
                DNLBlocks.COILING_STONE_PILLAR_STAIRS.get(),
                DNLBlocks.COILING_STONE_PILLAR_SLAB.get(),
                DNLBlocks.COILING_STONE_PILLAR_WALL.get(),
                DNLBlocks.STONE_TILES.get(),
                DNLBlocks.CRACKED_STONE_TILES.get(),
                DNLBlocks.STONE_TILE_STAIRS.get(),
                DNLBlocks.STONE_TILE_SLAB.get(),
                DNLBlocks.STONE_TILE_WALL.get(),
                DNLBlocks.OVERCHARGED_REDSTONE_BLOCK.get(),
                DNLBlocks.REDSTONE_LANE_I.get(),
                DNLBlocks.REDSTONE_LANE_T.get(),
                DNLBlocks.REDSTONE_LANE_L.get(),
                DNLBlocks.ROTATOR_PRESSURE_PLATE.get(),
                DNLBlocks.SIGNALING_STONE_EMBLEM.get(),
                DNLBlocks.DUELING_STONE_EMBLEM.get(),
                DNLBlocks.PUZZLING_STONE_EMBLEM.get(),
                DNLBlocks.POLISHED_STONE.get(),
                DNLBlocks.BORDERED_STONE.get(),
                DNLBlocks.COBBLESTONE_PEBBLES.get(),
                DNLBlocks.MOSSY_COBBLESTONE_PEBBLES.get(),
                DNLBlocks.IRON_INGOT_PILE.get(),
                DNLBlocks.GOLD_INGOT_PILE.get(),
                DNLBlocks.CHAOS_SPAWNER_EDGE.get(),
                DNLBlocks.CHAOS_SPAWNER_DIAMOND_EDGE.get(),
                DNLBlocks.CHAOS_SPAWNER_DIAMOND_VERTEX.get(),
                DNLBlocks.SPIKES.get(),
                DNLBlocks.FAIRKEEEPER_SPAWNER.get(),
                DNLBlocks.STONE_NOTCH.get(),
                DNLBlocks.COAL_STONE_NOTCH.get(),
                DNLBlocks.COPPER_STONE_NOTCH.get(),
                DNLBlocks.IRON_STONE_NOTCH.get(),
                DNLBlocks.GOLD_STONE_NOTCH.get(),
                DNLBlocks.REDSTONE_STONE_NOTCH.get(),
                DNLBlocks.AMETHYST_STONE_NOTCH.get(),
                DNLBlocks.LAPIS_STONE_NOTCH.get(),
                DNLBlocks.EMERALD_STONE_NOTCH.get(),
                DNLBlocks.QUARTZ_STONE_NOTCH.get(),
                DNLBlocks.GLOWSTONE_STONE_NOTCH.get(),
                DNLBlocks.PRISMARINE_STONE_NOTCH.get(),
                DNLBlocks.CHORUS_STONE_NOTCH.get(),
                DNLBlocks.ECHO_STONE_NOTCH.get(),
                DNLBlocks.DIAMOND_STONE_NOTCH.get(),
                DNLBlocks.NETHERITE_STONE_NOTCH.get(),
                DNLBlocks.SIGNAL_GATE.get(),
                DNLBlocks.SCUTTLE_STATUE.get(),
                DNLBlocks.BALLISTA_GOLEM_STATUE.get(),
                DNLBlocks.BALLISTA_GOLEM_STATUE_PART.get(),
                DNLBlocks.VERTEX_PILLAR.get(),
                DNLBlocks.STONE_PRESERVER.get(),
                DNLBlocks.REDSTONE_IDOL.get(),
                DNLBlocks.PLAYER_STATUE.get(),
                DNLBlocks.MENDING_TABLE.get(),
                DNLBlocks.DURITE_CLUSTER.get(),
                DNLBlocks.SMALL_DURITE_BUD.get(),
                DNLBlocks.MEDIUM_DURITE_BUD.get(),
                DNLBlocks.LARGE_DURITE_BUD.get(),
                DNLBlocks.DURITE_QUELLER.get(),
                DNLBlocks.BRITTLESTONE.get(),
                DNLBlocks.DEEPSTEEL_BLOCK.get(),
                DNLBlocks.BURNACLE.get(),
                DNLBlocks.DEEPSTEEL_BLOCK.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_FRAME.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get(),
                DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING.get(),
                DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL.get(),
                DNLBlocks.DEEPSTEEL_PLATFORM_ENCLOSED_STAIRS.get()
        );

        this.tag(BlockTags.RAILS).add(
                DNLBlocks.DEEPSTEEL_MOUNTED_RAIL.get(),
                DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get(),
                DNLBlocks.DEEPSTEEL_MOUNTED_DETECTOR_RAIL.get(),
                DNLBlocks.DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL.get()
        );

        this.tag(BlockTags.MINEABLE_WITH_AXE).add(
                DNLBlocks.WOODEN_WALL_RACK.get(),
                DNLBlocks.WOODEN_WALL_PLATFORM.get(),
                DNLBlocks.FAIRKEEPER_CHEST.get(),
                DNLBlocks.WISE_FAIRKEEPER_CHEST.get(),
                DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get(),
                DNLBlocks.FAIRKEEEPER_SPAWNER.get()
        );

        this.tag(MINEABLE_WITH_SHEARS).add(
                DNLBlocks.SUSPENDED_WEB.get()
        );

        this.tag(MINEABLE_WITH_SWORD).add(
                DNLBlocks.SUSPENDED_WEB.get()
        );

        this.tag(BlockTags.FALL_DAMAGE_RESETTING).add(
                DNLBlocks.WEB_CARPET.get(),
                DNLBlocks.SUSPENDED_WEB.get()
        );

        this.tag(BlockTags.WALLS).add(
                DNLBlocks.COILING_STONE_PILLAR_WALL.get(),
                DNLBlocks.STONE_TILE_WALL.get()
        );

        this.tag(BlockTags.INFINIBURN_OVERWORLD).add(
                DNLBlocks.SCUTTLE_STATUE.get()
        );

        this.tag(BlockTags.WITHER_IMMUNE).add(
                DNLBlocks.STONE_PRESERVER.get(),
                DNLBlocks.MENDING_AURA.get()
        );

        this.tag(BlockTags.DRAGON_IMMUNE).add(
                DNLBlocks.STONE_PRESERVER.get()
        );

    }
}
