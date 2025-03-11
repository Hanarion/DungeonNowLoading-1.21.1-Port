package dev.hexnowloading.dungeonnowloading.mixin.structures;

import dev.hexnowloading.dungeonnowloading.network.packets.ClientboundStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class SituationalMusicMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public Screen screen;

    @Inject(method = "getSituationalMusic", at = @At("HEAD"), cancellable = true)
    private void injectStructureMusic(CallbackInfoReturnable<Music> cir) {

        Music music = (Music) Optionull.map(screen, Screen::getBackgroundMusic);

        if (music == null && player != null) {
            if (ClientboundStructureDetectionPacket.isClientInStructure()) {
                cir.setReturnValue(MUSIC_TEMPLE_OF_DUALITY);
            }
        }
    }

    private static final Music MUSIC_TEMPLE_OF_DUALITY = new Music(DNLSounds.MUSIC_TEMPLE_OF_DUALITY.get(), 0, 0, true);
}
