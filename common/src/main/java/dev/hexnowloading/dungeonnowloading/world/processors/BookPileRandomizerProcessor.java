package dev.hexnowloading.dungeonnowloading.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.PileBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLProcessors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class BookPileRandomizerProcessor extends StructureProcessor {

    public static final Codec<BookPileRandomizerProcessor> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.listOf().fieldOf("loot_tables").forGetter(p -> p.lootTables),
            Codec.intRange(1, 4).optionalFieldOf("pile_min", 1).forGetter(p -> p.pileMin),
            Codec.intRange(1, 4).optionalFieldOf("pile_max", 4).forGetter(p -> p.pileMax),
            Codec.BOOL.optionalFieldOf("write_seed", true).forGetter(p -> p.writeSeed)
    ).apply(inst, BookPileRandomizerProcessor::new));

    private final List<ResourceLocation> lootTables;
    private final int pileMin;
    private final int pileMax;
    private final boolean writeSeed;

    public BookPileRandomizerProcessor(List<ResourceLocation> lootTables, int pileMin, int pileMax, boolean writeSeed) {
        this.lootTables = lootTables;
        this.pileMin = pileMin;
        this.pileMax = pileMax;
        this.writeSeed = writeSeed;
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

        // Only touch your book pile block
        if (!state.is(DNLBlocks.BOOK_PILE.get())) {
            return placedInfo;
        }

        RandomSource rand = settings.getRandom(pos);

        // Randomize pile count
        int pile = pileMin + rand.nextInt((pileMax - pileMin) + 1);
        if (state.hasProperty(PileBlock.PILE)) {
            state = state.setValue(PileBlock.PILE, pile);
        }

        // Pick a loot table
        if (lootTables.isEmpty()) {
            return new StructureTemplate.StructureBlockInfo(placedInfo.pos(), state, placedInfo.nbt());
        }
        ResourceLocation chosen = lootTables.get(rand.nextInt(lootTables.size()));

        // Merge / create BE NBT and write LootTable like chest/shulker does
        CompoundTag tag = placedInfo.nbt() == null ? new CompoundTag() : placedInfo.nbt().copy();
        tag.putString("LootTable", chosen.toString());
        if (writeSeed) {
            tag.putLong("LootTableSeed", rand.nextLong());
        }

        return new StructureTemplate.StructureBlockInfo(placedInfo.pos(), state, tag);
    }
}
