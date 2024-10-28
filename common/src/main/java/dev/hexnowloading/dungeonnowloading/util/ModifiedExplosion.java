package dev.hexnowloading.dungeonnowloading.util;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModifiedExplosion extends Explosion {

    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    private final boolean fire;
    private final Explosion.BlockInteraction blockInteraction;
    private final RandomSource random = RandomSource.create();
    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    @javax.annotation.Nullable
    private final Entity source;
    private final float radius;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final ObjectArrayList<BlockPos> toBlow = new ObjectArrayList<>();
    private final SoundEvent soundEvent;

    public ModifiedExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double x, double y, double z, float radius, boolean causesFire, BlockInteraction blockInteraction, SoundEvent soundEvent) {
        super(level, source, damageSource, explosionDamageCalculator, x, y, z, radius, causesFire, blockInteraction);
        this.blockInteraction = blockInteraction;
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.source = source;
        this.radius = radius;
        this.fire = causesFire;
        this.damageSource = damageSource == null ? level.damageSources().explosion(this) : damageSource;
        this.damageCalculator = explosionDamageCalculator == null ? this.makeDamageCalculator(source) : explosionDamageCalculator;
        this.soundEvent = soundEvent;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@javax.annotation.Nullable Entity $$0) {
        return (ExplosionDamageCalculator)($$0 == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator($$0));
    }

    private Explosion.BlockInteraction getDestroyType(GameRules.Key<GameRules.BooleanValue> gameRules) {
        return level.getGameRules().getBoolean(gameRules) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }

    public Explosion blockExplosion(double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, SoundEvent soundEvent) {
        return this.blockExplosion(null ,null, null, x, y, z, radius, fire, explosionInteraction, true, soundEvent);
    }

    public Explosion blockExplosion(double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, boolean spawnParticles, SoundEvent soundEvent) {
        return this.blockExplosion(null ,null, null, x, y, z, radius, fire, explosionInteraction, spawnParticles, soundEvent);
    }

    public Explosion blockExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator explosionDamageCalculator, double x, double y, double z, float radius, boolean fire, Level.ExplosionInteraction explosionInteraction, boolean spawnParticles, SoundEvent soundEvent) {
        Explosion.BlockInteraction interaction = switch (explosionInteraction) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> this.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                    ? this.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
                    : Explosion.BlockInteraction.KEEP;
            case TNT -> this.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
        };
        ModifiedExplosion modifiedExplosion = new ModifiedExplosion(level, entity, damageSource, explosionDamageCalculator, x, y, z, radius, fire, interaction, soundEvent);
        modifiedExplosion.explode();
        modifiedExplosion.finalizeExplosion(spawnParticles);
        return modifiedExplosion;
    }

    public Explosion blockExplosionV2(ModifiedExplosion modifiedExplosion) {
        modifiedExplosion.explode();
        modifiedExplosion.finalizeExplosion(true);
        return modifiedExplosion;
    }

    public static void blockSourceExplosion(Level level, Block block, double x, double y, double z, float radius, BlockInteraction interaction, SoundEvent soundEvent) {
        ModifiedExplosion modifiedExplosion = new ModifiedExplosion(level, null, null, null, x, y, z, radius, false, interaction, soundEvent);
        modifiedExplosion.explode();
        modifiedExplosion.finalizeExplosion(true);
    }

    @Override
    public void finalizeExplosion(boolean spawnParticle) {
        if (this.level.isClientSide) {
            this.level
                    .playLocalSound(
                            this.x,
                            this.y,
                            this.z,
                            this.soundEvent,
                            SoundSource.BLOCKS,
                            4.0F,
                            (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F,
                            false
                    );
        }

        boolean $$1 = this.interactsWithBlocks();
        if (spawnParticle) {
            if (!(this.radius < 2.0F) && $$1) {
                this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            } else {
                this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
            }
        }

        if ($$1) {
            ObjectArrayList<Pair<ItemStack, BlockPos>> $$2 = new ObjectArrayList<>();
            boolean $$3 = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(this.toBlow, this.level.random);

            for (BlockPos $$4 : this.toBlow) {
                BlockState $$5 = this.level.getBlockState($$4);
                Block $$6 = $$5.getBlock();
                if (!$$5.isAir()) {
                    BlockPos $$7 = $$4.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if ($$6.dropFromExplosion(this)) {
                        Level $$9 = this.level;
                        if ($$9 instanceof ServerLevel) {
                            ServerLevel $$8 = (ServerLevel)$$9;
                            BlockEntity $$9x = $$5.hasBlockEntity() ? this.level.getBlockEntity($$4) : null;
                            LootParams.Builder $$10 = new LootParams.Builder($$8)
                                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf($$4))
                                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, $$9x)
                                    .withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                            if (this.blockInteraction == BlockInteraction.DESTROY_WITH_DECAY) {
                                $$10.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                            }

                            $$5.spawnAfterBreak($$8, $$4, ItemStack.EMPTY, $$3);
                            $$5.getDrops($$10).forEach($$2x -> addBlockDrops($$2, $$2x, $$7));
                        }
                    }

                    this.level.setBlock($$4, Blocks.AIR.defaultBlockState(), 3);
                    $$6.wasExploded(this.level, $$4, this);
                    this.level.getProfiler().pop();
                }
            }

            for (Pair<ItemStack, BlockPos> $$11 : $$2) {
                Block.popResource(this.level, $$11.getSecond(), $$11.getFirst());
            }
        }

        if (this.fire) {
            for (BlockPos $$12 : this.toBlow) {
                if (this.random.nextInt(3) == 0
                        && this.level.getBlockState($$12).isAir()
                        && this.level.getBlockState($$12.below()).isSolidRender(this.level, $$12.below())) {
                    this.level.setBlockAndUpdate($$12, BaseFireBlock.getState(this.level, $$12));
                }
            }
        }
    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> $$0, ItemStack $$1, BlockPos $$2) {
        int $$3 = $$0.size();

        for (int $$4 = 0; $$4 < $$3; $$4++) {
            Pair<ItemStack, BlockPos> $$5 = $$0.get($$4);
            ItemStack $$6 = $$5.getFirst();
            if (ItemEntity.areMergable($$6, $$1)) {
                ItemStack $$7 = ItemEntity.merge($$6, $$1, 16);
                $$0.set($$4, Pair.of($$7, $$5.getSecond()));
                if ($$1.isEmpty()) {
                    return;
                }
            }
        }

        $$0.add(Pair.of($$1, $$2));
    }
}
