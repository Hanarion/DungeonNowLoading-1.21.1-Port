package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.util.event_managers.ExplosionDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21: vanilla no longer destroys explosion-hit blocks via an isAir()-gated loop in
 * finalizeExplosion — each block is now removed through {@code BlockBehaviour.onExplosionHit}.
 * The per-block DNL game event ({@code BLOCK_DESTROYED_BY_EXPLOSION}) and the cancellation are
 * therefore handled in {@link BlockBehaviourExplosionMixin}; this mixin only flushes the
 * neighbour re-render updates that protected blocks queue while being replaced by Mending Aura.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private Level level;

    @Inject(method = "finalizeExplosion", at = @At("TAIL"))
    private void sendProtectedBlockUpdates(boolean spawnParticles, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (BlockPos pos : ExplosionDestructionManager.consumePendingBlockUpdates()) {
            BlockState state = serverLevel.getBlockState(pos);
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
}
