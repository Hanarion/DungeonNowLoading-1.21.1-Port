package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.block.WispwardLanternBlock;
import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import dev.hexnowloading.dungeonnowloading.entity.monster.WispwardLanternCartEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SpecialItemEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.MendingFadeParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class WispwardChestBlockEntity extends RandomizableContainerBlockEntity implements MenuProvider, ZoneReceiverBlockEntity {
    public static final ResourceLocation DEFAULT_LOOT_TABLE = ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "chests/wispward_chest");
    private static final int LANTERN_NOTIFY_CHUNK_RADIUS = 2;
    private static final int FLAME_TRAVEL_TICKS = 40;
    private static final int FLAME_EMIT_INTERVAL_TICKS = 4;
    private static final int LOOT_SPEW_DELAY_TICKS = 40;
    private static final int LOOT_SPEW_INTERVAL_TICKS = 3;
    private static final String LANTERN_OFFSETS_TAG = "LanternOffsets";

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private BlockPos cornerA = BlockPos.ZERO;
    private BlockPos cornerB = BlockPos.ZERO;
    private Direction authoredFacing = Direction.NORTH;
    private final Set<BlockPos> lanternOffsets = new LinkedHashSet<>();
    private boolean lanternIndexInitialized = false;
    private int cachedLanterns = 0;
    private int cachedLitLanterns = 0;
    private int requiredLitLanterns = -1;
    private ResourceLocation configuredLootTable = DEFAULT_LOOT_TABLE;
    private int flameStreamCooldown = 0;
    private int lootSpewDelayTicks = -1;
    private int lootSpewCooldown = 0;
    private boolean lootSpewing = false;
    private boolean lootSpewed = false;

    public WispwardChestBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.WISPWARD_CHEST.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WispwardChestBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        blockEntity.refreshLanternCache(server);

        blockEntity.tickLitLanternFlames(server);
        blockEntity.tickLootSpew(server);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.dungeonnowloading.wispward_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        this.unpackLootTable(inventory.player);
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public void setRegion(BlockPos cornerAWorld, BlockPos cornerBWorld, Direction authoredFacing) {
        BlockPos center = this.getBlockPos();
        this.cornerA = cornerAWorld.subtract(center);
        this.cornerB = cornerBWorld.subtract(center);
        this.authoredFacing = authoredFacing;
        if (this.level instanceof ServerLevel server) {
            this.rebuildLanternIndex(server);
        }
        this.setChanged();
    }

    public boolean hasRequiredLanternsLit() {
        return this.getLanternProgress() >= 1.0F;
    }

    public float getLanternProgress() {
        if (this.lootSpewed) {
            return 1.0F;
        }

        int required = this.getEffectiveRequiredLitLanterns();
        return required <= 0 ? 1.0F : Math.min(1.0F, (float) this.cachedLitLanterns / (float) required);
    }

    public int getRequiredLitLanterns() {
        return this.requiredLitLanterns;
    }

    public int getCachedLanterns() {
        return this.cachedLanterns;
    }

    public int getEffectiveRequiredLitLanterns() {
        return this.requiredLitLanterns > 0 ? this.requiredLitLanterns : this.cachedLanterns;
    }

    public int cycleRequiredLitLanterns(ServerLevel level, boolean backwards) {
        this.refreshLanternCache(level);
        int max = Math.max(1, this.cachedLanterns);
        int current = this.requiredLitLanterns > 0 ? this.requiredLitLanterns : max;

        current += backwards ? -1 : 1;
        if (current < 1) {
            current = max;
        } else if (current > max) {
            current = 1;
        }

        this.requiredLitLanterns = current;
        this.refreshLanternCache(level);
        this.setChanged();
        this.syncToClients();
        return this.requiredLitLanterns;
    }

    public ResourceLocation getConfiguredLootTable() {
        return this.configuredLootTable;
    }

    public void applyCreativeConfig(ServerLevel level, ResourceLocation lootTable, int requiredLitLanterns) {
        this.configuredLootTable = lootTable == null ? DEFAULT_LOOT_TABLE : lootTable;
        this.requiredLitLanterns = Math.max(1, requiredLitLanterns);
        this.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, this.configuredLootTable), level.random.nextLong());
        this.refreshLanternCache(level);
        this.setChanged();
        this.syncToClients();
    }

    public void resetReward(ServerLevel level) {
        this.lootSpewing = false;
        this.lootSpewed = false;
        this.lootSpewDelayTicks = -1;
        this.lootSpewCooldown = 0;
        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }
        this.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, this.configuredLootTable), level.random.nextLong());
        this.refreshLanternCache(level);
        this.setChanged();
        this.syncToClients();
    }

    public void initializeLootTable(ServerLevel level) {
        this.configuredLootTable = DEFAULT_LOOT_TABLE;
        this.setLootTable(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, this.configuredLootTable), level.random.nextLong());
        this.setChanged();
        this.syncToClients();
    }

    public void destroySavedLanterns(ServerLevel level) {
        this.replacePreserversInRegion(level);

        for (BlockPos offset : Set.copyOf(this.lanternOffsets)) {
            BlockPos lanternPos = this.storedOffsetToWorld(level, offset);
            BlockState state = level.getBlockState(lanternPos);
            if (isWispwardLantern(state)) {
                level.destroyBlock(lanternPos, false);
            }
        }

        for (WispwardLanternCartEntity cart : this.getWispwardLanternCartsInRegion(level)) {
            cart.discard();
        }

        this.lanternOffsets.clear();
        this.cachedLanterns = 0;
        this.cachedLitLanterns = 0;
        this.setChanged();
        this.syncToClients();
    }

    private void replacePreserversInRegion(ServerLevel level) {
        RegionBounds bounds = this.getRegionBounds(level);
        Block preserver = DNLBlocks.STONE_PRESERVER.get();
        BlockState replaceState = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = bounds.minY; y <= bounds.maxY; y++) {
            for (int x = bounds.minX; x <= bounds.maxX; x++) {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                    cursor.set(x, y, z);
                    if (!level.getBlockState(cursor).is(preserver)) {
                        continue;
                    }

                    BlockPos preserverPos = cursor.immutable();
                    PreserverBlockEntity.spawnPopBurst(level, preserverPos);
                    level.setBlock(preserverPos, replaceState, Block.UPDATE_ALL);
                    level.playSound(null, preserverPos, DNLSounds.DURITE_QUELLER_REPLACE_PRESERVER.get(), SoundSource.BLOCKS, 1.0F, 0.5F);
                }
            }
        }
    }

    public void refreshLanternCache(ServerLevel level) {
        if (!this.lanternIndexInitialized) {
            this.rebuildLanternIndex(level);
            return;
        }

        this.refreshLanternCacheFromIndex(level);
    }

    public void rebuildLanternIndex(ServerLevel level) {
        this.lanternOffsets.clear();

        BlockPos center = this.getBlockPos();
        BlockPos absCornerA = center.offset(rotateOffset(level, center, this.cornerA, this.authoredFacing));
        BlockPos absCornerB = center.offset(rotateOffset(level, center, this.cornerB, this.authoredFacing));

        int minX = Math.min(absCornerA.getX(), absCornerB.getX());
        int minY = Math.min(absCornerA.getY(), absCornerB.getY());
        int minZ = Math.min(absCornerA.getZ(), absCornerB.getZ());
        int maxX = Math.max(absCornerA.getX(), absCornerB.getX());
        int maxY = Math.max(absCornerA.getY(), absCornerB.getY());
        int maxZ = Math.max(absCornerA.getZ(), absCornerB.getZ());

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (isWispwardLantern(state)) {
                        this.lanternOffsets.add(this.worldToStoredOffset(level, cursor.immutable()));
                    }
                }
            }
        }

        this.lanternIndexInitialized = true;
        this.refreshLanternCacheFromIndex(level);
    }

    private void refreshLanternCacheFromIndex(ServerLevel level) {
        int lanterns = 0;
        int litLanterns = 0;
        boolean changedOffsets = false;

        Iterator<BlockPos> iterator = this.lanternOffsets.iterator();
        while (iterator.hasNext()) {
            BlockPos lanternPos = this.storedOffsetToWorld(level, iterator.next());
            BlockState state = level.getBlockState(lanternPos);
            if (!isWispwardLantern(state)) {
                iterator.remove();
                changedOffsets = true;
                continue;
            }

            lanterns++;
            if (state.getValue(WispwardLanternBlock.LIT)) {
                litLanterns++;
            }
        }

        for (WispwardLanternCartEntity cart : this.getWispwardLanternCartsInRegion(level)) {
            lanterns++;
            if (cart.isLit()) {
                litLanterns++;
            }
        }

        boolean hasRequiredLitLanterns = lanterns > 0 && litLanterns >= this.getRequiredLitLanternsFor(lanterns);
        if (hasRequiredLitLanterns) {
            this.lockTimedLanterns(level);
            this.scheduleLootSpew();
        }

        if (this.cachedLanterns != lanterns || this.cachedLitLanterns != litLanterns || changedOffsets) {
            this.cachedLanterns = lanterns;
            this.cachedLitLanterns = litLanterns;
            this.setChanged();
            this.syncToClients();
        }
    }

    private void scheduleLootSpew() {
        if (!this.lootSpewed && this.lootSpewDelayTicks < 0) {
            this.lootSpewDelayTicks = LOOT_SPEW_DELAY_TICKS;
            this.setChanged();
        }
    }

    private void tickLootSpew(ServerLevel level) {
        if (this.lootSpewed) {
            return;
        }

        if (this.lootSpewing) {
            if (this.lootSpewCooldown > 0) {
                this.lootSpewCooldown--;
                this.setChanged();
                return;
            }

            this.spewNextLootStack(level);
            return;
        }

        if (this.lootSpewDelayTicks < 0) {
            return;
        }

        this.lootSpewDelayTicks--;
        if (this.lootSpewDelayTicks <= 0) {
            this.startLootSpew();
        } else {
            this.setChanged();
        }
    }

    private void startLootSpew() {
        this.unpackLootTable(null);
        this.lootSpewing = true;
        this.lootSpewDelayTicks = -1;
        this.lootSpewCooldown = 0;
        this.setChanged();
    }

    private void spewNextLootStack(ServerLevel level) {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                this.spewLootStack(level, stack);
                this.items.set(i, ItemStack.EMPTY);
                this.lootSpewCooldown = LOOT_SPEW_INTERVAL_TICKS - 1;
                this.setChanged();
                return;
            }
        }

        this.lootSpewing = false;
        this.lootSpewed = true;
        this.lootSpewCooldown = 0;
        this.destroySavedLanterns(level);
        this.setChanged();
        this.syncToClients();
    }

    private void spewLootStack(ServerLevel level, ItemStack stack) {
        Direction[] sides = new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        Direction side = sides[level.random.nextInt(sides.length)];
        Vec3 forward = new Vec3(side.getStepX(), 0.0D, side.getStepZ());
        Vec3 tangent = new Vec3(-side.getStepZ(), 0.0D, side.getStepX());
        double angle = (level.random.nextDouble() - 0.5D) * 0.65D;
        double strength = 0.12D + level.random.nextDouble() * 0.12D;
        Vec3 horizontalVelocity = forward.scale(Math.cos(angle)).add(tangent.scale(Math.sin(angle))).scale(strength);
        Vec3 spawn = Vec3.atCenterOf(this.getBlockPos())
                .add(side.getStepX() * 0.72D, 0.05D, side.getStepZ() * 0.72D)
                .add(tangent.scale((level.random.nextDouble() - 0.5D) * 0.22D));
        double yVelocity = 0.10D + level.random.nextDouble() * 0.12D;

        SpecialItemEntity itemEntity = new SpecialItemEntity(level, spawn.x, spawn.y, spawn.z, stack.copy(), horizontalVelocity.x, yVelocity, horizontalVelocity.z);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
        level.sendParticles(ParticleTypes.POOF, spawn.x, spawn.y, spawn.z, 4, 0.08D, 0.08D, 0.08D, 0.01D);
        level.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.25F, 0.8F + level.random.nextFloat() * 0.3F);
    }

    private void tickLitLanternFlames(ServerLevel level) {
        if (this.lootSpewed) {
            this.flameStreamCooldown = 0;
            return;
        }

        if (this.cachedLitLanterns <= 0) {
            this.flameStreamCooldown = 0;
            return;
        }

        if (this.flameStreamCooldown <= 0) {
            this.spawnLitLanternFlames(level);
            this.flameStreamCooldown = FLAME_EMIT_INTERVAL_TICKS;
        }

        this.flameStreamCooldown--;
    }

    private void spawnLitLanternFlames(ServerLevel level) {
        Vec3 target = Vec3.atCenterOf(this.getBlockPos()).add(0.0D, 0.25D, 0.0D);

        for (BlockPos offset : this.lanternOffsets) {
            BlockPos lanternPos = this.storedOffsetToWorld(level, offset);
            BlockState state = level.getBlockState(lanternPos);
            if (!isWispwardLantern(state) || !state.getValue(WispwardLanternBlock.LIT)) {
                continue;
            }

            Vec3 start = Vec3.atCenterOf(lanternPos).add(0.0D, 0.05D, 0.0D);
            Vec3 velocity = target.subtract(start).scale(1.0D / FLAME_TRAVEL_TICKS);

            double jitterX = (level.random.nextDouble() - 0.5D) * 0.18D;
            double jitterY = (level.random.nextDouble() - 0.5D) * 0.18D;
            double jitterZ = (level.random.nextDouble() - 0.5D) * 0.18D;
            MendingFadeParticleType.Data data = new MendingFadeParticleType.Data(
                    DNLParticleTypes.WISPWARD_FLAME_TRAVEL_PARTICLE.get(),
                    (float) velocity.x,
                    (float) velocity.y,
                    (float) velocity.z,
                    4,
                    8,
                    FLAME_TRAVEL_TICKS
            );
            level.sendParticles(data, start.x + jitterX, start.y + jitterY, start.z + jitterZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        for (WispwardLanternCartEntity cart : this.getWispwardLanternCartsInRegion(level)) {
            if (!cart.isLit()) {
                continue;
            }

            Vec3 start = cart.getLightPosition();
            Vec3 velocity = target.subtract(start).scale(1.0D / FLAME_TRAVEL_TICKS);

            double jitterX = (level.random.nextDouble() - 0.5D) * 0.18D;
            double jitterY = (level.random.nextDouble() - 0.5D) * 0.18D;
            double jitterZ = (level.random.nextDouble() - 0.5D) * 0.18D;
            MendingFadeParticleType.Data data = new MendingFadeParticleType.Data(
                    DNLParticleTypes.WISPWARD_FLAME_TRAVEL_PARTICLE.get(),
                    (float) velocity.x,
                    (float) velocity.y,
                    (float) velocity.z,
                    4,
                    8,
                    FLAME_TRAVEL_TICKS
            );
            level.sendParticles(data, start.x + jitterX, start.y + jitterY, start.z + jitterZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void lockTimedLanterns(ServerLevel level) {
        for (BlockPos offset : this.lanternOffsets) {
            BlockPos lanternPos = this.storedOffsetToWorld(level, offset);
            BlockState state = level.getBlockState(lanternPos);
            if (state.is(DNLBlocks.TIMED_WISPWARD_LANTERN.get())
                    && state.getValue(WispwardLanternBlock.LIT)
                    && level.getBlockEntity(lanternPos) instanceof WispwardLanternBlockEntity lantern) {
                lantern.lockLit();
            }
        }

        for (WispwardLanternCartEntity cart : this.getWispwardLanternCartsInRegion(level)) {
            if (cart.isTimed() && cart.isLit()) {
                cart.lockLit();
            }
        }
    }

    private void addLanternIfInRegion(ServerLevel level, BlockPos lanternPos) {
        if (!this.lanternIndexInitialized) {
            this.rebuildLanternIndex(level);
            return;
        }

        if (this.isInRegion(level, lanternPos) && this.lanternOffsets.add(this.worldToStoredOffset(level, lanternPos))) {
            this.refreshLanternCacheFromIndex(level);
        }
    }

    private void removeLantern(ServerLevel level, BlockPos lanternPos) {
        if (!this.lanternIndexInitialized) {
            return;
        }

        if (this.lanternOffsets.remove(this.worldToStoredOffset(level, lanternPos))) {
            this.refreshLanternCacheFromIndex(level);
        }
    }

    private boolean isInRegion(Level level, BlockPos pos) {
        RegionBounds bounds = this.getRegionBounds(level);

        return pos.getX() >= bounds.minX && pos.getX() <= bounds.maxX
                && pos.getY() >= bounds.minY && pos.getY() <= bounds.maxY
                && pos.getZ() >= bounds.minZ && pos.getZ() <= bounds.maxZ;
    }

    private java.util.List<WispwardLanternCartEntity> getWispwardLanternCartsInRegion(ServerLevel level) {
        RegionBounds bounds = this.getRegionBounds(level);
        AABB searchArea = new AABB(bounds.minX, bounds.minY, bounds.minZ, bounds.maxX + 1.0D, bounds.maxY + 1.0D, bounds.maxZ + 1.0D).inflate(1.0D);
        return level.getEntitiesOfClass(WispwardLanternCartEntity.class, searchArea, cart -> !cart.isRemoved() && this.isInRegion(level, cart.blockPosition()));
    }

    private RegionBounds getRegionBounds(Level level) {
        BlockPos center = this.getBlockPos();
        BlockPos absCornerA = center.offset(rotateOffset(level, center, this.cornerA, this.authoredFacing));
        BlockPos absCornerB = center.offset(rotateOffset(level, center, this.cornerB, this.authoredFacing));

        return new RegionBounds(
                Math.min(absCornerA.getX(), absCornerB.getX()),
                Math.min(absCornerA.getY(), absCornerB.getY()),
                Math.min(absCornerA.getZ(), absCornerB.getZ()),
                Math.max(absCornerA.getX(), absCornerB.getX()),
                Math.max(absCornerA.getY(), absCornerB.getY()),
                Math.max(absCornerA.getZ(), absCornerB.getZ())
        );
    }

    private BlockPos storedOffsetToWorld(Level level, BlockPos offset) {
        BlockPos center = this.getBlockPos();
        return center.offset(rotateOffset(level, center, offset, this.authoredFacing));
    }

    private BlockPos worldToStoredOffset(Level level, BlockPos worldPos) {
        BlockPos center = this.getBlockPos();
        return unrotateOffset(level, center, worldPos.subtract(center), this.authoredFacing);
    }

    public static void notifyLanternPlaced(ServerLevel level, BlockPos lanternPos) {
        forNearbyWispwardChests(level, lanternPos, chest -> chest.addLanternIfInRegion(level, lanternPos));
    }

    public static void notifyLanternRemoved(ServerLevel level, BlockPos lanternPos) {
        forNearbyWispwardChests(level, lanternPos, chest -> chest.removeLantern(level, lanternPos));
    }

    public static void notifyLanternChanged(ServerLevel level, BlockPos lanternPos) {
        forNearbyWispwardChests(level, lanternPos, chest -> {
            if (!chest.lanternIndexInitialized || chest.lanternOffsets.contains(chest.worldToStoredOffset(level, lanternPos)) || chest.isInRegion(level, lanternPos)) {
                chest.addLanternIfInRegion(level, lanternPos);
                chest.refreshLanternCacheFromIndex(level);
            }
        });
    }

    private static void forNearbyWispwardChests(ServerLevel level, BlockPos pos, java.util.function.Consumer<WispwardChestBlockEntity> consumer) {
        int chunkX = net.minecraft.core.SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = net.minecraft.core.SectionPos.blockToSectionCoord(pos.getZ());

        for (int x = chunkX - LANTERN_NOTIFY_CHUNK_RADIUS; x <= chunkX + LANTERN_NOTIFY_CHUNK_RADIUS; x++) {
            for (int z = chunkZ - LANTERN_NOTIFY_CHUNK_RADIUS; z <= chunkZ + LANTERN_NOTIFY_CHUNK_RADIUS; z++) {
                for (BlockEntity blockEntity : java.util.List.copyOf(level.getChunk(x, z).getBlockEntities().values())) {
                    if (blockEntity instanceof WispwardChestBlockEntity chest) {
                        consumer.accept(chest);
                    }
                }
            }
        }
    }

    public static BlockPos rotateOffset(Level level, BlockPos pos, BlockPos offset, Direction authoredFacing) {
        BlockState state = level.getBlockState(pos);
        Direction currentFacing = state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.NORTH;

        int currentFacingIndex = switch (currentFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int authoredFacingIndex = switch (authoredFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int facingDifference = currentFacingIndex - authoredFacingIndex;
        return switch (facingDifference) {
            default -> offset;
            case 1, -3 -> offset.rotate(Rotation.CLOCKWISE_90);
            case -1, 3 -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
            case -2, 2 -> offset.rotate(Rotation.CLOCKWISE_180);
        };
    }

    public static BlockPos unrotateOffset(Level level, BlockPos pos, BlockPos offset, Direction authoredFacing) {
        BlockState state = level.getBlockState(pos);
        Direction currentFacing = state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING) : Direction.NORTH;

        int currentFacingIndex = switch (currentFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int authoredFacingIndex = switch (authoredFacing) {
            default -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };

        int facingDifference = currentFacingIndex - authoredFacingIndex;
        return switch (facingDifference) {
            default -> offset;
            case 1, -3 -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
            case -1, 3 -> offset.rotate(Rotation.CLOCKWISE_90);
            case -2, 2 -> offset.rotate(Rotation.CLOCKWISE_180);
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }

        this.saveZone(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }

        this.loadZone(tag);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveZone(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void saveZone(CompoundTag tag) {
        tag.putInt("cornerAx", this.cornerA.getX());
        tag.putInt("cornerAy", this.cornerA.getY());
        tag.putInt("cornerAz", this.cornerA.getZ());
        tag.putInt("cornerBx", this.cornerB.getX());
        tag.putInt("cornerBy", this.cornerB.getY());
        tag.putInt("cornerBz", this.cornerB.getZ());
        tag.putString("authoredFacing", this.authoredFacing.getName());
        tag.putBoolean("lanternIndexInitialized", this.lanternIndexInitialized);
        tag.putInt("cachedLanterns", this.cachedLanterns);
        tag.putInt("cachedLitLanterns", this.cachedLitLanterns);
        tag.putInt("requiredLitLanterns", this.requiredLitLanterns);
        tag.putString("configuredLootTable", this.configuredLootTable.toString());
        tag.putInt("lootSpewDelayTicks", this.lootSpewDelayTicks);
        tag.putInt("lootSpewCooldown", this.lootSpewCooldown);
        tag.putBoolean("lootSpewing", this.lootSpewing);
        tag.putBoolean("lootSpewed", this.lootSpewed);

        ListTag lanterns = new ListTag();
        for (BlockPos offset : this.lanternOffsets) {
            CompoundTag offsetTag = new CompoundTag();
            offsetTag.putInt("X", offset.getX());
            offsetTag.putInt("Y", offset.getY());
            offsetTag.putInt("Z", offset.getZ());
            lanterns.add(offsetTag);
        }
        tag.put(LANTERN_OFFSETS_TAG, lanterns);
    }

    private void loadZone(CompoundTag tag) {
        this.cornerA = new BlockPos(tag.getInt("cornerAx"), tag.getInt("cornerAy"), tag.getInt("cornerAz"));
        this.cornerB = new BlockPos(tag.getInt("cornerBx"), tag.getInt("cornerBy"), tag.getInt("cornerBz"));
        Direction facing = Direction.byName(tag.getString("authoredFacing"));
        this.authoredFacing = facing == null ? Direction.NORTH : facing;
        this.lanternIndexInitialized = tag.getBoolean("lanternIndexInitialized");
        this.cachedLanterns = tag.getInt("cachedLanterns");
        this.cachedLitLanterns = tag.getInt("cachedLitLanterns");
        this.requiredLitLanterns = tag.contains("requiredLitLanterns", Tag.TAG_INT) ? tag.getInt("requiredLitLanterns") : -1;
        this.configuredLootTable = tag.contains("configuredLootTable", Tag.TAG_STRING)
                ? ResourceLocation.parse(tag.getString("configuredLootTable"))
                : DEFAULT_LOOT_TABLE;
        this.lootSpewDelayTicks = tag.contains("lootSpewDelayTicks", Tag.TAG_INT) ? tag.getInt("lootSpewDelayTicks") : -1;
        this.lootSpewCooldown = tag.getInt("lootSpewCooldown");
        this.lootSpewing = tag.getBoolean("lootSpewing");
        this.lootSpewed = tag.getBoolean("lootSpewed");

        this.lanternOffsets.clear();
        ListTag lanterns = tag.getList(LANTERN_OFFSETS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < lanterns.size(); i++) {
            CompoundTag offsetTag = lanterns.getCompound(i);
            this.lanternOffsets.add(new BlockPos(offsetTag.getInt("X"), offsetTag.getInt("Y"), offsetTag.getInt("Z")));
        }
    }

    private void syncToClients() {
        if (this.level == null) {
            return;
        }

        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    private static boolean isWispwardLantern(BlockState state) {
        return state.is(DNLBlocks.WISPWARD_LANTERN.get()) || state.is(DNLBlocks.TIMED_WISPWARD_LANTERN.get());
    }

    private int getRequiredLitLanternsFor(int lanterns) {
        return this.requiredLitLanterns > 0 ? this.requiredLitLanterns : lanterns;
    }

    private record RegionBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
