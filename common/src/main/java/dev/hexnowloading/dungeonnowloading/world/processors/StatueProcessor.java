package dev.hexnowloading.dungeonnowloading.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLProcessors;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StatueProcessor extends StructureProcessor {

    public static final Codec<StatueProcessor> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.STRING.fieldOf("campaign").forGetter(p -> p.campaign),
                    Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(p -> p.chance),
                    BuiltInRegistries.BLOCK.byNameCodec()
                            .optionalFieldOf("fallback_block", Blocks.AIR)
                            .forGetter(p -> p.fallbackBlock)
            ).apply(i, StatueProcessor::new)
    );

    private final String campaign;
    private final float chance;           // 0..1
    private final Block fallbackBlock;    // replacement when roll fails

    // Back-compat ctor (when created programmatically)
    public StatueProcessor(String campaign) { this(campaign, 1.0F, Blocks.AIR); }

    public StatueProcessor(String campaign, float chance, Block fallbackBlock) {
        this.campaign = campaign;
        this.chance = Math.max(0.0F, Math.min(1.0F, chance));
        this.fallbackBlock = fallbackBlock == null ? Blocks.AIR : fallbackBlock;
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level, BlockPos pieceOrigin, BlockPos pieceWorldPos,
            StructureTemplate.StructureBlockInfo original,
            StructureTemplate.StructureBlockInfo current,
            StructurePlaceSettings settings
    ) {
        if (!current.state().is(DNLBlocks.PLAYER_STATUE.get())) return current;

        RandomSource rand = settings.getRandom(current.pos());

        // Roll first; on fail, replace the statue block entirely.
        if (rand.nextFloat() >= this.chance) {
            return new StructureTemplate.StructureBlockInfo(
                    current.pos(),
                    this.fallbackBlock.defaultBlockState(),
                    null // no NBT on the fallback
            );
        }

        // We’re keeping the statue → build its NBT
        var state = current.state();
        var beTag = current.nbt() == null ? new CompoundTag() : current.nbt().copy();

        // Random pose if absent
        if (!beTag.contains("PoseVariant", Tag.TAG_INT)) {
            beTag.putInt("PoseVariant", rand.nextInt(dev.hexnowloading.dungeonnowloading.block.PlayerStatueBlock.MAX_POSES));
        }

        // Clear any pre-authored owner and assign a random patron
        beTag.remove("Owner");
        beTag.remove("SkullOwner");

        var patron = PatronRegistry.pickPatron(this.campaign, rand);
        if (patron != null && patron.uuid != null) {
            CompoundTag owner = new CompoundTag();
            owner.putUUID("Id", patron.uuid);
            if (patron.name != null && !patron.name.isBlank()) {
                owner.putString("Name", patron.name);
            }
            beTag.put("Owner", owner);

            var tier = PatronRegistry.tierFor(patron);
            beTag.putString("NotchTier", tier.name());
        } else {
            beTag.putString("NotchTier", PlayerStatueBlockEntity.NotchTier.NONE.name());
        }

        return new StructureTemplate.StructureBlockInfo(current.pos(), state, beTag);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return DNLProcessors.STATUE_PROCESSOR.get();
    }
}
