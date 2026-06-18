package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.block.SuspendedWebBlock;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.BlockDestructionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class PlayerBlockDestroyCancelMixin {

    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    private static boolean shouldCancelDestruction;

    @Shadow public abstract boolean isCreative();

    private boolean dungeonnowloading$creativeDestroyingSuspendedWeb;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(blockPos);
        this.dungeonnowloading$creativeDestroyingSuspendedWeb = this.isCreative()
                && state.getBlock() instanceof SuspendedWebBlock
                && state.hasProperty(SuspendedWebBlock.FACING);
        if (this.dungeonnowloading$creativeDestroyingSuspendedWeb) {
            if (player.getMainHandItem().getItem() instanceof SwordItem) {
                this.dungeonnowloading$creativeDestroyingSuspendedWeb = false;
                cir.setReturnValue(false);
                return;
            }
            if (!player.getMainHandItem().getItem().canAttackBlock(state, this.level, blockPos, this.player)) {
                this.dungeonnowloading$creativeDestroyingSuspendedWeb = false;
                cir.setReturnValue(false);
                return;
            }
            SuspendedWebBlock.beginCreativePlayerRemoval();
            SuspendedWebBlock.markPlayerRemovedStructure(level, blockPos, state.getValue(SuspendedWebBlock.FACING));
            this.dungeonnowloading$removeSuspendedWebStructure(blockPos, state.getValue(SuspendedWebBlock.FACING));
            SuspendedWebBlock.endCreativePlayerRemoval();
            this.dungeonnowloading$creativeDestroyingSuspendedWeb = false;
            cir.setReturnValue(true);
            return;
        }

        BlockDestructionManager.reset();
        level.gameEvent(DNLGameEvents.PLAYER_BLOCK_DESTROY_EARLY.get(), blockPos, GameEvent.Context.of(player, level.getBlockState(blockPos)));

        var be = level.getBlockEntity(blockPos);
        if (be instanceof dev.hexnowloading.dungeonnowloading.block.entity.DuriteQuellerBlockEntity quellerBe) {
            quellerBe.tryReplaceSelfWithMendingAura(level);
        }

        if (BlockDestructionManager.shouldCancel()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void dungeonnowloading$endCreativeSuspendedWebDestroy(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (this.dungeonnowloading$creativeDestroyingSuspendedWeb) {
            SuspendedWebBlock.endCreativePlayerRemoval();
            this.dungeonnowloading$creativeDestroyingSuspendedWeb = false;
        }
    }

    private void dungeonnowloading$removeSuspendedWebStructure(BlockPos origin, Direction facing) {
        BlockPos start = origin;
        while (this.dungeonnowloading$isMatchingSuspendedWeb(start.relative(facing.getOpposite()), facing)) {
            start = start.relative(facing.getOpposite());
        }

        BlockPos end = origin;
        while (this.dungeonnowloading$isMatchingSuspendedWeb(end.relative(facing), facing)) {
            end = end.relative(facing);
        }

        for (BlockPos pos = start; ; pos = pos.relative(facing)) {
            if (this.dungeonnowloading$isMatchingSuspendedWeb(pos, facing)) {
                this.level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
            if (pos.equals(end)) {
                break;
            }
        }
    }

    private boolean dungeonnowloading$isMatchingSuspendedWeb(BlockPos pos, Direction facing) {
        BlockState state = this.level.getBlockState(pos);
        return state.getBlock() instanceof SuspendedWebBlock
                && state.hasProperty(SuspendedWebBlock.FACING)
                && state.getValue(SuspendedWebBlock.FACING) == facing;
    }
}
