package dev.hexnowloading.dungeonnowloading.entity.util;

import net.minecraft.world.entity.Mob;

public interface LocomotionLockable {
    boolean isLocomotionLocked();

    void setLocomotionLocked(boolean locomotionLocked);

    default void applyLocomotionLock() {
        Mob mob = (Mob) this;
        mob.getNavigation().stop();
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0D);
        mob.setSpeed(0.0F);
        mob.setZza(0.0F);
        mob.setXxa(0.0F);
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
    }

    default void tickLocomotionLock() {
        Mob mob = (Mob) this;
        if (mob.level().isClientSide || !this.isLocomotionLocked()) {
            return;
        }

        this.applyLocomotionLock();
        mob.hasImpulse = true;
    }
}
