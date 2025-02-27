package dev.hexnowloading.dungeonnowloading.game_event_listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.PreserverBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import dev.hexnowloading.dungeonnowloading.util.event_managers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public interface PreserverBlockDestructionSystem {

    public User getUser();

    public static class User {
        public static final Codec<User> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(User::getBlockPos),
                BlockPos.CODEC.fieldOf("cornerA").forGetter(User::getCornerA),
                BlockPos.CODEC.fieldOf("cornerB").forGetter(User::getCornerB),
                Direction.CODEC.fieldOf("facing").forGetter(User::getFacing)
        ).apply(instance, User::new));

        private final BlockPos blockPos;
        private BlockPos cornerA;
        private BlockPos cornerB;
        private Direction direction;

        public User(BlockPos blockPos, BlockPos cornerA, BlockPos cornerB, Direction direction) {
            this.blockPos = blockPos;
            this.cornerA = cornerA;
            this.cornerB = cornerB;
            this.direction = direction;
        }

        public BlockPos getCornerA() {
            return cornerA;
        }

        public void setCornerA(BlockPos blockPos) {
            this.cornerA = blockPos;
        }

        public void setCornerB(BlockPos blockPos) {
            this.cornerB = blockPos;
        }

        public BlockPos getCornerB() {
            return cornerB;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public PositionSource getPositionSource() {
            return new BlockPositionSource(blockPos);
        }

        public Direction getFacing() {
            return direction;
        }

        public void setFacing(Direction direction) {
            this.direction = direction;
        }

        public boolean isInsideRegion(BlockPos pos, Direction blockFacing) {
            BlockPos absCornerA = blockPos.offset(rotateOffset(cornerA, blockFacing));
            BlockPos absCornerB = blockPos.offset(rotateOffset(cornerB, blockFacing));

            int minX = Math.min(absCornerA.getX(), absCornerB.getX());
            int minY = Math.min(absCornerA.getY(), absCornerB.getY());
            int minZ = Math.min(absCornerA.getZ(), absCornerB.getZ());
            int maxX = Math.max(absCornerA.getX(), absCornerB.getX());
            int maxY = Math.max(absCornerA.getY(), absCornerB.getY());
            int maxZ = Math.max(absCornerA.getZ(), absCornerB.getZ());

            return pos.getX() >= minX && pos.getX() <= maxX &&
                    pos.getY() >= minY && pos.getY() <= maxY &&
                    pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }


        public static BlockPos rotateOffset(BlockPos offset, Direction facing) {
            return switch (facing) {
                case NORTH -> new BlockPos(offset.getX(), offset.getY(), offset.getZ());  // No change
                case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ()); // Mirror XZ
                case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());   // Rotate +90°
                case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());   // Rotate -90°
                default -> offset;
            };
        }
    }

    public static class Listener implements GameEventListener {
        private final PreserverBlockDestructionSystem system;

        public Listener(PreserverBlockDestructionSystem system) {
            this.system = system;
        }

        public BlockPos getBlockPos() {
            return system.getUser().getBlockPos();
        }

        @Override
        public PositionSource getListenerSource() {
            return system.getUser().getPositionSource();
        }

        @Override
        public int getListenerRadius() {
            BlockPos absCornerA = system.getUser().getBlockPos().offset(system.getUser().getCornerA());
            BlockPos absCornerB = system.getUser().getBlockPos().offset(system.getUser().getCornerB());

            int maxDistance = Math.max(
                    Math.abs(absCornerA.getX() - absCornerB.getX()),
                    Math.max(Math.abs(absCornerA.getY() - absCornerB.getY()),
                            Math.abs(absCornerA.getZ() - absCornerB.getZ()))
            );

            return squareRegionCalculation(maxDistance);
        }


        public static int squareRegionCalculation(int centerToSideDistance) {
            return (int) Math.ceil((centerToSideDistance) * Math.sqrt(3));
        }

        public static int reverseSquareRegionCalculation(int y) {
            return (int) Math.floor((y / Math.sqrt(3)));
        }

        @Override
        public boolean handleGameEvent(ServerLevel serverLevel, GameEvent gameEvent, GameEvent.Context context, Vec3 pos) {

            BlockPos eventBlockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
            //int radius = reverseSquareRegionCalculation(this.getListenerRadius());

            Vec3 centerPos = this.getListenerSource().getPosition(serverLevel).orElseThrow();
            BlockPos centerBlockPos = new BlockPos((int) Math.floor(centerPos.x), (int) Math.floor(centerPos.y), (int) Math.floor(centerPos.z));

            User user = system.getUser();

            if (!user.isInsideRegion(eventBlockPos, serverLevel.getBlockState(centerBlockPos).getValue(BlockStateProperties.FACING))) {
                return false;
            }

            if (gameEvent == DNLGameEvents.BLOCK_DESTROY_EARLY.get()) {

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                    preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                    return false;
                }

                if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                BlockDestructionManager.cancel();
                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                    blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    blockEntity.setPersistent(false);
                }

                if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            }
            if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get()) {
                if (serverLevel.getBlockState(eventBlockPos).isAir()) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    ExplosionDestructionManager.cancel();
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    return false;
                }

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                    preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                ExplosionDestructionManager.cancel();
                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                    blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    blockEntity.setPersistent(false);
                }

                if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            }
            if (gameEvent == DNLGameEvents.BLOCK_BURNED.get()) {
                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                    preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE_ON_FIRE)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                BlockBurnManager.cancel();
                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                    blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    blockEntity.setPersistent(false);
                }

                if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            }
            if (gameEvent == DNLGameEvents.BLOCK_PUSHED_EARLY.get()) {

                if (serverLevel.getBlockState(eventBlockPos).isAir()) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                PistonPushManager.cancel();

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                    blockEntity.setStoredBlock(originalBlockState, compoundTag);
                    blockEntity.setPersistent(false);
                }

                if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            }
            if (gameEvent == GameEvent.BLOCK_PLACE) {
                if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                    return false;
                }

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity blockEntity) {
                    blockEntity.addPlayerPlacedBlock(eventBlockPos);
                }
                return true;
            }

            return false;
        }

        private void placeMendingBlock(ServerLevel serverLevel, BlockState originalBlockState, BlockPos eventBlockPos, GameEvent gameEvent) {
            serverLevel.setBlock(eventBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);

            BlockState mendingAuraState;

            if (originalBlockState.getBlock() instanceof StairBlock) {
                mendingAuraState = DNLBlocks.MENDING_AURA_STAIRS.get().defaultBlockState()
                        .setValue(StairBlock.FACING, originalBlockState.getValue(StairBlock.FACING))
                        .setValue(StairBlock.HALF, originalBlockState.getValue(StairBlock.HALF))
                        .setValue(StairBlock.SHAPE, originalBlockState.getValue(StairBlock.SHAPE))
                        .setValue(BlockStateProperties.WATERLOGGED, originalBlockState.getValue(BlockStateProperties.WATERLOGGED));

            }
            else if (originalBlockState.getBlock() instanceof SlabBlock) {
                mendingAuraState = DNLBlocks.MENDING_AURA_SLAB.get().defaultBlockState()
                        .setValue(SlabBlock.TYPE, originalBlockState.getValue(SlabBlock.TYPE))
                        .setValue(BlockStateProperties.WATERLOGGED, originalBlockState.getValue(BlockStateProperties.WATERLOGGED));

            }
            else if (originalBlockState.getBlock() instanceof FenceBlock) {
                mendingAuraState = DNLBlocks.MENDING_AURA_FENCE.get().defaultBlockState()
                        .setValue(BlockStateProperties.NORTH, originalBlockState.getValue(BlockStateProperties.NORTH))
                        .setValue(BlockStateProperties.SOUTH, originalBlockState.getValue(BlockStateProperties.SOUTH))
                        .setValue(BlockStateProperties.EAST, originalBlockState.getValue(BlockStateProperties.EAST))
                        .setValue(BlockStateProperties.WEST, originalBlockState.getValue(BlockStateProperties.WEST))
                        .setValue(BlockStateProperties.WATERLOGGED, originalBlockState.getValue(BlockStateProperties.WATERLOGGED));

            }
            else if (originalBlockState.getBlock() instanceof WallBlock) {
                mendingAuraState = DNLBlocks.MENDING_AURA_WALL.get().defaultBlockState()
                        .setValue(BlockStateProperties.UP, originalBlockState.getValue(BlockStateProperties.UP))
                        .setValue(BlockStateProperties.NORTH_WALL, originalBlockState.getValue(BlockStateProperties.NORTH_WALL))
                        .setValue(BlockStateProperties.SOUTH_WALL, originalBlockState.getValue(BlockStateProperties.SOUTH_WALL))
                        .setValue(BlockStateProperties.EAST_WALL, originalBlockState.getValue(BlockStateProperties.EAST_WALL))
                        .setValue(BlockStateProperties.WEST_WALL, originalBlockState.getValue(BlockStateProperties.WEST_WALL))
                        .setValue(BlockStateProperties.WATERLOGGED, originalBlockState.getValue(BlockStateProperties.WATERLOGGED));

            }
            else {
                mendingAuraState = DNLBlocks.MENDING_AURA.get().defaultBlockState();

            }

            serverLevel.setBlock(eventBlockPos, mendingAuraState, Block.UPDATE_CLIENTS);


            /*if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get() &&
                    (mendingAuraState.getBlock() instanceof MendingAuraFenceBlock ||
                    mendingAuraState.getBlock() instanceof MendingAuraWallBlock)) {

            }*/

            /*if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get()) {
                Block mendingAuraBlock = mendingAuraState.getBlock();

                if (mendingAuraBlock instanceof MendingAuraSlabBlock && mendingAuraState.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
                    return;
                }

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    fixConnection(serverLevel, eventBlockPos, direction);
                }
            }*/

        }

        private void fixConnection(ServerLevel serverLevel, BlockPos eventBlockPos, Direction direction) {
            BlockPos neighborPos = eventBlockPos.relative(direction);
            BlockState neighborState = serverLevel.getBlockState(neighborPos);
            Block block = neighborState.getBlock();

            if (block instanceof FenceBlock fenceBlock) {
                serverLevel.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                serverLevel.setBlock(neighborPos, fenceBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
            }

            if (block instanceof WallBlock wallBlock) {

                serverLevel.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                serverLevel.setBlock(neighborPos, wallBlock.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
