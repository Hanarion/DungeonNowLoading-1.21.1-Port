package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ContainerDropManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternBlock.class)
public abstract class LecternBlockMixin {

    @Inject(method = "popBook",
            at = @At("HEAD"),
            cancellable = true)
    private void cancelPopBook(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.gameEvent(null, DNLGameEvents.holder(DNLGameEvents.BLOCK_CONTENT_DROPPING), Vec3.atCenterOf(pos));
        }

        if (ContainerDropManager.shouldCancel(pos)) {
            ContainerDropManager.reset();
            ci.cancel();
        }
    }
}
