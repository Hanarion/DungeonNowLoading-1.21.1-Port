package dev.hexnowloading.dungeonnowloading.world;

import dev.hexnowloading.dungeonnowloading.block.entity.WispBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WispBlockTracker extends SavedData {
    private static final String DATA_NAME = "dungeonnowloading_wisp_blocks";
    private static final String PLAYERS_TAG = "Players";
    private static final String OWNER_TAG = "Owner";
    private static final String WISPS_TAG = "Wisps";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private static final String Z_TAG = "Z";
    private static final String PLACED_GAME_TIME_TAG = "PlacedGameTime";

    private final Map<UUID, List<TrackedWisp>> wispsByOwner = new HashMap<>();

    public static WispBlockTracker get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WispBlockTracker::new, WispBlockTracker::load, null),
                DATA_NAME);
    }

    public static WispBlockTracker load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        WispBlockTracker tracker = new WispBlockTracker();
        ListTag players = tag.getList(PLAYERS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            if (!playerTag.hasUUID(OWNER_TAG)) {
                continue;
            }

            UUID owner = playerTag.getUUID(OWNER_TAG);
            List<TrackedWisp> wisps = new ArrayList<>();
            ListTag wispTags = playerTag.getList(WISPS_TAG, Tag.TAG_COMPOUND);
            for (int j = 0; j < wispTags.size(); j++) {
                CompoundTag wispTag = wispTags.getCompound(j);
                ResourceLocation dimensionId = ResourceLocation.parse(wispTag.getString(DIMENSION_TAG));
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
                BlockPos pos = new BlockPos(wispTag.getInt(X_TAG), wispTag.getInt(Y_TAG), wispTag.getInt(Z_TAG));
                wisps.add(new TrackedWisp(dimension, pos, wispTag.getLong(PLACED_GAME_TIME_TAG)));
            }

            if (!wisps.isEmpty()) {
                tracker.wispsByOwner.put(owner, wisps);
            }
        }
        return tracker;
    }

    public void registerPlaced(ServerLevel level, BlockPos pos, UUID owner, long placedGameTime) {
        List<TrackedWisp> wisps = this.wispsByOwner.computeIfAbsent(owner, ignored -> new ArrayList<>());
        this.clean(level.getServer(), owner, wisps);
        wisps.removeIf(wisp -> wisp.dimension.equals(level.dimension()) && wisp.pos.equals(pos));
        wisps.add(new TrackedWisp(level.dimension(), pos.immutable(), placedGameTime));
        wisps.sort(Comparator.comparingLong(TrackedWisp::placedGameTime));

        while (wisps.size() > WispBlockEntity.MAX_WISPS_PER_PLAYER) {
            TrackedWisp oldest = wisps.remove(0);
            ServerLevel wispLevel = level.getServer().getLevel(oldest.dimension);
            if (wispLevel != null && wispLevel.getBlockState(oldest.pos).is(DNLBlocks.WISP_BLOCK.get())) {
                wispLevel.setBlock(oldest.pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        this.setDirty();
    }

    public void unregister(ServerLevel level, BlockPos pos, UUID owner) {
        List<TrackedWisp> wisps = this.wispsByOwner.get(owner);
        if (wisps == null) {
            return;
        }

        if (wisps.removeIf(wisp -> wisp.dimension.equals(level.dimension()) && wisp.pos.equals(pos))) {
            if (wisps.isEmpty()) {
                this.wispsByOwner.remove(owner);
            }
            this.setDirty();
        }
    }

    private void clean(MinecraftServer server, UUID owner, List<TrackedWisp> wisps) {
        if (wisps.removeIf(wisp -> !this.isValid(server, owner, wisp))) {
            this.setDirty();
        }
    }

    private boolean isValid(MinecraftServer server, UUID owner, TrackedWisp wisp) {
        ServerLevel level = server.getLevel(wisp.dimension);
        if (level == null || !level.getBlockState(wisp.pos).is(DNLBlocks.WISP_BLOCK.get())) {
            return false;
        }

        return level.getBlockEntity(wisp.pos) instanceof WispBlockEntity blockEntity && owner.equals(blockEntity.getOwner());
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, List<TrackedWisp>> entry : this.wispsByOwner.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }

            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(OWNER_TAG, entry.getKey());

            ListTag wisps = new ListTag();
            for (TrackedWisp wisp : entry.getValue()) {
                CompoundTag wispTag = new CompoundTag();
                wispTag.putString(DIMENSION_TAG, wisp.dimension.location().toString());
                wispTag.putInt(X_TAG, wisp.pos.getX());
                wispTag.putInt(Y_TAG, wisp.pos.getY());
                wispTag.putInt(Z_TAG, wisp.pos.getZ());
                wispTag.putLong(PLACED_GAME_TIME_TAG, wisp.placedGameTime);
                wisps.add(wispTag);
            }

            playerTag.put(WISPS_TAG, wisps);
            players.add(playerTag);
        }

        tag.put(PLAYERS_TAG, players);
        return tag;
    }

    private record TrackedWisp(ResourceKey<Level> dimension, BlockPos pos, long placedGameTime) {
    }
}
