package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.SignalRailBlock;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class C2SSignalRailInputPacket implements DNLPacket {
    private final boolean left;

    public C2SSignalRailInputPacket(boolean left) {
        this.left = left;
    }

    private C2SSignalRailInputPacket(FriendlyByteBuf buffer) {
        this.left = buffer.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.left);
    }

    public static C2SSignalRailInputPacket decode(FriendlyByteBuf buffer) {
        return new C2SSignalRailInputPacket(buffer);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || !(sender.getVehicle() instanceof AbstractMinecart minecart)
                || !(sender.level() instanceof ServerLevel level)) {
            return;
        }

        BlockPos railPos = findSignalRail(level, minecart);
        if (railPos == null) {
            return;
        }

        BlockState state = level.getBlockState(railPos);
        SignalRailBlock rail = (SignalRailBlock) state.getBlock();
        boolean positiveFacing = isPositiveFacing(sender, state.getValue(SignalRailBlock.SHAPE));
        int signal = this.left == positiveFacing ? SignalRailBlock.LEFT_SIDE : SignalRailBlock.RIGHT_SIDE;
        rail.activate(level, railPos, signal);
    }

    @Nullable
    private static BlockPos findSignalRail(ServerLevel level, Entity minecart) {
        BlockPos pos = minecart.blockPosition();
        if (level.getBlockState(pos).getBlock() instanceof SignalRailBlock) {
            return pos;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).getBlock() instanceof SignalRailBlock ? below : null;
    }

    private static boolean isPositiveFacing(ServerPlayer player, RailShape shape) {
        Vec3 look = player.getLookAngle();
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        if (northSouth) {
            return Math.abs(look.z) > 0.001D ? look.z < 0.0D : player.getDirection().getStepZ() <= 0;
        }
        return Math.abs(look.x) > 0.001D ? look.x > 0.0D : player.getDirection().getStepX() >= 0;
    }
}
