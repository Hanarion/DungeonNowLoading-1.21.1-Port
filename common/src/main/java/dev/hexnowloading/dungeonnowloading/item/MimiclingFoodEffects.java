package dev.hexnowloading.dungeonnowloading.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.hexnowloading.dungeonnowloading.entity.misc.MimiclingFallingBlockEntity;
import dev.hexnowloading.dungeonnowloading.particle.type.SnifferTrailParticleType;
import dev.hexnowloading.dungeonnowloading.registry.DNLParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MimiclingFoodEffects {
    private static final String MEMORY_TAG = "MimiclingFoodEffectMemory";
    private static final String DURABILITY_DRAIN_TAG = "MimiclingDurabilityDrainTimers";
    private static final String TELEPORT_DESTINATION_TAG = "teleport_destination";
    private static final String MAGMA_CREAM_ID = "minecraft:magma_cream";
    private static final String ENDER_EYE_ID = "minecraft:ender_eye";
    private static final int HELD_EFFECT_TICKS = 40;
    private static final double DEFAULT_REACH_DISTANCE = 4.5D;
    private static final double CREATIVE_REACH_DISTANCE = 5.0D;
    private static final double DEFAULT_ENTITY_REACH_DISTANCE = 3.0D;

    private MimiclingFoodEffects() {}

    public static void tickHeld(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }

        tickDurabilityDrainEffects(stack, level);

        if (!isHeld(player, stack)) {
            return;
        }

        if (player.getMainHandItem() == stack && MimiclingFoods.hasActiveEffect(stack, "while_in_hand", "apply_potion_effect")) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, HELD_EFFECT_TICKS, 0, true, false, true));
        }
    }

    public static void onBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, LivingEntity entity) {
        if (level.isClientSide) {
            return;
        }

        playEnderEyeUseEffects(stack, level, entity, Vec3.atCenterOf(pos), getVanillaBlockReachDistance(entity));

        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("memorize_position", null) && formsMatch(stack, effect.data())) {
                memorizePosition(stack, level, effect.data(), pos);
            } else if (effect.matches("on_break", "auto_switch_unsuited_tool")) {
                MimiclingItem.tryTransformHeldOrEquippedToForm(stack, entity, MimiclingItem.getWorstFormFor(stack, state), MimiclingItem.getBlockUseDurabilityCost(stack), true);
            } else if (effect.matches("on_break", "trail_to_matching_block") && formsMatch(stack, effect.data())) {
                if (spawnTrailToMatchingBlock(level, pos, state, effect.data())) {
                    consumeEffectUsage(stack, effect);
                }
            } else if (effect.matches("change_break_area", null) && formsMatch(stack, effect.data())) {
                breakMatchingNeighborBlocks(stack, level, pos, state, entity, effect.data());
            } else if (effect.matches("change_drop", "auto_smelt") && formsMatch(stack, effect.data())) {
                consumeAutoSmeltUsageAfterBreak(stack, level, state);
            } else if (effect.matches("while_in_hand", "remove_underwater_mining_penalty") && formsMatch(stack, effect.data())) {
                consumeUnderwaterMiningPenaltyUsage(stack, entity, effect);
            } else if (effect.matches("on_break", "grant_air") && formsMatch(stack, effect.data())) {
                if (grantAir(entity, effect.data())) {
                    consumeEffectUsage(stack, effect);
                }
            }
        }
    }

    public static void onAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide) {
            return;
        }

        playEnderEyeUseEffects(stack, attacker.level(), attacker, target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), DEFAULT_ENTITY_REACH_DISTANCE);

        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("change_mob_position", "teleport_to_memorized_position")) {
                teleportTarget(stack, target, effect.data());
                consumeEffectUsage(stack, effect);
            } else if (effect.matches("change_mob_position", "swap_positions")) {
                if (swapPositions(attacker, target)) {
                    consumeEffectUsage(stack, effect);
                }
            } else if (effect.matches("on_attack", "auto_switch_unsuited_tool")) {
                MimiclingItem.tryTransformHeldOrEquippedToForm(stack, attacker, MimiclingItem.getWorstCombatForm(stack), MimiclingItem.getAttackUseDurabilityCost(stack), true);
                consumeEffectUsage(stack, effect);
            } else if (effect.matches("apply_non_potion_effect", "jump_up") && formsMatch(stack, effect.data())) {
                jumpTarget(target, effect.data());
                consumeEffectUsage(stack, effect);
            } else if (effect.matches("apply_potion_effect", "inflict_status_effect") && formsMatch(stack, effect.data())) {
                inflictStatusEffects(stack, target, effect.data());
                consumeEffectUsage(stack, effect);
            }
        }
        convertActiveStatusEffects(stack, target);
    }

    public static boolean hasUnderwaterMiningSpeedEffect(Player player) {
        return hasMimiclingEffect(player.getMainHandItem(), "while_in_hand", "remove_underwater_mining_penalty");
    }

    public static double getMimiclingReachDistance(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!isMimiclingStack(stack)) {
            return DEFAULT_REACH_DISTANCE;
        }

        double reach = DEFAULT_REACH_DISTANCE;
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("while_in_hand", "extend_reach") && formsMatch(stack, effect.data())) {
                double configuredReach = effect.data().has("range") ? effect.data().get("range").getAsDouble() : 16.0D;
                reach = Math.max(reach, Math.max(DEFAULT_REACH_DISTANCE, configuredReach));
            }
        }
        return reach;
    }

    public static double getMimiclingReachDistanceSqr(Player player) {
        double reach = getMimiclingReachDistance(player);
        return reach * reach;
    }

    public static double getMimiclingReachValidationDistanceSqr(Player player) {
        double reach = getMimiclingReachDistance(player) + 1.0D;
        return reach * reach;
    }

    public static boolean hasExtendedReach(Player player) {
        return getMimiclingReachDistance(player) > DEFAULT_REACH_DISTANCE;
    }

    public static boolean hasEnderEyeReachEffect(Player player) {
        ItemStack stack = player.getMainHandItem();
        return isMimiclingStack(stack)
                && hasActiveFood(stack, ENDER_EYE_ID)
                && getConfiguredEffect(stack, "while_in_hand", "extend_reach") != null;
    }

    public static float getUnderwaterMiningSpeedMultiplier(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!isMimiclingStack(stack)) {
            return 1.0F;
        }

        float multiplier = 1.0F;
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (!effect.matches("while_in_hand", "remove_underwater_mining_penalty") || !formsMatch(stack, effect.data())) {
                continue;
            }

            float reduction = effect.data().has("penalty_reduction") ? effect.data().get("penalty_reduction").getAsFloat() : 1.0F;
            multiplier = Math.max(multiplier, 1.0F + Mth.clamp(reduction, 0.0F, 1.0F) * 4.0F);
        }
        return multiplier;
    }

    private static void consumeUnderwaterMiningPenaltyUsage(ItemStack stack, LivingEntity entity, MimiclingFoods.EffectDefinition effect) {
        if (entity instanceof Player player
                && player.getMainHandItem() == stack
                && player.isEyeInFluid(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(player)) {
            if (MimiclingFoods.consumeUsage(stack, effect.ownerId(), 1)) {
                MimiclingFoods.markUsageHandled(stack, effect.ownerId());
            }
        }
    }

    private static void consumeEffectUsage(ItemStack stack, MimiclingFoods.EffectDefinition effect) {
        if (MimiclingFoods.isUsageHandled(stack, effect.ownerId())) {
            return;
        }

        if (MimiclingFoods.consumeUsage(stack, effect.ownerId(), 1)) {
            MimiclingFoods.markUsageHandled(stack, effect.ownerId());
        }
    }

    private static void tickDurabilityDrainEffects(ItemStack stack, Level level) {
        if (!stack.isDamageableItem()) {
            return;
        }

        long gameTime = level.getGameTime();
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (!effect.matches("while_in_inventory", "drain_durability_over_time")) {
                continue;
            }

            int interval = effect.data().has("interval_ticks") ? Math.max(1, effect.data().get("interval_ticks").getAsInt()) : 20;
            String timerTag = effect.ownerId();
            CompoundTag timers = stack.getOrCreateTag().getCompound(DURABILITY_DRAIN_TAG);
            long nextTick = timers.getLong(timerTag);
            if (nextTick <= 0L) {
                timers.putLong(timerTag, gameTime + interval);
                stack.getOrCreateTag().put(DURABILITY_DRAIN_TAG, timers);
                continue;
            }
            if (gameTime < nextTick) {
                continue;
            }

            int amount = effect.data().has("amount") ? Math.max(1, effect.data().get("amount").getAsInt()) : 1;
            stack.setDamageValue(Math.min(stack.getMaxDamage(), stack.getDamageValue() + amount));
            timers.putLong(timerTag, gameTime + interval);
            stack.getOrCreateTag().put(DURABILITY_DRAIN_TAG, timers);
            MimiclingFoods.consumeUsage(stack, effect.ownerId(), 1);
        }
    }

    public static boolean tryTransformSmeltedBlockBeforeBreak(ServerLevel level, BlockPos pos, ItemStack tool) {
        if (!isMimiclingStack(tool) || !hasConfiguredEffect(tool, "change_drop", "auto_smelt")) {
            return false;
        }
        if (hasConfiguredEffect(tool, "change_break_area", null)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (state.is(Blocks.CLAY)) {
            return false;
        }

        ItemStack input = new ItemStack(state.getBlock());
        if (input.isEmpty()) {
            return false;
        }

        SimpleContainer container = new SimpleContainer(input);
        boolean transformed = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> applyBlockSmeltingTransform(level, pos, tool, container, recipe))
                .orElse(false);
        if (transformed) {
            consumeAutoSmeltUsage(tool, true);
        }
        return transformed;
    }

    public static List<ItemStack> transformBlockDrops(BlockState state, LootParams.Builder builder, List<ItemStack> originalDrops) {
        ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
        if (!isMimiclingStack(tool)) {
            return originalDrops;
        }

        DropTransformResult configuredDrops = applyConfiguredDropEffects(tool, state, builder.getLevel(), originalDrops);
        List<ItemStack> drops = configuredDrops.drops();
        if (configuredDrops.replacedOriginal()) {
            if (dropLootAsFallingBlocksIfConfigured(tool, state, builder, drops)) {
                return List.of();
            }
            return collectDropsToInventoryIfConfigured(tool, builder, drops);
        }

        ServerLevel level = builder.getLevel();
        Vec3 origin = builder.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) {
            origin = Vec3.ZERO;
        }

        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(tool)) {
            JsonObject data = effect.data();
            if (!effect.matches("change_drop", null) || !formsMatch(tool, data)) {
                continue;
            }

            String action = data.has("action") ? data.get("action").getAsString() : "";
            if ("auto_smelt".equals(action)) {
                List<ItemStack> blockDrops = smeltBlockItemDrop(level, origin, state, tool, drops);
                if (blockDrops != drops) {
                    consumeAutoSmeltUsage(tool, true);
                    if (dropLootAsFallingBlocksIfConfigured(tool, state, builder, blockDrops)) {
                        return List.of();
                    }
                    return collectDropsToInventoryIfConfigured(tool, builder, blockDrops);
                }
                SmeltedDropResult smeltedDrops = smeltDrops(level, origin, drops);
                consumeAutoSmeltUsage(tool, smeltedDrops.changed());
                if (dropLootAsFallingBlocksIfConfigured(tool, state, builder, smeltedDrops.drops())) {
                    return List.of();
                }
                return collectDropsToInventoryIfConfigured(tool, builder, smeltedDrops.drops());
            }
        }

        if (dropLootAsFallingBlocksIfConfigured(tool, state, builder, drops)) {
            return List.of();
        }
        return collectDropsToInventoryIfConfigured(tool, builder, drops);
    }

    private static void consumeAutoSmeltUsage(ItemStack tool, boolean changedDrops) {
        if (changedDrops) {
            if (MimiclingFoods.consumeUsage(tool, MAGMA_CREAM_ID, 1)) {
                MimiclingFoods.markUsageHandled(tool, MAGMA_CREAM_ID);
            }
        }
    }

    private static void consumeAutoSmeltUsageAfterBreak(ItemStack stack, Level level, BlockState state) {
        if (!(level instanceof ServerLevel serverLevel) || MimiclingFoods.isUsageHandled(stack, MAGMA_CREAM_ID)) {
            return;
        }

        if (wouldAutoSmeltDrop(serverLevel, new ItemStack(state.getBlock()))) {
            consumeAutoSmeltUsage(stack, true);
            return;
        }

        for (ItemStack drop : Block.getDrops(state, serverLevel, BlockPos.ZERO, null, null, ItemStack.EMPTY)) {
            if (wouldAutoSmeltDrop(serverLevel, drop)) {
                consumeAutoSmeltUsage(stack, true);
                return;
            }
        }

        consumeAutoSmeltUsage(stack, false);
    }

    private static boolean wouldAutoSmeltDrop(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        SimpleContainer container = new SimpleContainer(stack.copyWithCount(1));
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> !recipe.getResultItem(level.registryAccess()).isEmpty())
                .orElse(false);
    }

    public static boolean shouldSuppressVanillaBlockExperience(BlockState state, ItemStack tool) {
        if (!isMimiclingStack(tool) || !MimiclingItem.getPickaxeForm().equals(getForm(tool))) {
            return false;
        }

        return isGoldOre(state) && hasActiveFood(tool, "minecraft:piglin_head")
                || isOre(state) && hasConfiguredEffect(tool, "change_drop", "auto_smelt");
    }

    public static void onMobDeath(LivingEntity target, Entity attacker) {
        if (target.level().isClientSide || !(target.level() instanceof ServerLevel level) || !(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        for (ItemStack mimicling : getActiveMimiclingStacks(livingAttacker)) {
            for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(mimicling)) {
                if (effect.matches("reference_potion_effect", "death_effect_cloud")) {
                    spawnDeathEffectCloud(target, livingAttacker, level);
                } else if (effect.matches("on_kill", "roll_effect_group")) {
                    if (rollDeathEffectGroup(mimicling, target, level, effect)) {
                        consumeEffectUsage(mimicling, effect);
                    }
                } else if (effect.matches("on_kill", "summon_entities_at_death")) {
                    if (trySummonEntitiesAtDeath(target, level, effect.data())) {
                        consumeEffectUsage(mimicling, effect);
                    }
                } else if (effect.matches("on_kill", "trail_to_matching_entity")) {
                    if (spawnTrailToMatchingEntity(level, target, effect.data())) {
                        consumeEffectUsage(mimicling, effect);
                    }
                }
            }
        }
    }

    private static boolean grantAir(LivingEntity entity, JsonObject data) {
        int bubbles = data.has("bubbles") ? Math.max(1, data.get("bubbles").getAsInt()) : 3;
        int ticks = bubbles * 30;
        int currentAir = entity.getAirSupply();
        int updatedAir = Math.min(entity.getMaxAirSupply(), currentAir + ticks);
        if (updatedAir <= currentAir) {
            return false;
        }

        entity.setAirSupply(updatedAir);
        return true;
    }

    private static boolean dropLootAsFallingBlocksIfConfigured(ItemStack tool, BlockState minedState, LootParams.Builder builder, List<ItemStack> drops) {
        MimiclingFoods.EffectDefinition effect = getConfiguredEffect(tool, "change_drop", "drop_loot_as_falling_blocks");
        if (effect == null || minedState.isAir() || drops.isEmpty()) {
            return false;
        }

        Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        Vec3 origin = builder.getOptionalParameter(LootContextParams.ORIGIN);
        if (!(entity instanceof Player player) || origin == null) {
            return false;
        }

        ServerLevel level = builder.getLevel();
        float damage = Math.max(1.0F, minedState.getDestroySpeed(level, BlockPos.containing(origin)));
        List<FallingLootPayload> payloads = getFallingLootPayloads(minedState, drops);
        if (payloads.isEmpty()) {
            return false;
        }

        JsonObject data = effect.data();
        int radius = data.has("radius") ? Math.max(1, data.get("radius").getAsInt()) : 32;
        List<Mob> candidates = getMobsTargetingPlayer(level, player, radius);
        int spawned = 0;
        spawnFallingBlockTeleportEffects(level, origin);
        for (FallingLootPayload payload : payloads) {
            BlockPos spawnPos = findFallingSpawnPos(level, player, candidates, data, spawned);
            if (spawnPos == null) {
                if (spawned > 0) {
                    consumeEffectUsage(tool, effect);
                }
                return spawned > 0;
            }
            float payloadDamage = Math.max(damage, payload.visualState().getDestroySpeed(level, spawnPos));
            level.addFreshEntity(new MimiclingFallingBlockEntity(level, spawnPos, payload.visualState(), payloadDamage, payload.drops()));
            spawnFallingBlockTeleportEffects(level, Vec3.atCenterOf(spawnPos));
            level.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.65F, 1.0F);
            spawned++;
        }
        consumeEffectUsage(tool, effect);
        return spawned > 0;
    }

    private static void spawnFallingBlockTeleportEffects(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.5D, pos.z, 24, 0.35D, 0.45D, 0.35D, 0.08D);
    }

    private static void playEnderEyeUseEffects(ItemStack stack, Level level, LivingEntity entity, Vec3 target, double vanillaReach) {
        MimiclingFoods.EffectDefinition effect = getConfiguredEffect(stack, "while_in_hand", "extend_reach");
        Vec3 source = getEnderEyeParticleSource(entity);
        if (!(level instanceof ServerLevel serverLevel) || !hasActiveFood(stack, ENDER_EYE_ID) || effect == null || source.distanceToSqr(target) <= vanillaReach * vanillaReach) {
            return;
        }

        spawnPortalLine(serverLevel, source, target);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.75F, 1.0F);
        consumeEffectUsage(stack, effect);
    }

    private static void spawnPortalLine(ServerLevel level, Vec3 source, Vec3 target) {
        Vec3 delta = target.subtract(source);
        double distance = delta.length();
        if (distance < 0.01D) {
            sendEnderEyePortalParticles(level, target, 8, 0.08D, 0.08D, 0.08D, 0.02D);
            return;
        }

        int steps = Math.max(2, Mth.ceil(distance * 4.0D));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = source.add(delta.scale(i / (double)steps));
            sendEnderEyePortalParticles(level, point, 2, 0.035D, 0.035D, 0.035D, 0.01D);
        }
    }

    private static void sendEnderEyePortalParticles(ServerLevel level, Vec3 pos, int count, double xSpread, double ySpread, double zSpread, double speed) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y - 0.75D, pos.z, count, xSpread, ySpread, zSpread, speed);
    }

    private static double getVanillaBlockReachDistance(LivingEntity entity) {
        return entity instanceof Player player && player.isCreative() ? CREATIVE_REACH_DISTANCE : DEFAULT_REACH_DISTANCE;
    }

    private static Vec3 getEnderEyeParticleSource(LivingEntity entity) {
        return entity.getEyePosition(1.0F).subtract(0.0D, 0.22D, 0.0D);
    }

    private static List<FallingLootPayload> getFallingLootPayloads(BlockState minedState, List<ItemStack> drops) {
        List<FallingLootPayload> payloads = new ArrayList<>();
        List<ItemStack> itemDrops = new ArrayList<>();
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            if (drop.getItem() instanceof BlockItem blockItem) {
                for (int i = 0; i < drop.getCount(); i++) {
                    payloads.add(new FallingLootPayload(blockItem.getBlock().defaultBlockState(), List.of(drop.copyWithCount(1))));
                }
            } else {
                itemDrops.add(drop.copy());
            }
        }
        if (!itemDrops.isEmpty()) {
            payloads.add(new FallingLootPayload(minedState, itemDrops));
        }
        return payloads;
    }

    private static List<Mob> getMobsTargetingPlayer(ServerLevel level, Player player, int radius) {
        AABB area = player.getBoundingBox().inflate(radius);
        return new ArrayList<>(level.getEntitiesOfClass(Mob.class, area, mob -> mob.isAlive() && mob.getTarget() == player));
    }

    private static BlockPos findFallingSpawnPos(ServerLevel level, Player player, List<Mob> candidates, JsonObject data, int spawnIndex) {
        if (!candidates.isEmpty()) {
            int start = level.random.nextInt(candidates.size());
            for (int i = 0; i < candidates.size(); i++) {
                Mob target = candidates.get((start + i) % candidates.size());
                if (!target.isAlive() || target.getTarget() != player) {
                    continue;
                }
                BlockPos predicted = predictTargetBlockPos(target, data);
                BlockPos spawnPos = findVerticalDropSpace(level, predicted, data);
                if (spawnPos != null) {
                    return offsetOverlappingSpawn(level, spawnPos, spawnIndex);
                }
            }
        }

        return findRandomFallingSpawnPos(level, player, data, spawnIndex);
    }

    private static BlockPos predictTargetBlockPos(Mob target, JsonObject data) {
        int minHeight = getInt(data, "min_height", 5);
        int maxHeight = Math.max(minHeight, getInt(data, "max_height", 10));
        int height = (minHeight + maxHeight) / 2;
        double fallTicks = Math.sqrt(height / 0.02D);
        Vec3 movement = target.getDeltaMovement();
        double maxLead = data.has("max_lead") ? Math.max(0.0D, data.get("max_lead").getAsDouble()) : 6.0D;
        double leadX = Mth.clamp(movement.x * fallTicks, -maxLead, maxLead);
        double leadZ = Mth.clamp(movement.z * fallTicks, -maxLead, maxLead);
        return BlockPos.containing(target.getX() + leadX, target.getY(), target.getZ() + leadZ);
    }

    private static BlockPos findVerticalDropSpace(ServerLevel level, BlockPos targetPos, JsonObject data) {
        int preferredMin = getInt(data, "min_height", 5);
        int preferredMax = Math.max(preferredMin, getInt(data, "max_height", 10));
        int fallbackMin = Math.min(preferredMin, getInt(data, "fallback_min_height", 3));
        int maxHeight = Math.min(level.getMaxBuildHeight() - targetPos.getY() - 1, preferredMax);
        for (int height = maxHeight; height >= fallbackMin; height--) {
            BlockPos pos = targetPos.above(height);
            if (canSpawnFallingBlockAt(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos findRandomFallingSpawnPos(ServerLevel level, Player player, JsonObject data, int spawnIndex) {
        int radius = getInt(data, "random_radius", 16);
        for (int attempts = 0; attempts < 32; attempts++) {
            int x = Mth.floor(player.getX()) + level.random.nextInt(radius * 2 + 1) - radius;
            int z = Mth.floor(player.getZ()) + level.random.nextInt(radius * 2 + 1) - radius;
            int y = Mth.floor(player.getY()) + level.random.nextInt(9) - 4;
            BlockPos target = new BlockPos(x, y, z);
            BlockPos spawnPos = findVerticalDropSpace(level, target, data);
            if (spawnPos != null) {
                return offsetOverlappingSpawn(level, spawnPos, spawnIndex);
            }
        }
        return null;
    }

    private static BlockPos offsetOverlappingSpawn(ServerLevel level, BlockPos pos, int spawnIndex) {
        if (spawnIndex <= 0) {
            return pos;
        }
        for (int attempts = 0; attempts < 8; attempts++) {
            int distance = 1 + level.random.nextInt(2);
            int x = level.random.nextBoolean() ? distance : -distance;
            int z = level.random.nextBoolean() ? distance : -distance;
            int y = Math.min(3, spawnIndex + attempts / 4);
            BlockPos offset = pos.offset(x, y, z);
            if (canSpawnFallingBlockAt(level, offset)) {
                return offset;
            }
        }
        BlockPos raised = pos.above(Math.min(3, spawnIndex));
        return canSpawnFallingBlockAt(level, raised) ? raised : pos;
    }

    private static boolean canSpawnFallingBlockAt(ServerLevel level, BlockPos pos) {
        if (pos.getY() <= level.getMinBuildHeight() || pos.getY() >= level.getMaxBuildHeight()) {
            return false;
        }
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isAir();
    }

    private static int getInt(JsonObject data, String field, int fallback) {
        if (!data.has(field)) {
            return fallback;
        }
        return data.get(field).getAsInt();
    }

    private record FallingLootPayload(BlockState visualState, List<ItemStack> drops) {}

    private static boolean spawnTrailToMatchingBlock(Level level, BlockPos origin, BlockState state, JsonObject data) {
        if (!(level instanceof ServerLevel serverLevel) || state.isAir()) {
            return false;
        }
        if (hasAdjacentMatchingBlock(level, origin, state)) {
            return false;
        }

        int horizontalRange = data.has("horizontal_range") ? Math.max(1, data.get("horizontal_range").getAsInt()) : 16;
        int verticalRange = data.has("vertical_range") ? Math.max(1, data.get("vertical_range").getAsInt()) : 10;
        BlockPos target = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-horizontalRange, -verticalRange, -horizontalRange), origin.offset(horizontalRange, verticalRange, horizontalRange))) {
            if (candidate.equals(origin)) {
                continue;
            }
            BlockState candidateState = level.getBlockState(candidate);
            if (candidateState.is(state.getBlock())) {
                double distance = candidate.distSqr(origin);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    target = candidate.immutable();
                }
            }
        }

        if (target == null) {
            return false;
        }

        spawnTrailParticles(serverLevel, Vec3.atCenterOf(origin), Vec3.atCenterOf(target), data);
        return true;
    }

    private static boolean hasAdjacentMatchingBlock(Level level, BlockPos origin, BlockState state) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(origin.relative(direction)).is(state.getBlock())) {
                return true;
            }
        }
        return false;
    }

    private static boolean spawnTrailToMatchingEntity(ServerLevel level, LivingEntity killed, JsonObject data) {
        int horizontalRange = data.has("horizontal_range") ? Math.max(1, data.get("horizontal_range").getAsInt()) : 64;
        int verticalRange = data.has("vertical_range") ? Math.max(1, data.get("vertical_range").getAsInt()) : 64;
        AABB area = killed.getBoundingBox().inflate(horizontalRange, verticalRange, horizontalRange);
        LivingEntity target = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity.getType() == killed.getType() && entity != killed)) {
            double distance = candidate.distanceToSqr(killed);
            if (distance < bestDistance) {
                bestDistance = distance;
                target = candidate;
            }
        }

        if (target == null) {
            return false;
        }

        spawnTrailParticles(level, killed.position().add(0.0D, killed.getBbHeight() * 0.5D, 0.0D), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), data);
        return true;
    }

    private static void spawnTrailParticles(ServerLevel level, Vec3 start, Vec3 end, JsonObject data) {
        ResourceLocation particleId = data.has("particle") ? new ResourceLocation(data.get("particle").getAsString()) : null;
        if (particleId != null && DNLParticleTypes.SNIFFER_TRAIL_PARTICLE.get() == BuiltInRegistries.PARTICLE_TYPE.get(particleId)) {
            spawnSnifferTrailParticles(level, start, end, data);
            return;
        }

        ParticleOptions particle = getConfiguredParticle(data, ParticleTypes.HAPPY_VILLAGER);
        int steps = data.has("steps") ? Math.max(2, data.get("steps").getAsInt()) : 24;
        double spread = data.has("spread") ? data.get("spread").getAsDouble() : 0.03D;
        double speed = data.has("speed") ? data.get("speed").getAsDouble() : 0.01D;
        Vec3 delta = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            double progress = i / (double)steps;
            Vec3 point = start.add(delta.scale(progress));
            level.sendParticles(particle, point.x, point.y, point.z, 1, spread, spread, spread, speed);
        }
    }

    private static void spawnSnifferTrailParticles(ServerLevel level, Vec3 start, Vec3 end, JsonObject data) {
        int emitterLifetime = data.has("particle_lifetime") ? Math.max(1, data.get("particle_lifetime").getAsInt()) : 200;
        int particlesPerSecond = data.has("particles_per_second") ? Math.max(1, data.get("particles_per_second").getAsInt()) : 5;
        int count = Math.max(data.has("steps") ? Math.max(1, data.get("steps").getAsInt()) : 1, Math.max(1, emitterLifetime * particlesPerSecond / 20));
        double particleSpeed = data.has("particle_speed") ? Math.max(0.01D, data.get("particle_speed").getAsDouble()) : 0.175D;
        double randomness = data.has("position_randomness") ? Math.max(0.0D, data.get("position_randomness").getAsDouble()) : 0.45D;
        for (int i = 0; i < count; i++) {
            Vec3 startOffset = randomBlockOffset(level, randomness);
            Vec3 endOffset = randomBlockOffset(level, randomness);
            Vec3 particleStart = start.add(startOffset);
            Vec3 particleTarget = end.add(endOffset);
            Vec3 targetOffset = particleTarget.subtract(particleStart);
            int travelLifetime = data.has("particle_travel_lifetime")
                    ? Math.max(1, data.get("particle_travel_lifetime").getAsInt())
                    : Math.max(1, (int)Math.ceil(targetOffset.length() / particleSpeed));
            int delay = count <= 1 ? 0 : Math.round(i * (emitterLifetime - 1) / (float)(count - 1));
            ParticleOptions particle = new SnifferTrailParticleType.Data(
                    DNLParticleTypes.SNIFFER_TRAIL_PARTICLE.get(),
                    (float)targetOffset.x,
                    (float)targetOffset.y,
                    (float)targetOffset.z,
                    travelLifetime,
                    delay
            );
            level.sendParticles(particle, particleStart.x, particleStart.y, particleStart.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static Vec3 randomBlockOffset(ServerLevel level, double amount) {
        return new Vec3(
                (level.random.nextDouble() - 0.5D) * amount,
                (level.random.nextDouble() - 0.5D) * amount,
                (level.random.nextDouble() - 0.5D) * amount
        );
    }

    private static ParticleOptions getConfiguredParticle(JsonObject data, ParticleOptions fallback) {
        if (!data.has("particle")) {
            return fallback;
        }

        return (ParticleOptions)BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(data.get("particle").getAsString()));
    }

    public static void applyTemporaryEnchantmentModifiers(ItemStack stack, ListTag enchantments) {
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("while_in_hand", "modify_enchantment") && formsMatch(stack, effect.data())) {
                applyTemporaryEnchantmentModifier(enchantments, effect.data());
            }
        }
    }

    private static void spawnDeathEffectCloud(LivingEntity target, LivingEntity livingAttacker, ServerLevel level) {
        if (target.getActiveEffects().isEmpty()) {
            return;
        }

        AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        cloud.setOwner(livingAttacker);
        cloud.setRadius(2.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(200);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        for (MobEffectInstance activeEffect : target.getActiveEffects()) {
            cloud.addEffect(new MobEffectInstance(activeEffect));
        }
        level.addFreshEntity(cloud);
    }

    private static boolean rollDeathEffectGroup(ItemStack mimicling, LivingEntity target, ServerLevel level, MimiclingFoods.EffectDefinition effect) {
        JsonObject data = effect.data();
        if (!data.has("entries") || !data.get("entries").isJsonArray()) {
            return false;
        }

        int rolls = data.has("rolls") ? Math.max(1, data.get("rolls").getAsInt()) : 1;
        boolean succeeded = false;
        JsonArray entries = data.getAsJsonArray("entries");
        for (int i = 0; i < rolls; i++) {
            JsonObject rolledEffect = rollEffectEntry(entries, level);
            if (rolledEffect != null && applyDeathEffect(target, level, rolledEffect)) {
                succeeded = true;
            }
        }
        return succeeded;
    }

    private static JsonObject rollEffectEntry(JsonArray entries, ServerLevel level) {
        int totalWeight = 0;
        for (JsonElement element : entries) {
            if (element.isJsonObject()) {
                JsonObject entry = element.getAsJsonObject();
                totalWeight += entry.has("weight") ? Math.max(0, entry.get("weight").getAsInt()) : 1;
            }
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = level.random.nextInt(totalWeight);
        for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject entry = element.getAsJsonObject();
            int weight = entry.has("weight") ? Math.max(0, entry.get("weight").getAsInt()) : 1;
            if (roll < weight) {
                return entry.has("effect") && entry.get("effect").isJsonObject() ? entry.getAsJsonObject("effect") : entry;
            }
            roll -= weight;
        }
        return null;
    }

    private static boolean applyDeathEffect(LivingEntity target, ServerLevel level, JsonObject effect) {
        if (!effect.has("action")) {
            return false;
        }

        String action = effect.get("action").getAsString();
        if ("summon_entities_at_death".equals(action)) {
            return trySummonEntitiesAtDeath(target, level, effect);
        }
        return false;
    }

    private static boolean trySummonEntitiesAtDeath(LivingEntity target, ServerLevel level, JsonObject data) {
        if (isBabyLikeSplitTarget(target)) {
            return false;
        }

        float chance = data.has("chance") ? data.get("chance").getAsFloat() : 1.0F;
        if (level.random.nextFloat() >= chance) {
            return false;
        }

        int count = data.has("count") ? Math.max(1, data.get("count").getAsInt()) : 1;
        JsonArray entities = data.has("entities") && data.get("entities").isJsonArray() ? data.getAsJsonArray("entities") : new JsonArray();
        if (entities.isEmpty()) {
            JsonObject killedEntity = new JsonObject();
            killedEntity.addProperty("type", "killed_entity");
            entities.add(killedEntity);
        }

        List<LivingEntity> summoned = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JsonObject entityData = getEntityEntry(entities, i);
            LivingEntity entity = createConfiguredEntity(entityData, target, level);
            if (entity == null) {
                return false;
            }
            summoned.add(entity);
        }

        Vec3[] motions = getSummonMotions(level, data, summoned.size());
        Vec3 center = target.position();
        for (int i = 0; i < summoned.size(); i++) {
            LivingEntity entity = summoned.get(i);
            entity.moveTo(center.x, center.y, center.z, target.getYRot(), target.getXRot());
            entity.setDeltaMovement(motions[i]);
            entity.hasImpulse = true;
            copyStatusEffectsFromKilledEntity(entity, target, data);
            level.addFreshEntity(entity);
        }
        return true;
    }

    private static boolean isBabyLikeSplitTarget(LivingEntity target) {
        if (target.isBaby()) {
            return true;
        }

        if (target.getType() == EntityType.SLIME || target.getType() == EntityType.MAGMA_CUBE) {
            CompoundTag tag = new CompoundTag();
            target.saveWithoutId(tag);
            return tag.contains("Size") && tag.getInt("Size") <= 0;
        }

        return false;
    }

    private static void copyStatusEffectsFromKilledEntity(LivingEntity summoned, LivingEntity killedEntity, JsonObject data) {
        if (!data.has("copy_status_effects") || killedEntity.getActiveEffects().isEmpty()) {
            return;
        }

        JsonObject copy = data.get("copy_status_effects").isJsonObject() ? data.getAsJsonObject("copy_status_effects") : new JsonObject();
        double durationMultiplier = copy.has("duration_multiplier") ? copy.get("duration_multiplier").getAsDouble() : 0.5D;
        double levelMultiplier = copy.has("level_multiplier") ? copy.get("level_multiplier").getAsDouble() : 0.5D;
        int minDuration = copy.has("min_duration") ? copy.get("min_duration").getAsInt() : 1;
        for (MobEffectInstance activeEffect : killedEntity.getActiveEffects()) {
            int duration = Math.max(minDuration, (int)Math.ceil(activeEffect.getDuration() * durationMultiplier));
            int level = activeEffect.getAmplifier() + 1;
            int copiedLevel = Math.max(1, (int)Math.ceil(level * levelMultiplier));
            summoned.addEffect(new MobEffectInstance(
                    activeEffect.getEffect(),
                    duration,
                    copiedLevel - 1,
                    activeEffect.isAmbient(),
                    activeEffect.isVisible(),
                    activeEffect.showIcon()
            ));
        }
    }

    private static JsonObject getEntityEntry(JsonArray entities, int index) {
        JsonElement element = entities.get(Math.min(index, entities.size() - 1));
        return element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    private static LivingEntity createConfiguredEntity(JsonObject data, LivingEntity target, ServerLevel level) {
        Entity entity;
        String type = data.has("type") ? data.get("type").getAsString() : "killed_entity";
        if ("killed_entity".equals(type)) {
            entity = target.getType().create(level);
        } else {
            entity = EntityType.byString(type).map(entityType -> entityType.create(level)).orElse(null);
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        if (livingEntity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(target.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        }
        if (!applyEntityNbt(livingEntity, data, target)) {
            return null;
        }

        livingEntity.setHealth(livingEntity.getMaxHealth());
        return livingEntity;
    }

    private static boolean applyEntityNbt(LivingEntity entity, JsonObject data, LivingEntity killedEntity) {
        if (data.has("nbt")) {
            applyEntityNbt(entity, data.get("nbt").getAsString());
        }

        if (!data.has("nbt_overrides") || !data.get("nbt_overrides").isJsonArray()) {
            return true;
        }

        ResourceLocation killedEntityId = BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType());
        for (JsonElement element : data.getAsJsonArray("nbt_overrides")) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject override = element.getAsJsonObject();
            if (!override.has("when_killed")) {
                continue;
            }

            if (killedEntityId.equals(new ResourceLocation(override.get("when_killed").getAsString()))) {
                if (override.has("nbt")) {
                    applyEntityNbt(entity, override.get("nbt").getAsString());
                }
                return applyCopiedKilledEntityNbt(entity, killedEntity, override);
            }
        }
        return true;
    }

    private static boolean applyCopiedKilledEntityNbt(LivingEntity entity, LivingEntity killedEntity, JsonObject data) {
        if (!data.has("copy_killed_nbt") || !data.get("copy_killed_nbt").isJsonArray()) {
            return true;
        }

        CompoundTag killedNbt = new CompoundTag();
        killedEntity.saveWithoutId(killedNbt);
        CompoundTag entityNbt = new CompoundTag();
        entity.saveWithoutId(entityNbt);
        for (JsonElement element : data.getAsJsonArray("copy_killed_nbt")) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject copy = element.getAsJsonObject();
            if (!copy.has("from") || !copy.has("to") || !killedNbt.contains(copy.get("from").getAsString())) {
                if (copy.has("fail_unmapped") && copy.get("fail_unmapped").getAsBoolean()) {
                    return false;
                }
                continue;
            }

            String from = copy.get("from").getAsString();
            String to = copy.get("to").getAsString();
            int value = killedNbt.getInt(from);
            if (copy.has("map") && copy.get("map").isJsonObject()) {
                JsonObject map = copy.getAsJsonObject("map");
                String key = Integer.toString(value);
                if (map.has(key)) {
                    value = map.get(key).getAsInt();
                } else if (copy.has("fail_unmapped") && copy.get("fail_unmapped").getAsBoolean()) {
                    return false;
                }
            }
            if (copy.has("subtract")) {
                value -= copy.get("subtract").getAsInt();
            }
            if (copy.has("add")) {
                value += copy.get("add").getAsInt();
            }
            if (copy.has("min")) {
                value = Math.max(copy.get("min").getAsInt(), value);
            }
            if (copy.has("max")) {
                value = Math.min(copy.get("max").getAsInt(), value);
            }
            entityNbt.putInt(to, value);
        }
        entity.load(entityNbt);
        return true;
    }

    private static void applyEntityNbt(LivingEntity entity, String rawNbt) {
        try {
            entity.load(TagParser.parseTag(rawNbt));
        } catch (Exception ignored) {
        }
    }

    private static Vec3[] getSummonMotions(ServerLevel level, JsonObject data, int count) {
        JsonObject motion = data.has("motion") && data.get("motion").isJsonObject() ? data.getAsJsonObject("motion") : new JsonObject();
        double upward = motion.has("upward") ? motion.get("upward").getAsDouble() : 0.25D;
        double horizontal = motion.has("horizontal") ? motion.get("horizontal").getAsDouble() : 0.18D;
        boolean oppositePairs = !motion.has("opposite_pairs") || motion.get("opposite_pairs").getAsBoolean();
        Vec3[] motions = new Vec3[count];
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        Vec3 base = new Vec3(Math.cos(angle) * horizontal, upward, Math.sin(angle) * horizontal);
        for (int i = 0; i < count; i++) {
            if (oppositePairs && i % 2 == 1) {
                motions[i] = new Vec3(-motions[i - 1].x, upward, -motions[i - 1].z);
            } else if (oppositePairs) {
                motions[i] = i == 0 ? base : rotateHorizontal(base, level.random.nextDouble() * Math.PI * 2.0D);
            } else {
                motions[i] = rotateHorizontal(base, level.random.nextDouble() * Math.PI * 2.0D);
            }
        }
        return motions;
    }

    private static Vec3 rotateHorizontal(Vec3 vector, double angle) {
        double horizontal = Math.sqrt(vector.x * vector.x + vector.z * vector.z);
        return new Vec3(Math.cos(angle) * horizontal, vector.y, Math.sin(angle) * horizontal);
    }

    private static void applyTemporaryEnchantmentModifier(ListTag enchantments, JsonObject data) {
        if (!data.has("enchantment")) {
            return;
        }

        String enchantmentId = data.get("enchantment").getAsString();
        int currentLevel = getEnchantmentLevel(enchantments, enchantmentId);
        int level = data.has("level") ? data.get("level").getAsInt() : 1;
        String operation = data.has("operation") ? data.get("operation").getAsString() : "add";
        int modifiedLevel;
        if ("set".equals(operation)) {
            modifiedLevel = level;
        } else if ("remove".equals(operation)) {
            modifiedLevel = currentLevel - level;
        } else {
            modifiedLevel = currentLevel + level;
        }

        setEnchantmentLevel(enchantments, enchantmentId, Math.max(0, modifiedLevel));
    }

    private static int getEnchantmentLevel(ListTag enchantments, String enchantmentId) {
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantment = enchantments.getCompound(i);
            if (enchantmentId.equals(enchantment.getString("id"))) {
                return enchantment.getInt("lvl");
            }
        }
        return 0;
    }

    private static void setEnchantmentLevel(ListTag enchantments, String enchantmentId, int level) {
        for (int i = 0; i < enchantments.size(); i++) {
            CompoundTag enchantment = enchantments.getCompound(i);
            if (enchantmentId.equals(enchantment.getString("id"))) {
                if (level <= 0) {
                    enchantments.remove(i);
                } else {
                    enchantment.putShort("lvl", (short)level);
                }
                return;
            }
        }

        if (level > 0) {
            CompoundTag enchantment = new CompoundTag();
            enchantment.putString("id", enchantmentId);
            enchantment.putShort("lvl", (short)level);
            enchantments.add(enchantment);
        }
    }

    public static void retainMemoriesForActiveFoods(ItemStack stack, List<MimiclingFoods.FoodDefinition> activeFoods) {
        if (!stack.hasTag() || !stack.getTag().contains(MEMORY_TAG)) {
            return;
        }

        Set<String> activeMemoryIds = new HashSet<>();
        for (MimiclingFoods.FoodDefinition food : activeFoods) {
            for (MimiclingFoods.EffectDefinition effect : food.effects()) {
                if (effect.matches("memorize_position", null)) {
                    activeMemoryIds.add(getMemoryId(effect.data()));
                }
            }
        }

        CompoundTag memory = stack.getTag().getCompound(MEMORY_TAG);
        for (String id : new HashSet<>(memory.getAllKeys())) {
            if (!activeMemoryIds.contains(id)) {
                memory.remove(id);
            }
        }

        if (memory.isEmpty()) {
            stack.getTag().remove(MEMORY_TAG);
        } else {
            stack.getTag().put(MEMORY_TAG, memory);
        }
    }

    private static boolean isHeld(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    private static void memorizePosition(ItemStack stack, Level level, JsonObject data, BlockPos pos) {
        String id = getMemoryId(data);
        CompoundTag memory = stack.getOrCreateTag().getCompound(MEMORY_TAG);
        memory.putLong(id, pos.asLong());
        stack.getOrCreateTag().put(MEMORY_TAG, memory);

        spawnMemorySetParticles(level, data, pos);
    }

    private static void spawnMemorySetParticles(Level level, JsonObject data, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || !data.has("particle")) {
            return;
        }

        ParticleOptions particle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(data.get("particle").getAsString()));
        int count = data.has("particle_count") ? data.get("particle_count").getAsInt() : 16;
        double spread = data.has("particle_spread") ? data.get("particle_spread").getAsDouble() : 0.35D;
        double xSpread = data.has("particle_spread_x") ? data.get("particle_spread_x").getAsDouble() : spread;
        double ySpread = data.has("particle_spread_y") ? data.get("particle_spread_y").getAsDouble() : spread;
        double zSpread = data.has("particle_spread_z") ? data.get("particle_spread_z").getAsDouble() : spread;
        double speed = data.has("particle_speed") ? data.get("particle_speed").getAsDouble() : 0.02D;
        serverLevel.sendParticles(particle, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, count, xSpread, ySpread, zSpread, speed);
    }

    private static String getMemoryId(JsonObject data) {
        return data.has("id") ? data.get("id").getAsString() : TELEPORT_DESTINATION_TAG;
    }

    private static void teleportTarget(ItemStack stack, LivingEntity target, JsonObject data) {
        if (isTeleportImmune(target, data)) {
            return;
        }

        int radius = data.has("radius") ? data.get("radius").getAsInt() : 16;
        String memoryId = data.has("memory") ? data.get("memory").getAsString() : TELEPORT_DESTINATION_TAG;
        CompoundTag memory = stack.getOrCreateTag().getCompound(MEMORY_TAG);
        if (memory.contains(memoryId)) {
            BlockPos destination = BlockPos.of(memory.getLong(memoryId));
            if (destination.closerToCenterThan(target.position(), radius)) {
                target.teleportTo(destination.getX() + 0.5D, destination.getY() + 1.0D, destination.getZ() + 0.5D);
                return;
            }
        }

        target.randomTeleport(
                target.getX() + (target.getRandom().nextDouble() - 0.5D) * radius * 2.0D,
                target.getY(),
                target.getZ() + (target.getRandom().nextDouble() - 0.5D) * radius * 2.0D,
                true
        );
    }

    private static boolean swapPositions(LivingEntity attacker, LivingEntity target) {
        if (attacker.level() != target.level() || attacker.isPassenger() || target.isPassenger()) {
            return false;
        }

        double attackerX = attacker.getX();
        double attackerY = attacker.getY();
        double attackerZ = attacker.getZ();
        float attackerYRot = attacker.getYRot();
        float attackerXRot = attacker.getXRot();
        Vec3 attackerMovement = attacker.getDeltaMovement();

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        float targetYRot = target.getYRot();
        float targetXRot = target.getXRot();
        Vec3 targetMovement = target.getDeltaMovement();

        attacker.teleportTo(targetX, targetY, targetZ);
        attacker.setYRot(targetYRot);
        attacker.setXRot(targetXRot);
        attacker.setDeltaMovement(targetMovement);
        attacker.hasImpulse = true;

        target.teleportTo(attackerX, attackerY, attackerZ);
        target.setYRot(attackerYRot);
        target.setXRot(attackerXRot);
        target.setDeltaMovement(attackerMovement);
        target.hasImpulse = true;
        attacker.level().playSound(null, attackerX, attackerY, attackerZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        attacker.level().playSound(null, targetX, targetY, targetZ, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean isTeleportImmune(LivingEntity target, JsonObject data) {
        if (!data.has("immune_entities") || !data.get("immune_entities").isJsonArray()) {
            return false;
        }

        ResourceLocation targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        for (JsonElement element : data.getAsJsonArray("immune_entities")) {
            if (targetId.equals(new ResourceLocation(element.getAsString()))) {
                return true;
            }
        }
        return false;
    }

    private static void jumpTarget(LivingEntity target, JsonObject data) {
        double blocks = data.has("blocks") ? data.get("blocks").getAsDouble() : 5.0D;
        Vec3 movement = target.getDeltaMovement();
        target.setDeltaMovement(movement.x, Math.max(movement.y, blocks * 0.22D), movement.z);
        target.hasImpulse = true;
    }

    private static void inflictStatusEffects(ItemStack stack, LivingEntity target, JsonObject data) {
        if (!data.has("effects") || !data.get("effects").isJsonArray()) {
            return;
        }

        for (JsonElement element : data.getAsJsonArray("effects")) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject effectData = element.getAsJsonObject();
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(effectData.get("effect").getAsString()));
            int duration = effectData.has("duration") ? effectData.get("duration").getAsInt() : 200;
            int amplifier = effectData.has("amplifier") ? effectData.get("amplifier").getAsInt() : 0;
            target.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private static void convertActiveStatusEffects(ItemStack stack, LivingEntity target) {
        for (MobEffectInstance activeEffect : new ArrayList<>(target.getActiveEffects())) {
            JsonObject conversion = findStatusEffectConversion(stack, BuiltInRegistries.MOB_EFFECT.getKey(activeEffect.getEffect()).toString());
            if (conversion == null) {
                continue;
            }

            MobEffect convertedEffect = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation(conversion.get("to").getAsString()));
            int duration = activeEffect.getDuration();
            if (conversion.has("copy_duration") && !conversion.get("copy_duration").getAsBoolean()) {
                duration = conversion.has("duration") ? conversion.get("duration").getAsInt() : 1;
            }
            int amplifier = activeEffect.getAmplifier();
            if (conversion.has("copy_amplifier") && !conversion.get("copy_amplifier").getAsBoolean()) {
                amplifier = conversion.has("amplifier") ? conversion.get("amplifier").getAsInt() : 0;
            }

            target.removeEffect(activeEffect.getEffect());
            target.addEffect(new MobEffectInstance(convertedEffect, duration, amplifier));
        }
    }

    private static JsonObject findStatusEffectConversion(ItemStack stack, String sourceEffectId) {
        for (MimiclingFoods.EffectDefinition converter : MimiclingFoods.getActiveEffects(stack)) {
            if (!converter.matches("reference_potion_effect", "convert_potion_effect") || !converter.data().has("conversions")) {
                continue;
            }

            for (JsonElement conversionElement : converter.data().getAsJsonArray("conversions")) {
                if (!conversionElement.isJsonObject()) {
                    continue;
                }

                JsonObject conversion = conversionElement.getAsJsonObject();
                if (!conversion.has("from") || !conversion.has("to") || !sourceEffectId.equals(conversion.get("from").getAsString())) {
                    continue;
                }

                return conversion;
            }
        }
        return null;
    }

    private static void breakMatchingNeighborBlocks(ItemStack stack, Level level, BlockPos origin, BlockState originalState, LivingEntity entity, JsonObject data) {
        if (!(level instanceof ServerLevel serverLevel) || !data.has("action")) {
            return;
        }

        String mode = data.get("action").getAsString();
        if (!"same_block_neighbors".equals(mode) && !"same_or_softer_neighbors".equals(mode)) {
            return;
        }

        float originalHardness = originalState.getDestroySpeed(level, origin);
        BreakArea breakArea = getBreakArea(data);
        Set<BlockPos> broken = new HashSet<>();
        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-breakArea.xRadius(), -breakArea.yRadius(), -breakArea.zRadius()),
                origin.offset(breakArea.xRadius(), breakArea.yRadius(), breakArea.zRadius())
        )) {
            BlockPos candidatePos = candidate.immutable();
            if (candidatePos.equals(origin) || broken.contains(candidatePos)) {
                continue;
            }

            BlockState candidateState = level.getBlockState(candidatePos);
            if (!canBreakAreaCandidate(level, candidatePos, candidateState, originalState, originalHardness, mode)) {
                continue;
            }

            broken.add(candidatePos);
            Block.dropResources(candidateState, serverLevel, candidatePos, level.getBlockEntity(candidatePos), entity, stack);
            level.destroyBlock(candidatePos, false, entity);
        }
    }

    private static BreakArea getBreakArea(JsonObject data) {
        if (!data.has("area") || !data.get("area").isJsonObject()) {
            return new BreakArea(1, 1, 1);
        }

        JsonObject area = data.getAsJsonObject("area");
        int radius = getNonNegativeInt(area, "radius", 1);
        int xRadius = getNonNegativeInt(area, "x", radius);
        int yRadius = getNonNegativeInt(area, "y", radius);
        int zRadius = getNonNegativeInt(area, "z", radius);
        return new BreakArea(xRadius, yRadius, zRadius);
    }

    private static int getNonNegativeInt(JsonObject data, String field, int fallback) {
        if (!data.has(field)) {
            return fallback;
        }
        return Math.max(0, data.get(field).getAsInt());
    }

    private static boolean canBreakAreaCandidate(Level level, BlockPos candidatePos, BlockState candidateState, BlockState originalState, float originalHardness, String mode) {
        if (candidateState.isAir() || candidateState.getDestroySpeed(level, candidatePos) < 0.0F) {
            return false;
        }
        if (candidateState.is(originalState.getBlock())) {
            return true;
        }
        return "same_or_softer_neighbors".equals(mode) && originalHardness >= 0.0F && candidateState.getDestroySpeed(level, candidatePos) <= originalHardness;
    }

    private record BreakArea(int xRadius, int yRadius, int zRadius) {}

    private static DropTransformResult applyConfiguredDropEffects(ItemStack stack, BlockState state, ServerLevel level, List<ItemStack> originalDrops) {
        List<MimiclingFoods.EffectDefinition> matchingEffects = new ArrayList<>();
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            JsonObject data = effect.data();
            if (!effect.matches("change_drop", null) || !data.has("loot_table") || !formsMatch(stack, data) || !targetsMatch(state, data)) {
                continue;
            }

            matchingEffects.add(effect);
        }

        if (matchingEffects.isEmpty()) {
            return new DropTransformResult(originalDrops, false);
        }

        List<ItemStack> drops = copyDrops(originalDrops);
        boolean replacedOriginal = false;
        for (MimiclingFoods.EffectDefinition effect : matchingEffects) {
            JsonObject data = effect.data();
            List<ItemStack> rolledDrops = rollConfiguredLootTable(level, stack, state, data);
            consumeRollLootTableUsage(stack, effect);
            if (data.has("replace") && data.get("replace").getAsBoolean()) {
                drops = rolledDrops;
                replacedOriginal = true;
            } else {
                drops.addAll(rolledDrops);
            }
        }

        return new DropTransformResult(drops, replacedOriginal);
    }

    private static void consumeRollLootTableUsage(ItemStack tool, MimiclingFoods.EffectDefinition effect) {
        MimiclingFoods.consumeUsage(tool, effect.ownerId(), 1);
        MimiclingFoods.markUsageHandled(tool, effect.ownerId());
    }

    private static List<ItemStack> rollConfiguredLootTable(ServerLevel level, ItemStack tool, BlockState state, JsonObject data) {
        ResourceLocation lootTableId = new ResourceLocation(data.get("loot_table").getAsString());
        LootTable table = level.getServer().getLootData().getLootTable(lootTableId);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .create(LootContextParamSets.BLOCK);
        return table.getRandomItems(params);
    }

    private static boolean targetsMatch(BlockState state, JsonObject data) {
        if (data.has("target_blocks") && data.get("target_blocks").isJsonArray()) {
            return targetArrayMatches(state, data, "target_blocks");
        }
        if (data.has("targets") && data.get("targets").isJsonArray()) {
            return targetArrayMatches(state, data, "targets");
        }
        if (data.has("target")) {
            return targetBlockMatches(state, data.get("target").getAsString());
        }
        return true;
    }

    private static boolean targetArrayMatches(BlockState state, JsonObject data, String field) {
        for (JsonElement element : data.getAsJsonArray(field)) {
            if (targetBlockMatches(state, element.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean targetBlockMatches(BlockState state, String blockId) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(new ResourceLocation(blockId));
    }

    private static List<ItemStack> copyDrops(List<ItemStack> originalDrops) {
        List<ItemStack> drops = new ArrayList<>(originalDrops.size());
        for (ItemStack drop : originalDrops) {
            drops.add(drop.copy());
        }
        return drops;
    }

    private static List<ItemStack> collectDropsToInventoryIfConfigured(ItemStack tool, LootParams.Builder builder, List<ItemStack> drops) {
        MimiclingFoods.EffectDefinition collectEffect = getConfiguredEffect(tool, "change_drop", "collect_drops_to_inventory");
        if (drops.isEmpty() || collectEffect == null) {
            return drops;
        }

        Entity entity = builder.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Player player) || player.level().isClientSide) {
            return drops;
        }

        List<ItemStack> remainingDrops = new ArrayList<>();
        int insertedCount = 0;
        for (ItemStack drop : drops) {
            ItemStack remaining = drop.copy();
            int beforeCount = remaining.getCount();
            boolean fullyInserted = player.getInventory().add(remaining);
            insertedCount += beforeCount - remaining.getCount();
            if (!fullyInserted && !remaining.isEmpty()) {
                remainingDrops.add(remaining);
            }
        }
        if (insertedCount > 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.45F, 1.35F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            ItemStack usageStack = getHeldMimiclingForDropEffect(player, tool, "change_drop", "collect_drops_to_inventory");
            MimiclingFoods.EffectDefinition usageEffect = getConfiguredEffect(usageStack, "change_drop", "collect_drops_to_inventory");
            if (usageEffect == null) {
                usageStack = tool;
                usageEffect = collectEffect;
            }
            if (MimiclingFoods.consumeUsage(usageStack, usageEffect.ownerId(), 1)) {
                MimiclingFoods.markUsageHandled(usageStack, usageEffect.ownerId());
            }
        }
        return remainingDrops;
    }

    private static ItemStack getHeldMimiclingForDropEffect(Player player, ItemStack fallback, String trigger, String action) {
        ItemStack mainHand = player.getMainHandItem();
        if (isMimiclingStack(mainHand) && getConfiguredEffect(mainHand, trigger, action) != null && getForm(mainHand).equals(getForm(fallback))) {
            return mainHand;
        }

        ItemStack offhand = player.getOffhandItem();
        if (isMimiclingStack(offhand) && getConfiguredEffect(offhand, trigger, action) != null && getForm(offhand).equals(getForm(fallback))) {
            return offhand;
        }

        return fallback;
    }

    private record DropTransformResult(List<ItemStack> drops, boolean replacedOriginal) {}

    private static boolean hasMimiclingEffect(ItemStack stack, String type, String action) {
        return isMimiclingStack(stack) && MimiclingFoods.hasActiveEffect(stack, type, action);
    }

    private static boolean hasConfiguredEffect(ItemStack stack, String type, String action) {
        return getConfiguredEffect(stack, type, action) != null;
    }

    private static MimiclingFoods.EffectDefinition getConfiguredEffect(ItemStack stack, String type, String action) {
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches(type, action) && formsMatch(stack, effect.data())) {
                return effect;
            }
        }
        return null;
    }

    private static boolean isMimiclingStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof MimiclingFormItem;
    }

    private static String getForm(ItemStack stack) {
        return stack.getItem() instanceof MimiclingFormItem mimiclingFormItem ? mimiclingFormItem.getMimiclingForm() : "";
    }

    private static boolean formsMatch(ItemStack stack, JsonObject data) {
        if (!data.has("forms") || !data.get("forms").isJsonArray()) {
            return true;
        }

        String form = getForm(stack);
        for (JsonElement element : data.getAsJsonArray("forms")) {
            if (form.equals(element.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasActiveFood(ItemStack stack, String foodId) {
        for (MimiclingFoods.FoodDefinition food : MimiclingFoods.getActiveFoods(stack)) {
            if (foodId.equals(food.id())) {
                return true;
            }
        }
        return false;
    }

    private static List<ItemStack> getActiveMimiclingStacks(LivingEntity entity) {
        List<ItemStack> stacks = new ArrayList<>(2);
        if (isMimiclingStack(entity.getMainHandItem())) {
            stacks.add(entity.getMainHandItem());
        }
        if (isMimiclingStack(entity.getOffhandItem()) && entity.getOffhandItem() != entity.getMainHandItem()) {
            stacks.add(entity.getOffhandItem());
        }
        return stacks;
    }

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES)
                || state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES);
    }

    private static boolean isGoldOre(BlockState state) {
        return state.is(BlockTags.GOLD_ORES);
    }

    private static boolean applyBlockSmeltingTransform(ServerLevel level, BlockPos pos, ItemStack tool, SimpleContainer container, SmeltingRecipe recipe) {
        ItemStack result = recipe.assemble(container, level.registryAccess());
        if (result.isEmpty() || !(result.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block resultBlock = blockItem.getBlock();
        if (resultBlock == Blocks.AIR) {
            return false;
        }

        BlockState resultState = resultBlock.defaultBlockState();
        if (!wouldDropAfterTransform(level, pos, tool, resultState)) {
            return false;
        }

        return level.setBlockAndUpdate(pos, resultState);
    }

    private static boolean wouldDropAfterTransform(ServerLevel level, BlockPos pos, ItemStack tool, BlockState resultState) {
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withParameter(LootContextParams.BLOCK_STATE, resultState);
        return !resultState.getDrops(builder).isEmpty();
    }

    private static List<ItemStack> smeltBlockItemDrop(ServerLevel level, Vec3 origin, BlockState state, ItemStack tool, List<ItemStack> originalDrops) {
        if (originalDrops.isEmpty()) {
            return originalDrops;
        }
        if (state.is(Blocks.CLAY)) {
            return originalDrops;
        }

        SimpleContainer container = new SimpleContainer(new ItemStack(state.getBlock()));
        AbstractCookingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level).orElse(null);
        if (recipe == null) {
            return originalDrops;
        }

        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        if (result.isEmpty() || !(result.getItem() instanceof BlockItem)) {
            return originalDrops;
        }
        BlockState resultState = ((BlockItem)result.getItem()).getBlock().defaultBlockState();
        if (!wouldDropAfterTransform(level, BlockPos.containing(origin), tool, resultState)) {
            return originalDrops;
        }

        float experience = recipe.getExperience() * result.getCount();
        int wholeExperience = Mth.floor(experience);
        if (experience > wholeExperience && level.random.nextFloat() < experience - wholeExperience) {
            wholeExperience++;
        }
        if (wholeExperience > 0) {
            ExperienceOrb.award(level, origin, wholeExperience);
        }

        return List.of(result);
    }

    private static SmeltedDropResult smeltDrops(ServerLevel level, Vec3 origin, List<ItemStack> originalDrops) {
        List<ItemStack> transformed = new ArrayList<>();
        float experience = 0.0F;
        boolean changed = false;

        for (ItemStack drop : originalDrops) {
            SimpleContainer container = new SimpleContainer(drop.copyWithCount(1));
            AbstractCookingRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level).orElse(null);
            if (recipe == null) {
                transformed.add(drop.copy());
                continue;
            }

            ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
            result.setCount(result.getCount() * drop.getCount());
            transformed.add(result);
            experience += recipe.getExperience() * drop.getCount();
            changed = true;
        }

        int wholeExperience = Mth.floor(experience);
        if (experience > wholeExperience && level.random.nextFloat() < experience - wholeExperience) {
            wholeExperience++;
        }
        if (wholeExperience > 0) {
            ExperienceOrb.award(level, origin, wholeExperience);
        }

        return new SmeltedDropResult(transformed, changed);
    }

    private record SmeltedDropResult(List<ItemStack> drops, boolean changed) {}

}
