package dev.hexnowloading.dungeonnowloading.mixin.fabric.block;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Extend Entity via LivingEntity's hierarchy
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void dnl$applyOilSlip(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Level level = this.level();
        if (level.isClientSide) return;

        if (!(self instanceof Player player)) return;

        // respect sneaking
        if (this.isSteppingCarefully()) return;

        // only when on ground
        if (!this.onGround()) return;

        Vec3 v = this.getDeltaMovement();

        // ignore strong vertical motion (jump/fall)
        if (Math.abs(v.y) > 0.08D) return;

        // Sample block at the bottom of the feet, slightly inside
        double feetY = this.getBoundingBox().minY;
        BlockPos feetPos = BlockPos.containing(this.getX(), feetY - 0.05D, this.getZ());
        BlockState stateAtFeet = level.getBlockState(feetPos);

        if (!stateAtFeet.is(DNLBlocks.OIL_SPILL.get())) {
            return;
        }

        double vx = v.x;
        double vz = v.z;
        double horSq = vx * vx + vz * vz;

        // no horizontal movement -> nothing to retain
        if (horSq < 0.0003D) return;

        // This is the "ice" effect: reduce net friction by compensating a bit
        double retention = 1.06D; // tune: 1.03 subtle, 1.06 ice-like, 1.10 crazy
        double max = 0.9D;

        double nx = Mth.clamp(vx * retention, -max, max);
        double nz = Mth.clamp(vz * retention, -max, max);

        this.setDeltaMovement(new Vec3(nx, v.y, nz));
    }
}
