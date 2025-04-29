package dev.hexnowloading.dungeonnowloading.registry;

import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public class DNLMusics {

    private static final int ONE_SECOND = 20;
    private static final int THIRTY_SECONDS = 600;
    private static final int TEN_MINUTES = 12000;
    private static final int TWENTY_MINUTES = 24000;
    private static final int FIVE_MINUTES = 6000;

    public static final Supplier<Music> TEMPLE_OF_DUALITY_MUSIC = () -> new Music(DNLSounds.MUSIC_TEMPLE_OF_DUALITY.get(), FIVE_MINUTES, TWENTY_MINUTES, true);

    public static void init() {}

}