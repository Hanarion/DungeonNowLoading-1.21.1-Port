package dev.hexnowloading.dungeonnowloading.mixin.client;

import dev.hexnowloading.dungeonnowloading.network.packets.S2CStructureDetectionPacket;
import dev.hexnowloading.dungeonnowloading.registry.DNLMusics;
import dev.hexnowloading.dungeonnowloading.sound.BackgroundMusicHandler;
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

        /**
         * For preventing the background music from playing when the boss theme or other music source
         * is playing.
         */
        if (BackgroundMusicHandler.isBackgroundMusicBlocked()) {
            cir.setReturnValue(null);
            return;
        }

        /**
         * Manages the music that is played in the structure.
         */

        //TODO: Needs rewrite once more structure musics are added.

        Music music = (Music) Optionull.map(screen, Screen::getBackgroundMusic);

        if (music == null && player != null) {
            if (S2CStructureDetectionPacket.isClientInStructure()) {
                cir.setReturnValue(DNLMusics.TEMPLE_OF_DUALITY_MUSIC.get());
            }
        }
    }
}
