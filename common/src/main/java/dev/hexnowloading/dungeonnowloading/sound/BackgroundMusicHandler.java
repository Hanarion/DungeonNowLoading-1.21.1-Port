package dev.hexnowloading.dungeonnowloading.sound;

import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.ChannelAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BackgroundMusicHandler {
    private static final List<FadingChannel> fadingChannels = new ArrayList<>();

    public static boolean isBackgroundMusicBlocked() {
        return DNLClientSoundHandler.isTickingSoundActive(DNLSounds.MUSIC_CLASH_OF_DUALITY_BASE.get().getLocation());
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
