package dev.hexnowloading.dungeonnowloading.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityTickingSound extends AbstractTickableSoundInstance implements DNLTickingSound {

    private static final float MINIMUM_AUDIBLE_VOLUME = 0.01f;

    protected final Entity entity;

    private boolean fadingOut = false;
    private boolean fadingIn = false;
    private boolean stopWhenOutOfRange = true;
    private boolean shouldStop = false;
    private int fadeTicks = 20;
    private int currentFade = 0;
    private final float range;
    private float maxVolume;

    public EntityTickingSound(SoundEvent sound, Entity entity, float maxVolume, float pitch, boolean stopWhenOutOfRange, float range) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = false;
        this.maxVolume = maxVolume;
        this.pitch = pitch;
        this.stopWhenOutOfRange = stopWhenOutOfRange;
        this.range = range;

        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();

        this.volume = (this.maxVolume == 0f) ? MINIMUM_AUDIBLE_VOLUME : this.maxVolume;
    }

    private float fadeVolume = 1.0f;
    private float distanceVolume = 1.0f;

    @Override
    public void tick() {
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();

        LocalPlayer localPlayer = Minecraft.getInstance().player;

        // -- Distance Volume Logic --
        if (localPlayer != null && !localPlayer.equals(entity)) {
            float distance = localPlayer.distanceTo(entity);
            float maxDistance = 2.0f * range;
            distanceVolume = Math.max(0f, 1.0f - (distance / maxDistance));

            if (stopWhenOutOfRange && distance > range) {
                this.stop();
                return;
            }
        } else {
            distanceVolume = 1.0f;
        }

        // -- Fading In Logic --
        if (fadingIn) {
            currentFade++;
            fadeVolume = (currentFade / (float) fadeTicks);
            if (currentFade >= fadeTicks) {
                fadeVolume = 1.0f;
                fadingIn = false;
            }
        }

        // -- Fading Out Logic --
        if (fadingOut) {
            currentFade++;
            fadeVolume = 1.0f - (currentFade / (float) fadeTicks);
            if ((this.shouldStop || this.stopWhenOutOfRange) && currentFade >= fadeTicks) {
                this.stop();
                return;
            }
        }

        // -- Final volume combines both effects --
        this.volume = maxVolume * fadeVolume * distanceVolume;

        if (!entity.isAlive()) {
            this.stop();
        }
    }

    @Override
    public void startFadingOut(boolean shouldStop, int fadeTicks) {
        this.fadingOut = true;
        this.shouldStop = shouldStop;
        this.fadeTicks = fadeTicks;
        this.currentFade = 0; // ✅ Required
    }

    @Override
    public void startFadingIn(float maxVolume, int fadeTicks) {
        this.fadingIn = true;
        this.maxVolume = maxVolume;
        this.fadeTicks = fadeTicks;
        this.currentFade = 0; // ✅ Required
    }

    @Override
    public void stopExternally() {
        this.stop();
    }
}
