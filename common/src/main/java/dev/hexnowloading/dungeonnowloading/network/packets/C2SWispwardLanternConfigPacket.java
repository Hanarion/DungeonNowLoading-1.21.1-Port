package dev.hexnowloading.dungeonnowloading.network.packets;

import dev.hexnowloading.dungeonnowloading.block.WispwardLanternBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.WispwardLanternBlockEntity;
import dev.hexnowloading.dungeonnowloading.network.DNLPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class C2SWispwardLanternConfigPacket implements DNLPacket {
    private static final double MAX_EDIT_DISTANCE_SQ = 64.0D * 64.0D;

    private final BlockPos pos;
    private final int timerSeconds;

    public C2SWispwardLanternConfigPacket(BlockPos pos, int timerSeconds) {
        this.pos = pos.immutable();
        this.timerSeconds = timerSeconds;
    }

    public C2SWispwardLanternConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.timerSeconds = buf.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.timerSeconds);
    }

    public static C2SWispwardLanternConfigPacket decode(FriendlyByteBuf buf) {
        return new C2SWispwardLanternConfigPacket(buf);
    }

    @Override
    public void handle(@Nullable ServerPlayer sender) {
        if (sender == null || !sender.getAbilities().instabuild) {
            return;
        }

        sender.server.execute(() -> {
            ServerLevel level = sender.serverLevel();
            if (!level.isLoaded(this.pos) || sender.distanceToSqr(this.pos.getCenter()) > MAX_EDIT_DISTANCE_SQ) {
                return;
            }

            if (level.getBlockEntity(this.pos) instanceof WispwardLanternBlockEntity lantern) {
                lantern.setTimerSeconds(this.timerSeconds);

                BlockState state = level.getBlockState(this.pos);
                if (state.hasProperty(WispwardLanternBlock.LIT) && state.getValue(WispwardLanternBlock.LIT)) {
                    lantern.markLit(level.getGameTime());
                    level.scheduleTick(this.pos, state.getBlock(), lantern.getTimerSeconds() * 20);
                    level.sendBlockUpdated(this.pos, state, state, Block.UPDATE_CLIENTS);
                }
            }
        });
    }
}
