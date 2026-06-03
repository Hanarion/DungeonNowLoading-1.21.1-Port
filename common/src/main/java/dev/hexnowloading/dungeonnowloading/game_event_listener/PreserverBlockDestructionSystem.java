package dev.hexnowloading.dungeonnowloading.game_event_listener;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.hexnowloading.dungeonnowloading.block.MendingAuraBlock;
import dev.hexnowloading.dungeonnowloading.block.PreserverBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.MendingAuraBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.MendstoneChalkMarkBlockEntity;
import dev.hexnowloading.dungeonnowloading.block.entity.PreserverBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CInstantRepairOverlayPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public interface PreserverBlockDestructionSystem {

    public User getUser();

    public static class User {
        public static final Codec<User> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("cornerA").forGetter(User::getCornerA),
                BlockPos.CODEC.fieldOf("cornerB").forGetter(User::getCornerB),
                Direction.CODEC.fieldOf("facing").forGetter(User::getFacing)
        ).apply(instance, (cornerA, cornerB, facing) -> new User(BlockPos.ZERO, cornerA, cornerB, facing)));

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

        public boolean isInsideRegion(Level level, BlockPos eventBlockPos, BlockPos centerBlock) {
            BlockEntity blockEntity = level.getBlockEntity(centerBlock);
            if (!(blockEntity instanceof PreserverBlockEntity preserverBlockEntity)) return false;

            Direction nbtDirection = preserverBlockEntity.getUser().getFacing();

            BlockPos absCornerA = blockPos.offset(rotateOffset(level, centerBlock, cornerA, nbtDirection));
            BlockPos absCornerB = blockPos.offset(rotateOffset(level, centerBlock, cornerB, nbtDirection));

            int minX = Math.min(absCornerA.getX(), absCornerB.getX());
            int minY = Math.min(absCornerA.getY(), absCornerB.getY());
            int minZ = Math.min(absCornerA.getZ(), absCornerB.getZ());
            int maxX = Math.max(absCornerA.getX(), absCornerB.getX());
            int maxY = Math.max(absCornerA.getY(), absCornerB.getY());
            int maxZ = Math.max(absCornerA.getZ(), absCornerB.getZ());

            return eventBlockPos.getX() >= minX && eventBlockPos.getX() <= maxX &&
                    eventBlockPos.getY() >= minY && eventBlockPos.getY() <= maxY &&
                    eventBlockPos.getZ() >= minZ && eventBlockPos.getZ() <= maxZ;
        }


        public static BlockPos rotateOffset(Level level, BlockPos pos, BlockPos offset, Direction nbtFacing) {

            Direction propertyDirection = level.getBlockState(pos).getValue(BlockStateProperties.FACING);

            int propertyFacingIndex = switch (propertyDirection) {
                default -> 0;
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
            };

            int nbtFacingIndex = switch (nbtFacing) {
                default -> 0;
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
            };

            int facingDifference = propertyFacingIndex - nbtFacingIndex;

            return switch (facingDifference) {
                default -> offset;
                case 1, -3 -> offset.rotate(Rotation.CLOCKWISE_90);
                case -1, 3 -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
                case -2, 2 -> offset.rotate(Rotation.CLOCKWISE_180);
            };
        }
    }

    public static class Listener implements GameEventListener {
        private final PreserverBlockDestructionSystem system;
        private static long lastInstantRepairEffectTick = Long.MIN_VALUE;
        private static final Set<BlockPos> instantRepairEffectPositionsThisTick = new HashSet<>();

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

            if (!user.isInsideRegion(serverLevel, eventBlockPos, centerBlockPos)) {
                return false;
            }

            if (gameEvent == DNLGameEvents.PLAYER_BLOCK_DESTROY_EARLY.get()) {

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                    preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                    return false;
                }

                if (context.sourceEntity() instanceof Player player && player.getAbilities().instabuild) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                    BlockDestructionManager.cancel();
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                if (!ignoreBlockTransformation(serverLevel, eventBlockPos)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                if (tryInstantRepair(serverLevel, eventBlockPos, centerBlockPos, originalBlockState, gameEvent)) {
                    return true;
                }

                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                //BlockDestructionManager.cancel();
                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                //Note: Block Destruction cancel must be placed after placeMendingBlock to avoid the blockDestruction being reset before placing the mendstone block. Doesn't cause the bug when the player destroys the block, but located here just in case.
                BlockDestructionManager.cancel();

                storeMendingAuraBlock(serverLevel, eventBlockPos, originalBlockState, compoundTag);

                if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                    preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
                    serverLevel.playSound(null, centerBlockPos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }

                return true;
            }
            if (gameEvent == DNLGameEvents.BLOCK_DESTROY_EARLY.get()) {
                if (context.sourceEntity() instanceof Player) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).isAir()) {
                    return false;
                }

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof PreserverBlockEntity preserverBlock && preserverBlock.isPlayerPlaced(eventBlockPos)) {
                    preserverBlock.removePlayerPlacedBlock(eventBlockPos);
                    return false;
                }

                if (serverLevel.getBlockEntity(centerBlockPos) instanceof MendstoneChalkMarkBlockEntity && context.sourceEntity() != null && context.sourceEntity().getType().is(DNLTags.BOSSES_AND_RELATED_DESTRUCTIVES)) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof MendingAuraBlock) {
                    BlockDestructionManager.cancel();
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).getBlock() instanceof PreserverBlock) {
                    return false;
                }

                if (serverLevel.getBlockState(eventBlockPos).is(DNLTags.PRESERVER_IGNORE)) {
                    return false;
                }

                if (!ignoreBlockTransformation(serverLevel, eventBlockPos)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                if (tryInstantRepair(serverLevel, eventBlockPos, centerBlockPos, originalBlockState, gameEvent)) {
                    return true;
                }

                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                //Note: Block Destruction cancel must be placed after placeMendingBlock to avoid the blockDestruction being reset before placing the mendstone block.
                BlockDestructionManager.cancel();

                storeMendingAuraBlock(serverLevel, eventBlockPos, originalBlockState, compoundTag);

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
                    ExplosionDestructionManager.cancel();
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

                if (!ignoreBlockTransformation(serverLevel, eventBlockPos)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                if (tryInstantRepair(serverLevel, eventBlockPos, centerBlockPos, originalBlockState, gameEvent)) {
                    return true;
                }

                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                //Note: Block Destruction cancel must be placed after placeMendingBlock to avoid the blockDestruction being reset before placing the mendstone block.
                ExplosionDestructionManager.cancel();

                storeMendingAuraBlock(serverLevel, eventBlockPos, originalBlockState, compoundTag);

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

                if (!ignoreBlockTransformation(serverLevel, eventBlockPos)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                if (tryInstantRepair(serverLevel, eventBlockPos, centerBlockPos, originalBlockState, gameEvent)) {
                    return true;
                }

                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                BlockBurnManager.cancel();
                ContainerDropManager.cancel(eventBlockPos);

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                storeMendingAuraBlock(serverLevel, eventBlockPos, originalBlockState, compoundTag);

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

                if (!ignoreBlockTransformation(serverLevel, eventBlockPos)) {
                    return false;
                }

                BlockState originalBlockState = serverLevel.getBlockState(eventBlockPos);
                if (tryInstantRepair(serverLevel, eventBlockPos, centerBlockPos, originalBlockState, gameEvent)) {
                    return true;
                }

                BlockEntity originalBlockEntity = serverLevel.getBlockEntity(eventBlockPos);
                CompoundTag compoundTag = new CompoundTag();
                if (originalBlockEntity != null) {
                    compoundTag = originalBlockEntity.saveWithFullMetadata();
                }

                PistonPushManager.cancel();

                placeMendingBlock(serverLevel, originalBlockState, eventBlockPos, gameEvent);

                storeMendingAuraBlock(serverLevel, eventBlockPos, originalBlockState, compoundTag);

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

        private boolean ignoreBlockTransformation(ServerLevel serverLevel, BlockPos eventBlockPos) {
            BlockState state = serverLevel.getBlockState(eventBlockPos);
            return !state.isAir() && !state.canBeReplaced();
        }

        private boolean tryInstantRepair(ServerLevel serverLevel, BlockPos eventBlockPos, BlockPos centerBlockPos, BlockState originalBlockState, GameEvent gameEvent) {
            if (!usesInstantRepair(originalBlockState)) {
                return false;
            }

            cancelInstantRepairEvent(gameEvent, eventBlockPos);
            ContainerDropManager.cancel(eventBlockPos);

            serverLevel.setBlock(eventBlockPos, originalBlockState, Block.UPDATE_ALL);
            playInstantRepairEffects(serverLevel, eventBlockPos);

            if (serverLevel.getBlockState(centerBlockPos).getBlock() instanceof PreserverBlock preserverBlock) {
                preserverBlock.setLitPreserverBlock(serverLevel, centerBlockPos);
            }

            return true;
        }

        private boolean usesInstantRepair(BlockState state) {
            return state.is(DNLTags.PRESERVER_INSTANT_REPAIR);
        }

        private void cancelInstantRepairEvent(GameEvent gameEvent, BlockPos eventBlockPos) {
            if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get()) {
                ExplosionDestructionManager.cancel(eventBlockPos);
            } else if (gameEvent == DNLGameEvents.BLOCK_BURNED.get()) {
                BlockBurnManager.cancel();
            } else if (gameEvent == DNLGameEvents.BLOCK_PUSHED_EARLY.get()) {
                PistonPushManager.cancel();
            } else {
                BlockDestructionManager.cancel();
            }
        }

        private void storeMendingAuraBlock(ServerLevel serverLevel, BlockPos eventBlockPos, BlockState originalBlockState, CompoundTag compoundTag) {
            if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                BlockState storedBlockState = MendingAuraBlock.refreshStoredConnections(originalBlockState, serverLevel, eventBlockPos);
                blockEntity.setStoredBlock(storedBlockState, compoundTag);
                MendingAuraBlock.refreshNeighboringStoredConnections(serverLevel, eventBlockPos);
                blockEntity.syncToClients(serverLevel, serverLevel.getBlockState(eventBlockPos));
            }
        }

        private void placeMendingBlock(ServerLevel serverLevel, BlockState originalBlockState, BlockPos eventBlockPos, GameEvent gameEvent) {
            Map<BlockPos, BlockState> instantRepairNeighbors = collectInstantRepairNeighbors(serverLevel, eventBlockPos);

            //Note: For some reason, this setblock resets the BlockDestructionManager when the event block has attachable blocks like torches and vines, so the BlockDestructionManager.cancel need to be ran after this setblock.
            serverLevel.setBlock(eventBlockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);

            BlockState mendingAuraState = MendingAuraBlock.configureForStoredBlock(DNLBlocks.MENDING_AURA.get().defaultBlockState(), originalBlockState);

            serverLevel.setBlock(eventBlockPos, mendingAuraState, Block.UPDATE_CLIENTS);
            if (serverLevel.getBlockEntity(eventBlockPos) instanceof MendingAuraBlockEntity blockEntity) {
                BlockState storedState = MendingAuraBlock.refreshStoredConnections(originalBlockState, serverLevel, eventBlockPos);
                blockEntity.setStoredBlock(storedState, new CompoundTag());
            }

            restoreInstantRepairNeighbors(serverLevel, instantRepairNeighbors, gameEvent);

            Block block = serverLevel.getBlockState(eventBlockPos).getBlock();
            if (block instanceof MendingAuraBlock mendingAuraBlock) {
                mendingAuraBlock.startRestoration(serverLevel, eventBlockPos);
            }
        }

        private Map<BlockPos, BlockState> collectInstantRepairNeighbors(ServerLevel serverLevel, BlockPos eventBlockPos) {
            Map<BlockPos, BlockState> states = new HashMap<>();

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = eventBlockPos.relative(direction);
                BlockState neighborState = serverLevel.getBlockState(neighborPos);
                if (usesInstantRepair(neighborState)) {
                    states.put(neighborPos.immutable(), neighborState);
                }
            }

            return states;
        }

        private void restoreInstantRepairNeighbors(ServerLevel serverLevel, Map<BlockPos, BlockState> states, GameEvent gameEvent) {
            states.forEach((pos, state) -> {
                if (!serverLevel.getBlockState(pos).equals(state)) {
                    serverLevel.setBlock(pos, state, Block.UPDATE_ALL);
                    if (gameEvent == DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION.get()) {
                        ExplosionDestructionManager.markBlockForUpdate(pos);
                    }
                    playInstantRepairEffects(serverLevel, pos);
                }
            });
        }

        private void playInstantRepairEffects(ServerLevel serverLevel, BlockPos pos) {
            if (!markInstantRepairEffect(serverLevel, pos)) {
                return;
            }

            PreserverBlockEntity.spawnPopBurst(serverLevel, getShapeCenter(serverLevel, pos));
            serverLevel.playSound(null, pos, DNLSounds.MENDING_AURA_POP.get(), SoundSource.BLOCKS, 1.0F, 1.2F);
            S2CInstantRepairOverlayPacket packet = new S2CInstantRepairOverlayPacket(pos);
            for (Player player : serverLevel.players()) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && player.blockPosition().closerThan(pos, 128.0D)) {
                    Services.NETWORK.sendToPlayer(packet, serverPlayer);
                }
            }
        }

        private Vec3 getShapeCenter(ServerLevel serverLevel, BlockPos pos) {
            BlockState state = serverLevel.getBlockState(pos);
            VoxelShape shape = state.getShape(serverLevel, pos, CollisionContext.empty());
            if (shape.isEmpty()) {
                return Vec3.atCenterOf(pos);
            }

            AABB bounds = shape.bounds();
            return new Vec3(
                    pos.getX() + (bounds.minX + bounds.maxX) * 0.5D,
                    pos.getY() + (bounds.minY + bounds.maxY) * 0.5D,
                    pos.getZ() + (bounds.minZ + bounds.maxZ) * 0.5D
            );
        }

        private boolean markInstantRepairEffect(ServerLevel serverLevel, BlockPos pos) {
            long gameTime = serverLevel.getGameTime();
            if (gameTime != lastInstantRepairEffectTick) {
                instantRepairEffectPositionsThisTick.clear();
                lastInstantRepairEffectTick = gameTime;
            }

            return instantRepairEffectPositionsThisTick.add(pos.immutable());
        }
    }
}
