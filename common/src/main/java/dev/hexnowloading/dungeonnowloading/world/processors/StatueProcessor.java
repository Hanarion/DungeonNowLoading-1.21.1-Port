package dev.hexnowloading.dungeonnowloading.world.processors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.PlayerStatueBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLProcessors;
import dev.hexnowloading.dungeonnowloading.supporter.PatronRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag; // <-- use Tag.TAG_INT instead of raw 3
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StatueProcessor extends StructureProcessor {

    public static final Codec<StatueProcessor> CODEC = RecordCodecBuilder.create(i ->
            i.group(Codec.STRING.fieldOf("campaign").forGetter(p -> p.campaign))
                    .apply(i, StatueProcessor::new));

    private final String campaign;

    public StatueProcessor(String campaign) { this.campaign = campaign; }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            LevelReader level, BlockPos pieceOrigin, BlockPos pieceWorldPos,
            StructureTemplate.StructureBlockInfo original,
            StructureTemplate.StructureBlockInfo current,
            StructurePlaceSettings settings
    ) {
        if (!current.state().is(DNLBlocks.PLAYER_STATUE.get())) return current;

        // Keep rotation/state as placed by the template
        var state = current.state();
        var beTag = current.nbt() == null ? new CompoundTag() : current.nbt().copy();

        RandomSource rand = settings.getRandom(current.pos());

        /// Always override any existing owner
        beTag.remove("Owner");
        beTag.remove("SkullOwner");

        var patron = PatronRegistry.pickPatron(this.campaign, rand);
        if (patron != null && patron.uuid != null) {
            CompoundTag owner = new CompoundTag();
            owner.putUUID("Id", patron.uuid);
            if (patron.name != null && !patron.name.isBlank()) {
                owner.putString("Name", patron.name);   // <-- add Name
            }
            beTag.put("Owner", owner);

            var tier = PatronRegistry.tierFor(patron);
            beTag.putString("NotchTier", tier.name());
        } else {
            beTag.putString("NotchTier", PlayerStatueBlockEntity.NotchTier.NONE.name());
        }


        // Fill PoseVariant if missing
        if (!beTag.contains("PoseVariant", Tag.TAG_INT)) {
            beTag.putInt("PoseVariant", rand.nextInt(PlayerStatueBlock.MAX_POSES));
        }

        return new StructureTemplate.StructureBlockInfo(current.pos(), state, beTag);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return DNLProcessors.STATUE_PROCESSOR.get();
    }
}
