package dev.hexnowloading.dungeonnowloading.registry;

import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public class DNLMusics {


    public static final Supplier<Music> TEMPLE_OF_DUALITY_MUSIC = () -> new Music(DNLSounds.MUSIC_TEMPLE_OF_DUALITY.get(), 0, 0, true);

    public static void init() {}
}
