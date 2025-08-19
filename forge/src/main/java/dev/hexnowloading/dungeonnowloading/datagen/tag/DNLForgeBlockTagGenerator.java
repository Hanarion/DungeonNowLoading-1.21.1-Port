package dev.hexnowloading.dungeonnowloading.datagen.tag;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DNLForgeBlockTagGenerator extends BlockTagsProvider {

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
                DNLBlocks.REDSTONE_IDOL.get()
        );

        this.tag(BlockTags.MINEABLE_WITH_AXE).add(
                DNLBlocks.WOODEN_WALL_RACK.get(),
                DNLBlocks.WOODEN_WALL_PLATFORM.get(),
                DNLBlocks.FAIRKEEPER_CHEST.get(),
                DNLBlocks.WISE_FAIRKEEPER_CHEST.get(),
                DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get(),
                DNLBlocks.FAIRKEEEPER_SPAWNER.get(),
                DNLBlocks.MENDING_TABLE.get(),

                DNLBlocks.AZURO_OAK_LOG.get(),
                DNLBlocks.STRIPPED_AZURO_OAK_LOG.get(),
                DNLBlocks.AZURO_OAK_PLANKS.get(),
                DNLBlocks.AZURO_OAK_PLANK_STAIRS.get(),
                DNLBlocks.AZURO_OAK_PLANK_SLAB.get(),
                DNLBlocks.AZURO_OAK_PLANK_FENCE.get(),
                DNLBlocks.AZURO_OAK_PLANK_FENCE_GATE.get(),
                DNLBlocks.AZURO_OAK_DOOR.get(),
                DNLBlocks.AZURO_OAK_BUTTON.get(),
                DNLBlocks.AZURO_OAK_PRESSURE_PLATE.get()
        );

        this.tag(BlockTags.WALLS).add(
                DNLBlocks.COILING_STONE_PILLAR_WALL.get(),
                DNLBlocks.STONE_TILE_WALL.get(),
                DNLBlocks.MENDING_AURA_WALL.get()
        );

        this.tag(BlockTags.INFINIBURN_OVERWORLD).add(
                DNLBlocks.SCUTTLE_STATUE.get()
        );

        this.tag(BlockTags.WITHER_IMMUNE).add(
                DNLBlocks.STONE_PRESERVER.get(),
                DNLBlocks.MENDING_AURA.get(),
                DNLBlocks.MENDING_AURA_STAIRS.get(),
                DNLBlocks.MENDING_AURA_SLAB.get(),
                DNLBlocks.MENDING_AURA_FENCE.get(),
                DNLBlocks.MENDING_AURA_WALL.get(),
                DNLBlocks.MENDING_AURA_PANE.get(),
                DNLBlocks.MENDING_AURA_PATH.get()
        );

        this.tag(BlockTags.DRAGON_IMMUNE).add(
                DNLBlocks.STONE_PRESERVER.get()
        );

        this.tag(BlockTags.WOODEN_FENCES).add(
                DNLBlocks.MENDING_AURA_FENCE.get(),
                DNLBlocks.AZURO_OAK_PLANK_FENCE.get()
        );

        this.tag(BlockTags.STAIRS).add(
                DNLBlocks.MENDING_AURA_STAIRS.get(),
                DNLBlocks.AZURO_OAK_PLANK_STAIRS.get()
        );

        this.tag(BlockTags.WOODEN_STAIRS).add(
                DNLBlocks.MENDING_AURA_STAIRS.get(),
                DNLBlocks.AZURO_OAK_PLANK_STAIRS.get()
        );
    }
}
