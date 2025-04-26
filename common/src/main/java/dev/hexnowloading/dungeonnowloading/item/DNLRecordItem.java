package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;

public class DNLRecordItem extends RecordItem {
    public DNLRecordItem(int analogOutput, SoundEvent soundEvent, Properties properties, int lengthInSeconds) {
        super(analogOutput, soundEvent, properties, lengthInSeconds);
    }
}
