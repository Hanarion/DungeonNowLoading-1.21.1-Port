package dev.hexnowloading.dungeonnowloading.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.PileBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class BookPileRandomizerProcessor extends StructureProcessor {

    public static final Codec<BookPileRandomizerProcessor> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                    ResourceLocation.CODEC.listOf().fieldOf("loot_tables").forGetter(p -> p.lootTables),
                    Codec.BOOL.optionalFieldOf("write_seed", true).forGetter(p -> p.writeSeed),
                    Codec.unboundedMap(Codec.INT, Codec.INT)
                            .optionalFieldOf("pile_weights", java.util.Map.of(1,1,2,1,3,1,4,1))
                            .forGetter(p -> p.pileWeights)
            ).apply(inst, BookPileRandomizerProcessor::new));

    private final List<ResourceLocation> lootTables;
    private final boolean writeSeed;

    private final java.util.Map<Integer, Integer> pileWeights;

    public BookPileRandomizerProcessor(
            List<ResourceLocation> lootTables,
            boolean writeSeed,
            java.util.Map<Integer, Integer> pileWeights
    ) {
        this.lootTables = lootTables;
        this.writeSeed = writeSeed;
        this.pileWeights = pileWeights;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return DNLProcessors.BOOK_PILE_RANDOMIZER_PROCESSOR.get();
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pos,
            StructureTemplate.StructureBlockInfo rawInfo,
            StructureTemplate.StructureBlockInfo placedInfo,
            StructurePlaceSettings settings
    ) {
        BlockState state = placedInfo.state();

        if (!state.is(DNLBlocks.BOOK_PILE.get())) {
            return placedInfo;
        }

        BlockPos worldPos = placedInfo.pos();
        long seed = net.minecraft.util.Mth.getSeed(worldPos.getX(), worldPos.getY(), worldPos.getZ())
                ^ settings.getRandom(worldPos).nextLong();

        net.minecraft.world.level.levelgen.WorldgenRandom rand =
                new net.minecraft.world.level.levelgen.WorldgenRandom(
                        new net.minecraft.world.level.levelgen.LegacyRandomSource(seed)
                );

        int pile = pickWeightedPile(rand);
        if (state.hasProperty(PileBlock.PILE)) {
            state = state.setValue(PileBlock.PILE, pile);
        }

        if (lootTables.isEmpty()) {
            return new StructureTemplate.StructureBlockInfo(worldPos, state, placedInfo.nbt());
        }

        ResourceLocation chosen = lootTables.get(rand.nextInt(lootTables.size()));

        CompoundTag tag = placedInfo.nbt() == null ? new CompoundTag() : placedInfo.nbt().copy();
        tag.putString("LootTable", chosen.toString());
        if (writeSeed) {
            tag.putLong("LootTableSeed", rand.nextLong());
        }

        return new StructureTemplate.StructureBlockInfo(worldPos, state, tag);
    }

    private int pickWeightedPile(net.minecraft.world.level.levelgen.WorldgenRandom rand) {

        int total = 0;
        for (int w : pileWeights.values()) {
            total += Math.max(w, 0);
        }

        if (total <= 0) {
            return 1;
        }

        int r = rand.nextInt(total);

        for (var entry : pileWeights.entrySet()) {
            r -= Math.max(entry.getValue(), 0);
            if (r < 0) {
                return entry.getKey();
            }
        }

        return 1;
    }

}
