package dev.hexnowloading.dungeonnowloading.mixin.fabric.structures;

import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(StructureTemplatePool.class)
public class StructurePoolMixin {

    /**
     * Increases the weight limit that mojang slapped on that was a workaround for "https://bugs.mojang.com/browse/MC-203131"
     * @author - TelepathicGrunt
     * @return - The higher weight that is a more reasonable limit.
     */
    @ModifyConstant(
            // Fabric loom dev + production use intermediary names at runtime; the 150 weight-limit
            // lives in StructureTemplatePool.method_28886 (the codec-builder method). require=0 as a
            // safety net if the name shifts.
            method = "method_28886",
            constant = @Constant(intValue = 150),
            remap = false,
            require = 0
    )
    private static int dungeonnowloading_increaseWeightLimit(int constant) {
        return 5000;
    }
}
