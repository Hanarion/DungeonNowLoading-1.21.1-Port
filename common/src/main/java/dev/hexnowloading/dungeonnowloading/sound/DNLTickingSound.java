package dev.hexnowloading.dungeonnowloading.sound;

public interface DNLTickingSound {
    void startFadingOut(boolean shouldStop, int fadeTicks);
    void startFadingIn(float maxVolume, int fadeTicks);
    void stopExternally();
}