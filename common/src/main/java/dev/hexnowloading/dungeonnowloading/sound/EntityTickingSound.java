package dev.hexnowloading.dungeonnowloading.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public class EntityTickingSound extends AbstractTickableSoundInstance implements DNLTickingSound {

    protected final LivingEntity entity;

    private boolean fadingOut = false;
    private int fadeTicks = 20;
    private int currentFade = 0;

    public EntityTickingSound(SoundEvent sound, LivingEntity entity) {
        super(sound, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = false;
        this.volume = 1.0f;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && !localPlayer.equals(entity)) {
            float distance = localPlayer.distanceTo(entity);
            this.volume = Math.max(0f, 1.0f - (distance / 64f));
            if (distance > 32f) {
                this.stop();
                return;
            }
        }

        if (fadingOut) {
            currentFade++;
            this.volume = 1.0f - (currentFade / (float) fadeTicks);
            if (currentFade >= fadeTicks) this.stop();
        }

        if (!entity.isAlive()) this.stop();
    }

    @Override
    public void startFadingOut() {
        this.fadingOut = true;
    }

    @Override
    public void stopExternally() {
        this.stop();
    }
}
