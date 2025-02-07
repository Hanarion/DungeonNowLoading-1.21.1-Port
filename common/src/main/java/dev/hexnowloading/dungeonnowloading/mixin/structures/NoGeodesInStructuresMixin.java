package dev.hexnowloading.dungeonnowloading.mixin.structures;

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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static dev.hexnowloading.dungeonnowloading.registry.DNLTags.NO_GEODES_TAG;

@Mixin(GeodeFeature.class)
public class NoGeodesInStructuresMixin {

    @Inject(
            method = "place(Lnet/minecraft/world/level/levelgen/feature/FeaturePlaceContext;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void preventGeodeInStructures(FeaturePlaceContext<BlockStateConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.level() instanceof WorldGenRegion worldGenRegion)) {
            return;
        }

        StructureManager structureManager = worldGenRegion.getLevel().structureManager();
        Registry<Structure> structureRegistry = worldGenRegion.registryAccess().registry(Registries.STRUCTURE).get();

        // Get all structure starts at the geode position
        List<StructureStart> structureStarts = getValidStructureStarts(
                worldGenRegion,
                structureManager,
                structureRegistry,
                context.origin()
        );

        // Check if any structure at this location is tagged as "NO_GEODES" and is too close
        boolean isBlockedByStructure = structureStarts.stream()
                .anyMatch(structureStart ->
                        structureRegistry.getHolder(structureRegistry.getResourceKey(structureStart.getStructure()).get())
                                .map(holder -> holder.is(NO_GEODES_TAG))
                                .orElse(false)
                                && isTooCloseToStructure(structureStart, context.origin())
                );

        if (isBlockedByStructure) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Retrieves all valid structure starts at a given position in the world.
     */
    private static List<StructureStart> getValidStructureStarts(
            LevelReader world,
            StructureManager structureManager,
            Registry<Structure> structureRegistry,
            BlockPos pos) {

        ChunkPos chunkPos = new ChunkPos(pos);
        SectionPos sectionPos = SectionPos.of(chunkPos, world.getMinSection());

        if (!world.hasChunk(sectionPos.x(), sectionPos.z())) {
            return List.of(); // Avoid checking unloaded chunks
        }

        ChunkAccess chunk = world.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS);
        var structureReferences = chunk.getAllReferences(); // Map<Structure, LongSet>

        List<StructureStart> validStructures = new ArrayList<>();

        for (var entry : structureReferences.entrySet()) {
            Structure structure = entry.getKey();
            LongSet references = entry.getValue();

            for (long ref : references) {
                SectionPos refSection = SectionPos.of(new ChunkPos(ref), world.getMinSection());
                StructureStart structureStart = structureManager.getStartForStructure(refSection, structure, chunk);

                if (structureStart != null && structureStart.isValid()) {
                    validStructures.add(structureStart);
                }
            }
        }

        return validStructures;
    }

    /**
     * Checks if a geode is too close to a structure using a fixed 16-block exclusion zone.
     */
    private static boolean isTooCloseToStructure(StructureStart structureStart, BlockPos pos) {
        return isInsideExpandedBoundingBox(structureStart.getBoundingBox(), pos, 16);
    }

    /**
     * Checks if a block position is inside an expanded bounding box.
     */
    private static boolean isInsideExpandedBoundingBox(net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox, BlockPos pos, int expansion) {
        return pos.getX() >= (boundingBox.minX() - expansion) && pos.getX() <= (boundingBox.maxX() + expansion)
                && pos.getY() >= (boundingBox.minY() - expansion) && pos.getY() <= (boundingBox.maxY() + expansion)
                && pos.getZ() >= (boundingBox.minZ() - expansion) && pos.getZ() <= (boundingBox.maxZ() + expansion);
    }
}
