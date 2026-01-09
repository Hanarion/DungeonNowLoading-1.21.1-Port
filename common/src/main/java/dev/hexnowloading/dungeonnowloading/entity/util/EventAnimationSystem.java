package dev.hexnowloading.dungeonnowloading.entity.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class EventAnimationSystem {

    private final Map<String, EventTrack> activeTracks = new HashMap<>();

    /** Call every tick (usually client-side only). */
    public void tick() {
        Iterator<EventTrack> it = activeTracks.values().iterator();
        while (it.hasNext()) {
            EventTrack track = it.next();
            track.ticksRemaining--;
            if (track.ticksRemaining <= 0) {
                track.stop();
                it.remove();
            }
        }
    }

    /**
     * Play (or replace) an event animation on a named channel.
     *
     * @param channel unique identifier (e.g. "hurt", "blink", "recoil")
     * @param ticks duration in ticks
     */
    public void play(
            String channel,
            int ticks,
            Runnable onStart,
            Runnable onStop
    ) {
        if (ticks <= 0) return;

        // stop existing track on same channel
        EventTrack existing = activeTracks.remove(channel);
        if (existing != null) {
            existing.stop();
        }

        if (onStart != null) onStart.run();
        activeTracks.put(channel, new EventTrack(ticks, onStop));
    }

    public void playSeconds(
            String channel,
            float seconds,
            Runnable onStart,
            Runnable onStop
    ) {
        play(channel, secondsToTicks(seconds), onStart, onStop);
    }

    public void stop(String channel) {
        EventTrack track = activeTracks.remove(channel);
        if (track != null) track.stop();
    }

    public void stopAll() {
        for (EventTrack track : activeTracks.values()) {
            track.stop();
        }
        activeTracks.clear();
    }

    public boolean isActive(String channel) {
        return activeTracks.containsKey(channel);
    }

    private static int secondsToTicks(float seconds) {
        return Math.max(0, (int) (seconds * 20f));
    }

    private static final class EventTrack {
        int ticksRemaining;
        Runnable onStop;

        EventTrack(int ticks, Runnable onStop) {
            this.ticksRemaining = ticks;
            this.onStop = onStop;
        }

        void stop() {
            if (onStop != null) onStop.run();
        }
    }
}
