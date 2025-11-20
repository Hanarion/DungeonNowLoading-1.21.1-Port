package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.entity.MobSpawnPointBlockEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DetectionWandItem extends Item {

    private static final String CORNER_A = "CornerA";
    private static final String CORNER_B = "CornerB";

    public DetectionWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        CompoundTag tag = stack.getOrCreateTag();
        var state = level.getBlockState(pos);

        // 1) If we clicked a MobSpawnPoint, try to apply corners
        if (state.is(DNLBlocks.MOB_SPAWN_POINT.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MobSpawnPointBlockEntity sp) {
                if (tag.contains(CORNER_A) && tag.contains(CORNER_B)) {
                    BlockPos a = readPos(tag.getCompound(CORNER_A));
                    BlockPos b = readPos(tag.getCompound(CORNER_B));

                    sp.setDetectionFromWorldCorners(state, a, b);

                    // ✅ Do NOT clear the corners here, so we can reuse the same zone
                    player.displayClientMessage(
                            Component.literal("Detection box applied to Mob Spawn Point (corners kept)."),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            Component.literal("Wand needs Corner A and Corner B first."),
                            true
                    );
                }
            }
            return InteractionResult.CONSUME;
        }

        // 2) Update corners when clicking non-MobSpawnPoint blocks
        if (player.isCrouching()) {
            tag.remove(CORNER_A);
            tag.remove(CORNER_B);
            tag.remove("NextCornerIsB");
            player.displayClientMessage(Component.literal("Corners cleared from wand."), true);
            return InteractionResult.CONSUME;
        }

// Track which corner should be updated next
        boolean nextCornerIsB = tag.getBoolean("NextCornerIsB");

        if (!tag.contains(CORNER_A)) {
            tag.put(CORNER_A, writePos(pos));
            tag.putBoolean("NextCornerIsB", true); // Next update goes to B
            player.displayClientMessage(Component.literal("Corner A set at " + pos.toShortString()), true);

        } else if (!tag.contains(CORNER_B)) {
            tag.put(CORNER_B, writePos(pos));
            tag.putBoolean("NextCornerIsB", false); // Next update goes to A
            player.displayClientMessage(Component.literal("Corner B set at " + pos.toShortString()), true);

        } else {
            if (nextCornerIsB) {
                tag.put(CORNER_B, writePos(pos));
                tag.putBoolean("NextCornerIsB", false); // Next update goes to A
                player.displayClientMessage(Component.literal(
                        "Corner B updated at " + pos.toShortString()
                ), true);
            } else {
                tag.put(CORNER_A, writePos(pos));
                tag.putBoolean("NextCornerIsB", true); // Next update goes to B
                player.displayClientMessage(Component.literal(
                        "Corner A updated at " + pos.toShortString()
                ), true);
            }
        }

        return InteractionResult.CONSUME;
    }

    private static CompoundTag writePos(BlockPos pos) {
        CompoundTag t = new CompoundTag();
        t.putInt("X", pos.getX());
        t.putInt("Y", pos.getY());
        t.putInt("Z", pos.getZ());
        return t;
    }

    private static BlockPos readPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
