package dev.hexnowloading.dungeonnowloading.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class LabyrinthSurfaceTemplateFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation TEMPLATE_ID =
            ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "labyrinth/surface");

    public LabyrinthSurfaceTemplateFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();

        BlockPos origin = ctx.origin();
        System.out.println("[DNL] LabyrinthSurfaceTemplateFeature CALLED at origin=" + origin
                + " dim=" + level.getLevel().dimension().location());

        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager templates = serverLevel.getStructureManager();

        // IMPORTANT: log whether it resolves
        StructureTemplate template = templates.getOrCreate(TEMPLATE_ID);
        if (template == null) {
            System.out.println("[DNL] Template is NULL for id=" + TEMPLATE_ID);
            return false;
        }

        Rotation rotation = Rotation.getRandom(random);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setIgnoreEntities(true)
                .setKnownShape(true);

        Vec3i size = template.getSize(rotation);

        // Ignore surface: paste right at origin Y
        BlockPos centered = origin.offset(-size.getX() / 2, 0, -size.getZ() / 2);

        boolean ok = template.placeInWorld(level, centered, centered, settings, random, 2);

        System.out.println("[DNL] Paste result=" + ok + " at " + centered + " rot=" + rotation
                + " size=" + size);

        // Extra: leave a visible marker block so you can find it even if template is subtle
        // Uncomment temporarily if you want:
        // level.setBlock(centered, net.minecraft.world.level.block.Blocks.GOLD_BLOCK.defaultBlockState(), 2);

        return ok;
    }
}