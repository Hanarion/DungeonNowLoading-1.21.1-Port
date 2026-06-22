package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.network.packets.S2CStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    private BlockPos lastCheckedPos = BlockPos.ZERO;

    @Shadow public abstract ServerLevel serverLevel();

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerMove(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        BlockPos currentPos = player.blockPosition();

        if (currentPos.equals(lastCheckedPos)) return; // Skip check if still in the same block

        lastCheckedPos = currentPos;
        // ✅ Get the structure at the player's positionetResourceKey(ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "temple_of_duality"));

        boolean isInTemple = serverLevel().structureManager().getStructureWithPieceAt(currentPos, DNLTags.TEMPLE_OF_DUALITY).isValid();

        Services.NETWORK.sendToPlayer(new S2CStructureDetectionPacket(isInTemple, player.getId()), player);
    }
}
