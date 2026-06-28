package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ExplosionDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

/**
 * 1.21 explosion-protection hook. In 1.21 the per-block destruction of an explosion runs through
 * {@code BlockBehaviour.onExplosionHit(state, level, pos, explosion, dropConsumer)} (which drops
 * the items and then calls {@code onBlockExploded} to remove the block). The previous mixin keyed
 * off {@code Explosion.finalizeExplosion}'s {@code isAir()} loop, which no longer exists — so the
 * mod's {@code BLOCK_DESTROYED_BY_EXPLOSION} game event was never posted and Preserver / Mendstone
 * Chalk Mark regions never regenerated blocks destroyed by TNT/creepers.
 *
 * Here we post that game event at HEAD (which lets the Preserver listener replace the block with a
 * Mending Aura) and, if the listener cancelled the destruction, abort {@code onExplosionHit} so
 * vanilla does not also drop items or remove the freshly-placed Mending Aura.
 */
@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourExplosionMixin {

    @Inject(method = "onExplosionHit", at = @At("HEAD"), cancellable = true)
    private void dnl_onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                                    BiConsumer<ItemStack, BlockPos> dropConsumer, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ExplosionDestructionManager.reset();
        serverLevel.gameEvent(null, DNLGameEvents.holder(DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION), Vec3.atCenterOf(pos));

        if (ExplosionDestructionManager.shouldCancel(pos)) {
            ci.cancel();
        }
    }
}
