package dev.hexnowloading.dungeonnowloading.mixin.entities;

import dev.hexnowloading.dungeonnowloading.entity.monster.HollowEntity;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @Shadow @Nullable private IntOpenHashSet piercingIgnoreEntityIds;
    @Shadow public abstract byte getPierceLevel();
    @Shadow public abstract void setPierceLevel(byte level);

    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void dnl_hollowGhostArrowLogic(EntityHitResult hit, CallbackInfo ci) {
        Entity target = hit.getEntity();
        if (!(target instanceof HollowEntity hollow)) return;

        AbstractArrow self = (AbstractArrow)(Object)this;

        if (!(self instanceof Arrow arrow)) return;

        // ✅ If Hollow is glowing, let vanilla handle the hit normally (so it can take damage)
        if (hollow.hasEffect(MobEffects.GLOWING)) {
            return;
        }

        if (arrow.potion != Potions.EMPTY) {
            return;
        }

        // ✅ Extinguish flaming arrows + play fx (server-side)
        if (self.isOnFire()) {
            self.clearFire();

            if (!self.level().isClientSide && self.level() instanceof ServerLevel sl) {
                HollowEntity.playExtinguishFx(sl, self.position());
            }
        }

        // ✅ Mark as "pierced" so we don't collide again, and cancel vanilla so it cannot bounce
        if (this.getPierceLevel() <= 0) {
            this.setPierceLevel((byte) 1);
        }

        if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
        }
        this.piercingIgnoreEntityIds.add(target.getId());

        ci.cancel(); // skip vanilla onHitEntity -> no bounce
    }
}
