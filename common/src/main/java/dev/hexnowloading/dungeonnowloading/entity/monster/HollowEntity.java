package dev.hexnowloading.dungeonnowloading.entity.monster;

import dev.hexnowloading.dungeonnowloading.entity.ai.hollow.HollowBreakLightGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.hollow.HollowChargeAttackGoal;
import dev.hexnowloading.dungeonnowloading.entity.ai.hollow.HollowMoveControl;
import dev.hexnowloading.dungeonnowloading.entity.ai.hollow.HollowRandomMoveGoal;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.registry.DNLTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class HollowEntity extends Monster {

    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(HollowEntity.class, EntityDataSerializers.BOOLEAN);

    private int chargeActiveTicks = 0;
    private int chargeCooldownTicks = 0;

    public static final int CHARGE_MAX_TICKS = 8 * 20;     // 8s
    public static final int CHARGE_COOLDOWN_TICKS = 5 * 20;
    public static final int CHARGE_HIT_COOLDOWN_TICKS = 2 * 20;

    public HollowEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new HollowMoveControl(this);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new HollowChargeAttackGoal(this));
        this.goalSelector.addGoal(5, new HollowBreakLightGoal(this, 16, 20, 1.1));
        this.goalSelector.addGoal(8, new HollowRandomMoveGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, HollowEntity.class)).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        // your existing noPhysics logic...
        boolean inLight = this.isInLight();
        if (inLight) {
            this.noPhysics = this.level().noCollision(this, this.getBoundingBox()) ? false : true;
        } else {
            this.noPhysics = true;
        }
        this.setNoGravity(true);

        if (!this.level().isClientSide) {
            if (chargeCooldownTicks > 0) chargeCooldownTicks--;

            if (this.IsCharging()) {
                chargeActiveTicks++;
                if (chargeActiveTicks >= CHARGE_MAX_TICKS) {
                    startChargeCooldownDefault();
                    this.getNavigation().stop();
                }
            } else {
                chargeActiveTicks = 0;
            }
        }

        super.tick();
    }




    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanFloat(true);
        return nav;
    }

    @Override
    public void move(MoverType moverType, Vec3 vec3) {
        super.move(moverType, vec3);
        if (!this.noPhysics) {
            this.checkInsideBlocks();
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isSunSensitive() && this.isSunBurnTick()) {

            // Optional helmet logic (same as zombie). If you DON'T want helmets to protect, delete this block.
            ItemStack head = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!head.isEmpty()) {
                if (head.isDamageableItem()) {
                    head.setDamageValue(head.getDamageValue() + this.random.nextInt(2));
                    if (head.getDamageValue() >= head.getMaxDamage()) {
                        this.broadcastBreakEvent(EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }
            } else {
                // Vanish instead of burning
                vanishPoof();
                return; // skip normal aiStep
            }
        }

        super.aiStep();
    }


    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) return false;

        if (source.getEntity() instanceof Player p && p.getAbilities().instabuild) {
            return super.hurt(source, amount);
        }

        if (this.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)) {
            return super.hurt(source, amount);
        }

        if (source.getEntity() instanceof LivingEntity attacker) {
            // Only treat as a "physical hit" if the direct entity is the attacker (not arrows, potions, etc.)
            if (source.getDirectEntity() == attacker) {
                ItemStack weapon = attacker.getMainHandItem();
                if (!weapon.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SMITE, weapon) > 0) {
                    return super.hurt(source, amount);
                }
            }
        }

        if (this.isNormallyHittable()) {
            if (source.getEntity() instanceof Player) {
                return super.hurt(source, amount);
            }
        }

        if (source.getDirectEntity() instanceof Arrow arrow) {
            // tipped arrows should damage
            if (arrow.potion != Potions.EMPTY) {
                return super.hurt(source, amount);
            }
            // non-tipped arrows should not damage
            return false;
        }

        if (!source.is(DNLTags.HOLLOW_HURTABLE)) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            boolean valid = isValidHollowKill(source);

            if (!valid) {

                vanishPoof();
                return;
            }
        }

        super.die(source);
    }

    private boolean isValidHollowKill(DamageSource source) {
        // tipped arrow impact kill
        if (source.getDirectEntity() instanceof Arrow arrow && arrow.potion != Potions.EMPTY) {
            return true;
        }

        // potion-related damage types (splash/lingering/dragon breath etc)
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (source.getDirectEntity() instanceof Arrow arrow && arrow.potion != Potions.EMPTY) {

            LootTable table = serverLevel.getServer().getLootData()
                    .getLootTable(ResourceLocation.fromNamespaceAndPath("dungeonnowloading", "entities/hollow_tipped_arrow_kill"));

            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, this)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, source.getEntity())
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, source.getDirectEntity())
                    .create(LootContextParamSets.ENTITY);

            for (ItemStack stack : table.getRandomItems(params)) {
                this.spawnAtLocation(stack);
            }
        }
    }

    private void vanishPoof() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.remove(RemovalReason.KILLED);
            return;
        }

        serverLevel.sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + this.getBbHeight() * 0.5, this.getZ(),
                20,
                this.getBbWidth() * 0.3, this.getBbHeight() * 0.3, this.getBbWidth() * 0.3,
                0.02
        );

        this.level().playSound(
                null,
                this.blockPosition(),
                this.getDeathSound(),
                SoundSource.HOSTILE,
                1.0F,
                1.0F
        );

        this.remove(RemovalReason.KILLED);
    }

    public static void playExtinguishFx(ServerLevel level, Vec3 pos) {
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 1.0F);
        level.sendParticles(ParticleTypes.SMOKE,
                pos.x, pos.y, pos.z,
                8, 0.12, 0.08, 0.12, 0.0
        );
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return DNLSounds.HOLLOW_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DNLSounds.HOLLOW_DEATH.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return DNLSounds.HOLLOW_HURT.get();
    }

    @Override
    public boolean causeFallDamage(float $$0, float $$1, DamageSource $$2) {
        return false;
    }

    public boolean IsCharging() { return this.entityData.get(CHARGING); }

    public void setCharging(boolean b) { this.entityData.set(CHARGING, b); }

    public boolean isInLight() {
        return this.level().getBrightness(LightLayer.BLOCK, this.blockPosition()) > 4;
    }

    public boolean isNormallyHittable() {
        return isInLight();
    }

    public boolean isChargeOnCooldown() {
        return chargeCooldownTicks > 0;
    }

    public boolean canStartCharge() {
        return !isChargeOnCooldown();
    }

    public void startChargeCooldown(int ticks) {
        this.setCharging(false);
        this.chargeActiveTicks = 0;
        this.chargeCooldownTicks = Math.max(this.chargeCooldownTicks, ticks); // don't shorten an existing longer cooldown
    }

    public void startChargeCooldownDefault() {
        startChargeCooldown(CHARGE_COOLDOWN_TICKS); // 5s
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
}
