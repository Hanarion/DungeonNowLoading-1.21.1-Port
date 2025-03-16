package dev.hexnowloading.dungeonnowloading.capabilities.fabric;

import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.nbt.CompoundTag;

public class ScorcherHeatCapabilityHandler implements IntComponent, PlayerComponent<ScorcherHeatCapabilityHandler> {

    private int heat;

    public ScorcherHeatCapabilityHandler() {
        this(0);
    }

    public ScorcherHeatCapabilityHandler(int heat) {
        this.heat = heat;
    }

    @Override
    public int getValue() {
        return heat;
    }

    @Override
    public void setValue(int i) {
        heat = i;
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        if (compoundTag.contains("ScorcherHeat", CompoundTag.TAG_INT)) {
            this.heat = compoundTag.getInt("ScorcherHeat");
        }
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        compoundTag.putInt("ScorcherHeat", this.heat);
    }
}
