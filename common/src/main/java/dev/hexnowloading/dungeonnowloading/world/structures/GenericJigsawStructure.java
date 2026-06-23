package dev.hexnowloading.dungeonnowloading.world.structures;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.registry.DNLStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class GenericJigsawStructure extends Structure {

    public static final Codec<GenericJigsawStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GenericJigsawStructure.settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 100).fieldOf("size").forGetter(structure -> structure.size),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter(structure -> structure.useExpansionHack),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
            StructureTemplatePool.CODEC.optionalFieldOf("extra_surface_pool").forGetter(structure -> structure.extraSurfacePool),
            ResourceLocation.CODEC.optionalFieldOf("extra_surface_start_jigsaw_name").forGetter(structure -> structure.extraSurfaceStartJigsawName),
            Codec.intRange(0, 100).optionalFieldOf("extra_surface_size", 0).forGetter(structure -> structure.extraSurfaceSize),
            Codec.intRange(1, 128).optionalFieldOf("extra_surface_max_distance_from_center", 80).forGetter(structure -> structure.extraSurfaceMaxDistanceFromCenter),
            Heightmap.Types.CODEC.optionalFieldOf("extra_surface_heightmap", Heightmap.Types.WORLD_SURFACE_WG).forGetter(structure -> structure.extraSurfaceHeightmap),
            Codec.INT.optionalFieldOf("extra_surface_y_offset", 0).forGetter(structure -> structure.extraSurfaceYOffset)
    ).apply(instance, GenericJigsawStructure::new));
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final Optional<Holder<StructureTemplatePool>> extraSurfacePool;
    private final Optional<ResourceLocation> extraSurfaceStartJigsawName;
    private final int extraSurfaceSize;
    private final int extraSurfaceMaxDistanceFromCenter;
    private final Heightmap.Types extraSurfaceHeightmap;
    private final int extraSurfaceYOffset;

    public GenericJigsawStructure(StructureSettings config, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, Optional<Holder<StructureTemplatePool>> extraSurfacePool, Optional<ResourceLocation> extraSurfaceStartJigsawName, int extraSurfaceSize, int extraSurfaceMaxDistanceFromCenter, Heightmap.Types extraSurfaceHeightmap, int extraSurfaceYOffset) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.extraSurfacePool = extraSurfacePool;
        this.extraSurfaceStartJigsawName = extraSurfaceStartJigsawName;
        this.extraSurfaceSize = extraSurfaceSize;
        this.extraSurfaceMaxDistanceFromCenter = extraSurfaceMaxDistanceFromCenter;
        this.extraSurfaceHeightmap = extraSurfaceHeightmap;
        this.extraSurfaceYOffset = extraSurfaceYOffset;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());
        Optional<GenerationStub> structurePiecesGenerator =
                JigsawPlacement.addPieces(
                        context,
                        this.startPool,
                        this.startJigsawName,
                        this.size,
                        blockPos,
                        false,
                        this.projectStartToHeightmap,
                        this.maxDistanceFromCenter,
                        net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup.EMPTY,
                        net.minecraft.world.level.levelgen.structure.pools.DimensionPadding.ZERO,
                        net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings.APPLY_WATERLOGGING);
        return structurePiecesGenerator.map(generationStub -> this.addExtraSurfacePieces(context, generationStub));
    }

    private GenerationStub addExtraSurfacePieces(GenerationContext context, GenerationStub generationStub) {
        if (this.extraSurfacePool.isEmpty()) {
            return generationStub;
        }

        StructurePiecesBuilder piecesBuilder = generationStub.getPiecesBuilder();
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX,
                centerZ,
                this.extraSurfaceHeightmap,
                context.heightAccessor(),
                context.randomState()) + this.extraSurfaceYOffset;

        JigsawPlacement.addPieces(
                context,
                this.extraSurfacePool.get(),
                this.extraSurfaceStartJigsawName,
                this.extraSurfaceSize,
                new BlockPos(centerX, surfaceY, centerZ),
                false,
                Optional.empty(),
                this.extraSurfaceMaxDistanceFromCenter,
                net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup.EMPTY,
                net.minecraft.world.level.levelgen.structure.pools.DimensionPadding.ZERO,
                net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings.APPLY_WATERLOGGING
        ).ifPresent(surfaceStub -> surfaceStub.getPiecesBuilder().build().pieces().forEach(piecesBuilder::addPiece));
        return new GenerationStub(generationStub.position(), Either.right(piecesBuilder));
    }

    @Override
    public StructureType<?> type() {
        return DNLStructures.GENERIC_JIGSAW_STRUCTURE.get();
    }
}
