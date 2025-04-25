package dev.hexnowloading.dungeonnowloading.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class DNLClientSoundHandler {
    private static final Map<ResourceLocation, Map<Integer, AbstractTickableSoundInstance>> activeSounds = new HashMap<>();

    public static void playTickingSound(ResourceLocation soundId, Entity entity, boolean forceRestart, float volume, float pitch, boolean stopSoundWhenOutOfRange, float range) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (sound == null) return;

        int id = entity.getId();
        activeSounds.computeIfAbsent(soundId, k -> new HashMap<>());
        Map<Integer, AbstractTickableSoundInstance> soundMap = activeSounds.get(soundId);

        AbstractTickableSoundInstance existing = soundMap.get(id);

        if (existing == null || existing.isStopped() || forceRestart) {
            if (existing != null && !existing.isStopped() && existing instanceof DNLTickingSound fadableTickingSound) {
                fadableTickingSound.stopExternally();
            }

            AbstractTickableSoundInstance soundInstance = new EntityTickingSound(sound, entity, volume, pitch, stopSoundWhenOutOfRange, range);
            Minecraft.getInstance().getSoundManager().play(soundInstance);
            soundMap.put(id, soundInstance);
        }
    }

    public static void playTickingSound(ResourceLocation soundId, Entity entity, boolean stopOnRecall) {
        playTickingSound(soundId, entity, true, 1, 1, stopOnRecall, 32);
    }

    public static void fadeOutTickingSound(ResourceLocation soundId, int entityId, int fadeTicks, boolean shouldStop) {
        Map<Integer, AbstractTickableSoundInstance> soundMap = activeSounds.get(soundId);
        if (soundMap != null) {
            AbstractTickableSoundInstance instance = soundMap.remove(entityId);
            if (instance instanceof DNLTickingSound sound) {
                sound.startFadingOut(shouldStop, fadeTicks);
            }
        }
    }

    public static void fadeInTickingSound(ResourceLocation soundId, int entityId, float maxVolume) {
        Map<Integer, AbstractTickableSoundInstance> soundMap = activeSounds.get(soundId);
        if (soundMap != null) {
            AbstractTickableSoundInstance instance = soundMap.get(entityId);
            if (instance instanceof DNLTickingSound sound) {
                sound.startFadingIn(maxVolume);
            }
        }
    }
}
