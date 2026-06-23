package dev.hexnowloading.dungeonnowloading.util;

import com.mojang.datafixers.util.Pair;
import dev.hexnowloading.dungeonnowloading.registry.DNLGameEvents;
import dev.hexnowloading.dungeonnowloading.util.event_managers.ExplosionDestructionManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class DNLCustomExplosion {
    private static final ExplosionDamageCalculator DEFAULT_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private static final float RAY_STEP = 0.3F;

    private DNLCustomExplosion() {
    }

    public static Explosion explode(ServerLevel level, Vec3 center, Settings settings) {
        DamageSource damageSource = settings.damageSource != null
                ? settings.damageSource
                : level.damageSources().explosion(settings.source, settings.source);
        Explosion.BlockInteraction blockInteraction = getBlockInteraction(level, settings.explosionInteraction);
        ExplosionDamageCalculator damageCalculator = settings.damageCalculator != null
                ? settings.damageCalculator
                : makeDamageCalculator(settings.source);
        Explosion explosion = new Explosion(
                level,
                settings.source,
                damageSource,
                damageCalculator,
                center.x,
                center.y,
                center.z,
                settings.radius,
                settings.causesFire,
                blockInteraction,
                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                settings.sound
        );

        ObjectArrayList<BlockPos> toBlow = calculateBlocks(level, explosion, damageCalculator, center, settings.radius, blockInteraction);
        damageEntities(level, explosion, damageSource, center, settings);
        finalizeExplosion(level, explosion, toBlow, center, settings, blockInteraction);
        return explosion;
    }

    public static Settings settings(float radius) {
        return new Settings(radius);
    }

    private static ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity source) {
        return source == null ? DEFAULT_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(source);
    }

    private static Explosion.BlockInteraction getBlockInteraction(ServerLevel level, Level.ExplosionInteraction explosionInteraction) {
        return switch (explosionInteraction) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> getDestroyType(level, GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                    ? getDestroyType(level, GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
                    : Explosion.BlockInteraction.KEEP;
            case TNT -> getDestroyType(level, GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
            case TRIGGER -> Explosion.BlockInteraction.TRIGGER_BLOCK;
        };
    }

    private static Explosion.BlockInteraction getDestroyType(ServerLevel level, GameRules.Key<GameRules.BooleanValue> gameRules) {
        return level.getGameRules().getBoolean(gameRules) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }

    private static ObjectArrayList<BlockPos> calculateBlocks(ServerLevel level, Explosion explosion, ExplosionDamageCalculator damageCalculator, Vec3 center, float radius, Explosion.BlockInteraction blockInteraction) {
        ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
        if (blockInteraction == Explosion.BlockInteraction.KEEP || radius < 0.1F) {
            return toBlow;
        }

        Set<BlockPos> positions = new HashSet<>();
        RandomSource random = level.random;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (x != 0 && x != 15 && y != 0 && y != 15 && z != 0 && z != 15) {
                        continue;
                    }

                    double directionX = (x / 15.0F * 2.0F - 1.0F);
                    double directionY = (y / 15.0F * 2.0F - 1.0F);
                    double directionZ = (z / 15.0F * 2.0F - 1.0F);
                    double length = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
                    directionX /= length;
                    directionY /= length;
                    directionZ /= length;

                    float power = radius * (0.7F + random.nextFloat() * 0.6F);
                    double rayX = center.x;
                    double rayY = center.y;
                    double rayZ = center.z;

                    while (power > 0.0F) {
                        BlockPos blockPos = BlockPos.containing(rayX, rayY, rayZ);
                        BlockState blockState = level.getBlockState(blockPos);
                        FluidState fluidState = level.getFluidState(blockPos);

                        if (!blockState.isAir() || !fluidState.isEmpty()) {
                            Optional<Float> resistance = damageCalculator.getBlockExplosionResistance(explosion, level, blockPos, blockState, fluidState);
                            if (resistance.isPresent()) {
                                power -= (resistance.get() + 0.3F) * RAY_STEP;
                            }
                        }

                        if (power > 0.0F && damageCalculator.shouldBlockExplode(explosion, level, blockPos, blockState, power)) {
                            positions.add(blockPos);
                        }

                        rayX += directionX * RAY_STEP;
                        rayY += directionY * RAY_STEP;
                        rayZ += directionZ * RAY_STEP;
                        power -= RAY_STEP * 0.75F;
                    }
                }
            }
        }

        toBlow.addAll(positions);
        return toBlow;
    }

    private static void damageEntities(ServerLevel level, Explosion explosion, DamageSource damageSource, Vec3 center, Settings settings) {
        float diameter = settings.radius * 2.0F;
        if (diameter <= 0.0F) {
            return;
        }

        int minX = Mth.floor(center.x - diameter - 1.0D);
        int maxX = Mth.floor(center.x + diameter + 1.0D);
        int minY = Mth.floor(center.y - diameter - 1.0D);
        int maxY = Mth.floor(center.y + diameter + 1.0D);
        int minZ = Mth.floor(center.z - diameter - 1.0D);
        int maxZ = Mth.floor(center.z + diameter + 1.0D);
        AABB bounds = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

        for (Entity entity : level.getEntities(settings.source, bounds)) {
            if (entity.ignoreExplosion(explosion)) {
                continue;
            }

            double distanceRatio = Math.sqrt(entity.distanceToSqr(center)) / diameter;
            if (distanceRatio > 1.0D) {
                continue;
            }

            double x = entity.getX() - center.x;
            double y = (entity instanceof LivingEntity ? entity.getEyeY() : entity.getY()) - center.y;
            double z = entity.getZ() - center.z;
            double length = Math.sqrt(x * x + y * y + z * z);
            if (length == 0.0D) {
                continue;
            }

            x /= length;
            y /= length;
            z /= length;

            double exposure = Explosion.getSeenPercent(center, entity);
            double impact = (1.0D - distanceRatio) * exposure;
            float damage = settings.damageMode.calculateDamage(impact, diameter, settings.maxDamage);
            if (damage > 0.0F) {
                entity.hurt(damageSource, damage);
            }

            // 1.21 removed ProtectionEnchantment#getExplosionKnockbackAfterDampener;
            // Blast Protection knockback reduction is now applied via enchantment
            // effect components by the engine, so use the raw impact here.
            double knockback = impact;
            entity.setDeltaMovement(entity.getDeltaMovement().add(x * knockback, y * knockback, z * knockback));
        }
    }

    private static void finalizeExplosion(ServerLevel level, Explosion explosion, ObjectArrayList<BlockPos> toBlow, Vec3 center, Settings settings, Explosion.BlockInteraction blockInteraction) {
        if (settings.sound != null) {
            level.playSound(null, center.x, center.y, center.z, settings.sound.value(), settings.soundSource, settings.soundVolume, settings.soundPitch);
        }

        if (settings.particle != null && settings.particleCount > 0) {
            level.sendParticles(settings.particle, center.x, center.y, center.z, settings.particleCount, settings.particleOffsetX, settings.particleOffsetY, settings.particleOffsetZ, settings.particleSpeed);
        }

        if (blockInteraction != Explosion.BlockInteraction.KEEP) {
            destroyBlocks(level, explosion, toBlow, settings, blockInteraction);
        }

        if (settings.causesFire) {
            RandomSource random = level.random;
            for (BlockPos blockPos : toBlow) {
                if (random.nextInt(3) == 0 && level.getBlockState(blockPos).isAir() && level.getBlockState(blockPos.below()).isSolidRender(level, blockPos.below())) {
                    level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(level, blockPos));
                }
            }
        }
    }

    private static void destroyBlocks(ServerLevel level, Explosion explosion, ObjectArrayList<BlockPos> toBlow, Settings settings, Explosion.BlockInteraction blockInteraction) {
        ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
        boolean playerSource = explosion.getIndirectSourceEntity() instanceof Player;
        Util.shuffle(toBlow, level.random);

        for (BlockPos blockPos : toBlow) {
            BlockState blockState = level.getBlockState(blockPos);
            ExplosionDestructionManager.reset();
            level.gameEvent(null, DNLGameEvents.holder(DNLGameEvents.BLOCK_DESTROYED_BY_EXPLOSION), Vec3.atCenterOf(blockPos));
            if (blockState.isAir() || ExplosionDestructionManager.shouldCancel(blockPos)) {
                continue;
            }

            Block block = blockState.getBlock();
            BlockPos immutablePos = blockPos.immutable();
            level.getProfiler().push("explosion_blocks");
            if (block.dropFromExplosion(explosion)) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                LootParams.Builder lootParams = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, settings.source);
                if (blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                    lootParams.withParameter(LootContextParams.EXPLOSION_RADIUS, settings.radius);
                }

                blockState.spawnAfterBreak(level, blockPos, ItemStack.EMPTY, playerSource);
                blockState.getDrops(lootParams).forEach(itemStack -> addBlockDrops(drops, itemStack, immutablePos));
            }

            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            block.wasExploded(level, blockPos, explosion);
            level.getProfiler().pop();
        }

        for (Pair<ItemStack, BlockPos> drop : drops) {
            Block.popResource(level, drop.getSecond(), drop.getFirst());
        }

        for (BlockPos pos : ExplosionDestructionManager.consumePendingBlockUpdates()) {
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos) {
        int size = drops.size();

        for (int i = 0; i < size; i++) {
            Pair<ItemStack, BlockPos> drop = drops.get(i);
            ItemStack existing = drop.getFirst();
            if (ItemEntity.areMergable(existing, stack)) {
                ItemStack merged = ItemEntity.merge(existing, stack, 16);
                drops.set(i, Pair.of(merged, drop.getSecond()));
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        drops.add(Pair.of(stack, pos));
    }

    public enum DamageMode {
        VANILLA {
            @Override
            float calculateDamage(double impact, float diameter, float maxDamage) {
                return (float) ((impact * impact + impact) / 2.0D * 7.0D * diameter + 1.0D);
            }
        },
        CAPPED {
            @Override
            float calculateDamage(double impact, float diameter, float maxDamage) {
                return (float) (maxDamage * ((impact * impact + impact) / 2.0D));
            }
        };

        abstract float calculateDamage(double impact, float diameter, float maxDamage);
    }

    public static final class Settings {
        private final float radius;
        @Nullable
        private Entity source;
        @Nullable
        private DamageSource damageSource;
        @Nullable
        private ExplosionDamageCalculator damageCalculator;
        private Level.ExplosionInteraction explosionInteraction = Level.ExplosionInteraction.NONE;
        private boolean causesFire;
        private DamageMode damageMode = DamageMode.VANILLA;
        private float maxDamage = 8.0F;
        @Nullable
        private ParticleOptions particle = ParticleTypes.EXPLOSION;
        private int particleCount = 1;
        private double particleOffsetX;
        private double particleOffsetY;
        private double particleOffsetZ;
        private double particleSpeed;
        @Nullable
        private net.minecraft.core.Holder<SoundEvent> sound = SoundEvents.GENERIC_EXPLODE;
        private SoundSource soundSource = SoundSource.BLOCKS;
        private float soundVolume = 4.0F;
        private float soundPitch = 0.7F;

        private Settings(float radius) {
            this.radius = radius;
        }

        public Settings source(@Nullable Entity source) {
            this.source = source;
            return this;
        }

        public Settings damageSource(@Nullable DamageSource damageSource) {
            this.damageSource = damageSource;
            return this;
        }

        public Settings damageCalculator(@Nullable ExplosionDamageCalculator damageCalculator) {
            this.damageCalculator = damageCalculator;
            return this;
        }

        public Settings explosionInteraction(Level.ExplosionInteraction explosionInteraction) {
            this.explosionInteraction = explosionInteraction;
            return this;
        }

        public Settings causesFire(boolean causesFire) {
            this.causesFire = causesFire;
            return this;
        }

        public Settings vanillaDamage() {
            this.damageMode = DamageMode.VANILLA;
            return this;
        }

        public Settings cappedDamage(float maxDamage) {
            this.damageMode = DamageMode.CAPPED;
            this.maxDamage = maxDamage;
            return this;
        }

        public Settings particle(@Nullable ParticleOptions particle, int count) {
            this.particle = particle;
            this.particleCount = count;
            return this;
        }

        public Settings particle(@Nullable ParticleOptions particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
            this.particle = particle;
            this.particleCount = count;
            this.particleOffsetX = offsetX;
            this.particleOffsetY = offsetY;
            this.particleOffsetZ = offsetZ;
            this.particleSpeed = speed;
            return this;
        }

        public Settings sound(@Nullable SoundEvent sound) {
            this.sound = sound == null ? null : net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
            return this;
        }

        public Settings sound(@Nullable SoundEvent sound, SoundSource soundSource, float volume, float pitch) {
            this.sound = sound == null ? null : net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
            this.soundSource = soundSource;
            this.soundVolume = volume;
            this.soundPitch = pitch;
            return this;
        }
    }
}
