package dev.hexnowloading.dungeonnowloading.entity.ai.control.look;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class EmptyLookControl extends LookControl {
    public EmptyLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void tick() {
    }
}
