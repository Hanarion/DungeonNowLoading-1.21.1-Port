package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.block.ZoneReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ZoneWandItem extends Item {

    private static final String CORNER_A = "CornerA";
    private static final String CORNER_B = "CornerB";

    public ZoneWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        CompoundTag tag = stack.getOrCreateTag();

        // Sneak-right-click: clear wand corners
        if (player.isCrouching()) {
            tag.remove(CORNER_A);
            tag.remove(CORNER_B);
            tag.remove("NextCornerIsB");
            player.displayClientMessage(Component.literal("Corners cleared from wand."), true);
            return InteractionResult.CONSUME;
        }

        // If clicked block is a zone receiver: apply corners to its BE (do NOT overwrite wand)
        BlockEntity be = level.getBlockEntity(clickedPos);
        if (be instanceof ZoneReceiverBlockEntity receiver) {
            if (tag.contains(CORNER_A) && tag.contains(CORNER_B)) {
                BlockPos a = readPos(tag.getCompound(CORNER_A));
                BlockPos b = readPos(tag.getCompound(CORNER_B));

                // Choose an authored facing.
                // If your blocks use BlockStateProperties.FACING, grab it; otherwise default.
                Direction authoredFacing = Direction.NORTH;
                var state = level.getBlockState(clickedPos);
                if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                    authoredFacing = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
                }

                receiver.setRegion(a, b, authoredFacing);

                // if you want clients to see any BE-rendered change immediately:
                be.setChanged();
                level.sendBlockUpdated(clickedPos, state, state, 3);

                player.displayClientMessage(
                        Component.literal("Zone applied to block at " + clickedPos.toShortString() + " (wand corners kept)."),
                        true
                );
            } else {
                player.displayClientMessage(
                        Component.literal("Wand needs Corner A and Corner B first."),
                        true
                );
            }
            return InteractionResult.CONSUME;
        }

        // Otherwise: we are setting/updating the wand corners (same as your old behavior)
        boolean nextCornerIsB = tag.getBoolean("NextCornerIsB");

        if (!tag.contains(CORNER_A)) {
            tag.put(CORNER_A, writePos(clickedPos));
            tag.putBoolean("NextCornerIsB", true);
            player.displayClientMessage(Component.literal("Corner A set at " + clickedPos.toShortString()), true);

        } else if (!tag.contains(CORNER_B)) {
            tag.put(CORNER_B, writePos(clickedPos));
            tag.putBoolean("NextCornerIsB", false);
            player.displayClientMessage(Component.literal("Corner B set at " + clickedPos.toShortString()), true);

        } else {
            if (nextCornerIsB) {
                tag.put(CORNER_B, writePos(clickedPos));
                tag.putBoolean("NextCornerIsB", false);
                player.displayClientMessage(Component.literal("Corner B updated at " + clickedPos.toShortString()), true);
            } else {
                tag.put(CORNER_A, writePos(clickedPos));
                tag.putBoolean("NextCornerIsB", true);
                player.displayClientMessage(Component.literal("Corner A updated at " + clickedPos.toShortString()), true);
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
