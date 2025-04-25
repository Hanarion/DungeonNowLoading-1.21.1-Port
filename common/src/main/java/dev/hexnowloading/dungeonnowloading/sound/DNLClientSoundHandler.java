package dev.hexnowloading.dungeonnowloading.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DNLClientSoundHandler {

    private static final Map<ResourceLocation, Map<Integer, Map<Integer, List<AbstractTickableSoundInstance>>>> activeSounds = new HashMap<>();

    public static void playTickingSound(ResourceLocation soundId, Entity entity, int tagId, float volume, float pitch, boolean stopWhenOutOfRange, float range) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(soundId);
        if (sound == null) return;

        int entityId = entity.getId();
        Map<Integer, Map<Integer, List<AbstractTickableSoundInstance>>> entityMap = activeSounds.computeIfAbsent(soundId, k -> new HashMap<>());
        Map<Integer, List<AbstractTickableSoundInstance>> tagMap = entityMap.computeIfAbsent(entityId, k -> new HashMap<>());

        if (tagId == -1) {
            tagId = tagMap.keySet().stream().mapToInt(i -> i).max().orElse(-1) + 1;
        }

        AbstractTickableSoundInstance instance = new EntityTickingSound(sound, entity, volume, pitch, stopWhenOutOfRange, range);
        Minecraft.getInstance().getSoundManager().play(instance);
        tagMap.computeIfAbsent(tagId, k -> new ArrayList<>()).add(instance);
    }

    public static void fadeInTickingSound(ResourceLocation soundId, int entityId, TickingSoundTarget target, int specificTagId, float maxVolume, int fadeInTicks) {
        Map<Integer, Map<Integer, List<AbstractTickableSoundInstance>>> entityMap = activeSounds.get(soundId);
        if (entityMap == null) return;

        Map<Integer, List<AbstractTickableSoundInstance>> tagMap = entityMap.get(entityId);
        if (tagMap == null || tagMap.isEmpty()) return;

        if (target == TickingSoundTarget.ALL) {
            for (List<AbstractTickableSoundInstance> list : tagMap.values()) {
                for (AbstractTickableSoundInstance instance : list) {
                    if (instance instanceof DNLTickingSound dnl) {
                        dnl.startFadingIn(maxVolume, fadeInTicks);
                    }
                }
            }
            return;
        }

        int tagId = resolveTagId(target, tagMap, specificTagId);
        if (tagId == -1) return;

        List<AbstractTickableSoundInstance> list = tagMap.get(tagId);
        if (list == null) return;

        for (AbstractTickableSoundInstance instance : list) {
            if (instance instanceof DNLTickingSound dnl) {
                dnl.startFadingIn(maxVolume, fadeInTicks);
            }
        }
    }

    public static void fadeOutTickingSound(ResourceLocation soundId, int entityId, TickingSoundTarget target, int specificTagId, int fadeTicks, boolean shouldStop) {
        Map<Integer, Map<Integer, List<AbstractTickableSoundInstance>>> entityMap = activeSounds.get(soundId);
        if (entityMap == null) return;

        Map<Integer, List<AbstractTickableSoundInstance>> tagMap = entityMap.get(entityId);
        if (tagMap == null || tagMap.isEmpty()) return;

        if (target == TickingSoundTarget.ALL) {
            for (List<AbstractTickableSoundInstance> list : tagMap.values()) {
                for (AbstractTickableSoundInstance instance : list) {
                    if (instance instanceof DNLTickingSound dnl) {
                        dnl.startFadingOut(shouldStop, fadeTicks);
                    }
                }
            }
            entityMap.remove(entityId);
            if (entityMap.isEmpty()) activeSounds.remove(soundId);
            return;
        }

        int tagId = resolveTagId(target, tagMap, specificTagId);
        if (tagId == -1) return;

        List<AbstractTickableSoundInstance> list = tagMap.remove(tagId);
        if (list == null) return;

        for (AbstractTickableSoundInstance instance : list) {
            if (instance instanceof DNLTickingSound dnl) {
                dnl.startFadingOut(shouldStop, fadeTicks);
            }
        }

        if (tagMap.isEmpty()) entityMap.remove(entityId);
        if (entityMap.isEmpty()) activeSounds.remove(soundId);
    }

    private static int resolveTagId(TickingSoundTarget target, Map<Integer, List<AbstractTickableSoundInstance>> tagMap, int specificTagId) {
        return switch (target) {
            case OLDEST -> tagMap.keySet().stream().min(Integer::compareTo).orElse(-1);
            case NEWEST -> tagMap.keySet().stream().max(Integer::compareTo).orElse(-1);
            case SPECIFIC -> specificTagId;
            case ALL -> throw new IllegalStateException("ALL is not a valid target for resolveTagId");
        };
    }
}
