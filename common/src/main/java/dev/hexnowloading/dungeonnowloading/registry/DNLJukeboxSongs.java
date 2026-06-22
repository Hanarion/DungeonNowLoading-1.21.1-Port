package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.JukeboxSong;

/**
 * ResourceKeys for the mod's jukebox songs. The songs themselves are data-driven
 * JSON files under {@code data/dungeonnowloading/jukebox_song/} (1.21 replaced the
 * class-based RecordItem with the JukeboxSong registry + JUKEBOX_PLAYABLE component).
 */
public class DNLJukeboxSongs {
    public static final ResourceKey<JukeboxSong> HELLSPAWN = key("hellspawn");
    public static final ResourceKey<JukeboxSong> AOTSUGI = key("aotsugi");
    public static final ResourceKey<JukeboxSong> BROKEN_AOTSUGI = key("broken_aotsugi");
    public static final ResourceKey<JukeboxSong> CLASH_OF_DUALITY_OUROS = key("clash_of_duality_ouros");
    public static final ResourceKey<JukeboxSong> CLASH_OF_DUALITY_BOROS = key("clash_of_duality_boros");
    public static final ResourceKey<JukeboxSong> CLASH_OF_DUALITY_PYTHONIC_OVERDRIVE = key("clash_of_duality_pythonic_overdrive");

    private static ResourceKey<JukeboxSong> key(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, DungeonNowLoading.id(name));
    }
}
