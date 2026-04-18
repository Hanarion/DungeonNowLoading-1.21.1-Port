package dev.hexnowloading.dungeonnowloading.sound;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackgroundMusicHandler {
    private static final List<FadingChannel> fadingChannels = new ArrayList<>();

    private static final List<ResourceLocation> BLOCKING_TICKING_SOUNDS = List.of(
            new ResourceLocation(DungeonNowLoading.MOD_ID, "music_clash_of_duality_base"),
            new ResourceLocation(DungeonNowLoading.MOD_ID, "music_hellspawn_base")
    );

    public static boolean isBackgroundMusicBlocked() {
        for (ResourceLocation id : BLOCKING_TICKING_SOUNDS) {
            if (DNLClientSoundHandler.isTickingSoundActive(id)) return true;
        }
        return false;
    }


    public static void fadeOutCurrentMusic(int fadeTicks) {
        var mc = Minecraft.getInstance();
        var music = mc.getMusicManager().currentMusic;
        if (music != null) {
            var handle = mc.getSoundManager().soundEngine.instanceToChannel.get(music);
            if (handle != null) {
                startFading(handle, fadeTicks);
            }
        }
    }

    private static void startFading(ChannelAccess.ChannelHandle handle, int fadeTicks) {
        fadingChannels.add(new FadingChannel(handle, fadeTicks));
    }

    public static void tick() {
        Iterator<FadingChannel> iterator = fadingChannels.iterator();
        while (iterator.hasNext()) {
            FadingChannel fading = iterator.next();
            if (!fading.tick()) {
                iterator.remove();
            }
        }
    }

    private static class FadingChannel {
        private final ChannelAccess.ChannelHandle handle;
        private final int totalTicks;
        private int currentTick = 0;

        FadingChannel(ChannelAccess.ChannelHandle handle, int totalTicks) {
            this.handle = handle;
            this.totalTicks = totalTicks;
        }

        boolean tick() {
            if (currentTick >= totalTicks) {
                handle.execute(channel -> {
                    channel.setVolume(0.0f);
                    channel.stop();
                });
                return false;
            } else {
                float volume = 1.0f - (currentTick / (float) totalTicks);
                handle.execute(channel -> channel.setVolume(volume));
                currentTick++;
                return true;
            }
        }
    }
}
