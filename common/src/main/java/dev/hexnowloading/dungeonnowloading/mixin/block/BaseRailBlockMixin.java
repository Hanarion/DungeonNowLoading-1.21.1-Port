package dev.hexnowloading.dungeonnowloading.mixin.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockMixin {
    @Shadow
    public abstract Property<RailShape> getShapeProperty();

    @Shadow
    protected abstract void updateState(BlockState state, Level level, BlockPos pos, Block block);

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$allowDeepsteelPlatforms(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (isRailSupportingDeepsteelPlatform(level.getBlockState(pos.below()))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void dungeonnowloading$placeRailsInDeepsteelPlatformRailDirection(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState state = cir.getReturnValue();
        if (state == null) {
            return;
        }

        BlockState supportState = context.getLevel().getBlockState(context.getClickedPos().below());
        if (isDirectionLockingDeepsteelPlatformRail(supportState)) {
            cir.setReturnValue(state.setValue(this.getShapeProperty(), railShapeForPlatform(supportState)));
        }
    }

    @Inject(method = "updateDir", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$lockRailsOnDeepsteelPlatformRail(Level level, BlockPos pos, BlockState state, boolean forceUpdate, CallbackInfoReturnable<BlockState> cir) {
        BlockState supportState = level.getBlockState(pos.below());
        if (isDirectionLockingDeepsteelPlatformRail(supportState)) {
            cir.setReturnValue(lockRailShapeToPlatform(state, supportState));
        }
    }

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$keepRailDirectionLockedOnDeepsteelPlatformRail(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (level.isClientSide) {
            return;
        }

        BlockState supportState = level.getBlockState(pos.below());
        if (!isDirectionLockingDeepsteelPlatformRail(supportState)) {
            return;
        }

        BlockState lockedState = lockRailShapeToPlatform(state, supportState);
        if (lockedState != state) {
            level.setBlock(pos, lockedState, Block.UPDATE_ALL);
        }

        this.updateState(lockedState, level, pos, block);
        ci.cancel();
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void dungeonnowloading$placeRailLockedOnDeepsteelPlatformRail(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (oldState.is(state.getBlock())) {
            return;
        }

        BlockState supportState = level.getBlockState(pos.below());
        if (isDirectionLockingDeepsteelPlatformRail(supportState)) {
            BlockState lockedState = lockRailShapeToPlatform(state, supportState);
            if (lockedState != state) {
                level.setBlock(pos, lockedState, Block.UPDATE_ALL);
            }
            this.updateState(lockedState, level, pos, lockedState.getBlock());
            ci.cancel();
        }
    }

    @Redirect(
            method = "neighborChanged",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;shouldBeRemoved(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/properties/RailShape;)Z")
    )
    private boolean dungeonnowloading$keepRailsOnDeepsteelPlatforms(BlockPos pos, Level level, RailShape shape) {
        if (isRailSupportingDeepsteelPlatform(level.getBlockState(pos.below()))) {
            return false;
        }

        return shouldRemoveRail(pos, level, shape);
    }

    private boolean isRailSupportingDeepsteelPlatform(BlockState state) {
        return state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get());
    }

    private boolean isDirectionLockingDeepsteelPlatformRail(BlockState state) {
        return state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get())
                || state.is(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get());
    }

    private RailShape railShapeForPlatform(BlockState state) {
        return state.getValue(dev.hexnowloading.dungeonnowloading.block.DeepsteelPlatformBlock.FACING).getAxis() == net.minecraft.core.Direction.Axis.X
                ? RailShape.EAST_WEST
                : RailShape.NORTH_SOUTH;
    }

    private BlockState lockRailShapeToPlatform(BlockState state, BlockState supportState) {
        return state.setValue(this.getShapeProperty(), railShapeForPlatform(supportState));
    }

    private boolean shouldRemoveRail(BlockPos pos, Level level, RailShape shape) {
        if (!BaseRailBlock.canSupportRigidBlock(level, pos.below())) {
            return true;
        }
        if (shape == RailShape.ASCENDING_EAST && !BaseRailBlock.canSupportRigidBlock(level, pos.east())) {
            return true;
        }
        if (shape == RailShape.ASCENDING_WEST && !BaseRailBlock.canSupportRigidBlock(level, pos.west())) {
            return true;
        }
        if (shape == RailShape.ASCENDING_NORTH && !BaseRailBlock.canSupportRigidBlock(level, pos.north())) {
            return true;
        }
        return shape == RailShape.ASCENDING_SOUTH && !BaseRailBlock.canSupportRigidBlock(level, pos.south());
    }
}
