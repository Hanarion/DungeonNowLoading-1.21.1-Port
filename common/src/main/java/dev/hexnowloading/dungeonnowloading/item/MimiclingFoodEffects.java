package dev.hexnowloading.dungeonnowloading.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MimiclingFoodEffects {
    private static final String MEMORY_TAG = "MimiclingFoodEffectMemory";
    private static final String TELEPORT_DESTINATION_TAG = "teleport_destination";
    private static final int HELD_EFFECT_TICKS = 40;

    private MimiclingFoodEffects() {}

    public static void tickHeld(ItemStack stack, Level level, Entity entity) {
        if (level.isClientSide || !(entity instanceof Player player) || !isHeld(player, stack)) {
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

        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("memorize_position", null) && formsMatch(stack, effect.data())) {
                memorizePosition(stack, level, effect.data(), pos);
            } else if (effect.matches("on_break", "auto_switch_unsuited_tool")) {
                MimiclingItem.tryTransformHeldOrEquippedToForm(stack, entity, MimiclingItem.getWorstFormFor(stack, state), MimiclingItem.getBlockUseDurabilityCost(stack), true);
            } else if (effect.matches("change_break_area", null) && formsMatch(stack, effect.data())) {
                breakMatchingNeighborBlocks(stack, level, pos, state, entity, effect.data());
            }
        }
    }

    public static void onAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level().isClientSide) {
            return;
        }

        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("change_mob_position", "teleport_to_memorized_position")) {
                teleportTarget(stack, target, effect.data());
            } else if (effect.matches("on_attack", "auto_switch_unsuited_tool")) {
                MimiclingItem.tryTransformHeldOrEquippedToForm(stack, attacker, MimiclingItem.getWorstCombatForm(stack), MimiclingItem.getAttackUseDurabilityCost(stack), true);
            } else if (effect.matches("apply_non_potion_effect", "jump_up")) {
                jumpTarget(target, effect.data());
            } else if (effect.matches("apply_potion_effect", null)) {
                applyPotionEffects(target, effect.data());
            } else if (effect.matches("reference_potion_effect", "invert_potion_effect")) {
                invertPotionEffects(target);
            }
        }
    }

    public static boolean hasUnderwaterMiningSpeedEffect(Player player) {
        return hasMimiclingEffect(player.getMainHandItem(), "while_in_hand", "remove_underwater_mining_penalty");
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

        ItemStack input = new ItemStack(state.getBlock());
        if (input.isEmpty()) {
            return false;
        }

        SimpleContainer container = new SimpleContainer(input);
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> applyBlockSmeltingTransform(level, pos, tool, container, recipe))
                .orElse(false);
    }

    public static List<ItemStack> transformBlockDrops(BlockState state, LootParams.Builder builder, List<ItemStack> originalDrops) {
        ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
        if (!isMimiclingStack(tool)) {
            return originalDrops;
        }

        DropTransformResult configuredDrops = applyConfiguredDropEffects(tool, state, builder.getLevel(), originalDrops);
        List<ItemStack> drops = configuredDrops.drops();
        if (configuredDrops.replacedOriginal()) {
            return drops;
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
                List<ItemStack> blockDrops = smeltBlockItemDrop(level, origin, state, drops);
                if (blockDrops != drops) {
                    return blockDrops;
                }
                return smeltDrops(level, origin, drops);
            }
        }

        return drops;
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

        ItemStack mimicling = getActiveMimiclingWithEffect(livingAttacker, "reference_potion_effect", "death_effect_cloud");
        if (mimicling.isEmpty() || target.getActiveEffects().isEmpty()) {
            return;
        }

        AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        cloud.setOwner(livingAttacker);
        cloud.setRadius(2.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(200);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        for (MobEffectInstance effect : target.getActiveEffects()) {
            cloud.addEffect(new MobEffectInstance(effect));
        }
        level.addFreshEntity(cloud);
    }

    public static void applyTemporaryEnchantmentModifiers(ItemStack stack, ListTag enchantments) {
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches("while_in_hand", "modify_enchantment") && formsMatch(stack, effect.data())) {
                applyTemporaryEnchantmentModifier(enchantments, effect.data());
            }
        }
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

        if (level instanceof ServerLevel serverLevel && data.has("particle")) {
            ParticleOptions particle = (ParticleOptions) BuiltInRegistries.PARTICLE_TYPE.get(new ResourceLocation(data.get("particle").getAsString()));
            serverLevel.sendParticles(particle, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 16, 0.35D, 0.35D, 0.35D, 0.02D);
        }
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

    private static void applyPotionEffects(LivingEntity target, JsonObject data) {
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

    private static void invertPotionEffects(LivingEntity target) {
        MobEffectInstance strength = target.getEffect(MobEffects.DAMAGE_BOOST);
        if (strength != null) {
            target.removeEffect(MobEffects.DAMAGE_BOOST);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, strength.getDuration(), strength.getAmplifier()));
            return;
        }

        MobEffectInstance speed = target.getEffect(MobEffects.MOVEMENT_SPEED);
        MobEffectInstance jump = target.getEffect(MobEffects.JUMP);
        MobEffectInstance source = speed != null ? speed : jump;
        if (source != null) {
            target.removeEffect(speed != null ? MobEffects.MOVEMENT_SPEED : MobEffects.JUMP);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, source.getDuration(), source.getAmplifier()));
            return;
        }

        MobEffectInstance nightVision = target.getEffect(MobEffects.NIGHT_VISION);
        if (nightVision != null) {
            target.removeEffect(MobEffects.NIGHT_VISION);
            target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, nightVision.getDuration(), nightVision.getAmplifier()));
            return;
        }

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0));
    }

    private static void breakMatchingNeighborBlocks(ItemStack stack, Level level, BlockPos origin, BlockState originalState, LivingEntity entity, JsonObject data) {
        if (!(level instanceof ServerLevel serverLevel) || !data.has("mode")) {
            return;
        }

        String mode = data.get("mode").getAsString();
        if (!"same_block_neighbors".equals(mode) && !"same_or_softer_neighbors".equals(mode)) {
            return;
        }

        float originalHardness = originalState.getDestroySpeed(level, origin);
        Set<BlockPos> broken = new HashSet<>();
        for (BlockPos candidate : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
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

    private static boolean canBreakAreaCandidate(Level level, BlockPos candidatePos, BlockState candidateState, BlockState originalState, float originalHardness, String mode) {
        if (candidateState.isAir() || candidateState.getDestroySpeed(level, candidatePos) < 0.0F) {
            return false;
        }
        if (candidateState.is(originalState.getBlock())) {
            return true;
        }
        return "same_or_softer_neighbors".equals(mode) && originalHardness >= 0.0F && candidateState.getDestroySpeed(level, candidatePos) <= originalHardness;
    }

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
            if (data.has("replace") && data.get("replace").getAsBoolean()) {
                drops = rolledDrops;
                replacedOriginal = true;
            } else {
                drops.addAll(rolledDrops);
            }
        }

        return new DropTransformResult(drops, replacedOriginal);
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

    private record DropTransformResult(List<ItemStack> drops, boolean replacedOriginal) {}

    private static boolean hasMimiclingEffect(ItemStack stack, String type, String action) {
        return isMimiclingStack(stack) && MimiclingFoods.hasActiveEffect(stack, type, action);
    }

    private static boolean hasConfiguredEffect(ItemStack stack, String type, String action) {
        for (MimiclingFoods.EffectDefinition effect : MimiclingFoods.getActiveEffects(stack)) {
            if (effect.matches(type, action) && formsMatch(stack, effect.data())) {
                return true;
            }
        }
        return false;
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

    private static ItemStack getActiveMimiclingWithEffect(LivingEntity entity, String type, String action) {
        if (hasMimiclingEffect(entity.getMainHandItem(), type, action)) {
            return entity.getMainHandItem();
        }
        if (hasMimiclingEffect(entity.getOffhandItem(), type, action)) {
            return entity.getOffhandItem();
        }
        return ItemStack.EMPTY;
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

    private static List<ItemStack> smeltBlockItemDrop(ServerLevel level, Vec3 origin, BlockState state, List<ItemStack> originalDrops) {
        if (originalDrops.isEmpty()) {
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

    private static List<ItemStack> smeltDrops(ServerLevel level, Vec3 origin, List<ItemStack> originalDrops) {
        List<ItemStack> transformed = new ArrayList<>();
        float experience = 0.0F;

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
        }

        int wholeExperience = Mth.floor(experience);
        if (experience > wholeExperience && level.random.nextFloat() < experience - wholeExperience) {
            wholeExperience++;
        }
        if (wholeExperience > 0) {
            ExperienceOrb.award(level, origin, wholeExperience);
        }

        return transformed;
    }

}
