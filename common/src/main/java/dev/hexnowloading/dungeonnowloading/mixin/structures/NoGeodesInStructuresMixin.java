package dev.hexnowloading.dungeonnowloading.mixin.structures;

import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/*
 * This code is adapted from the RepurposedStructures project
 * (https://github.com/TelepathicGrunt/RepurposedStructures),
 * licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).
 *
 * Portions of this code are based on:
 * - NoGeodesInStructuresMixin.java:
 *   https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.21.5-MDG/common/src/main/java/com/telepathicgrunt/repurposedstructures/mixins/features/NoGeodesInStructuresMixin.java
 * - GeneralUtils.java:
 *   https://github.com/TelepathicGrunt/RepurposedStructures/blob/1.21.5-MDG/common/src/main/java/com/telepathicgrunt/repurposedstructures/utils/GeneralUtils.java
 *
 * Changes have been made to better fit Dungeon Now Loading's structure generation system.
 *
 * A copy of the LGPL-3.0 license can be found at:
 * https://www.gnu.org/licenses/lgpl-3.0.html
 */


@Mixin(GeodeFeature.class)
public class NoGeodesInStructuresMixin {

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dungeonnowloading_noGeodesInStructures(FeaturePlaceContext<GeodeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) {
            return;
        }

        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registryOrThrow(Registries.STRUCTURE);

        List<StructureStart> starts = getValidStructureStarts(
                worldGenRegion,
                context.origin(),
                structure -> structureRegistry.getHolder(structureRegistry.getResourceKey(structure).get())
                        .map(holder -> holder.is(DNLTags.NO_GEODES_TAG))
                        .orElse(false)
        );

        if (!starts.isEmpty()) {
            cir.setReturnValue(false); // Cancel geode generation if any NO_GEODES-tagged structure is present
        }
    }

    private static List<StructureStart> getValidStructureStarts(WorldGenRegion level, BlockPos pos, Predicate<Structure> structureMatch) {
        StructureManager structureManager = level.getLevel().structureManager();
        SectionPos sectionPos = SectionPos.of(pos);

        ChunkAccess chunk = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES);
        if (!chunk.getHighestGeneratedStatus().isOrAfter(ChunkStatus.STRUCTURE_REFERENCES)) {
            return List.of();
        }

        Map<Structure, LongSet> references = chunk.getAllReferences();
        List<StructureStart> list = new ArrayList<>();

        for (var entry : references.entrySet()) {
            Structure structure = entry.getKey();
            LongSet refs = entry.getValue();

            if (structureMatch.test(structure)) {
                fillStartsForStructure(level, structureManager, structure, refs, pos, list::add);
            }
        }

        return list;
    }

    private static void fillStartsForStructure(LevelReader level, StructureManager structureManager, Structure structure, LongSet references, BlockPos pos, Consumer<StructureStart> consumer) {
        for (long ref : references) {
            SectionPos sectionPos = SectionPos.of(new ChunkPos(ref), level.getMinSection());
            if (!level.hasChunk(sectionPos.x(), sectionPos.z())) {
                continue;
            }

            StructureStart start = structureManager.getStartForStructure(sectionPos, structure, level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
            if (start != null && start.isValid() && start.getBoundingBox().isInside(pos)) {
                consumer.accept(start);
            }
        }
    }
}
