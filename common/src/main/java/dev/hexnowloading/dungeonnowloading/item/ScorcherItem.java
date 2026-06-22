package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.projectile.FlameProjectileEntity;
import dev.hexnowloading.dungeonnowloading.item.client.ItemAnimationState;
import dev.hexnowloading.dungeonnowloading.item.client.animation_duration.ScorcherAnimationDuration;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStartTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.network.packets.S2CStopTickingSoundPacket;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScorcherItem extends Item implements DNLAnimatedItem<ScorcherItem.ScorcherAnimationState> {

    private static final String HEAT_TAG = "ScorcherHeat";
    private static final String HEAT_TIME_STAMP = "ScorcherTimeStamp";
    private static final String BURN_TIME = "BurnTime";

    private static final Map<Item, FuelProperties> FUEL_TYPE = new HashMap<>();

    static {
        FUEL_TYPE.put(Items.COAL, new FuelProperties(0.5, 0, 0.4, 30));
        FUEL_TYPE.put(Items.CHARCOAL, new FuelProperties(0.5, 0, 0.4, 20));
    }

    public ScorcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime())) {
            return InteractionResultHolder.fail(stack);
        }

        if (!hasFuel(player)) {
            return InteractionResultHolder.fail(stack);
        }

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            if (!level.isClientSide) {
                ItemAnimationState.start(stack, ScorcherAnimationState.SCORCHER_STALLING.getName(), level.getGameTime(), (long) (ScorcherAnimationDuration.SCORCHER_STALLING * 20L), false, false);
                playScorcherSounds(stack, player, DNLSounds.SCORCHER_STALL.get(), DNLSounds.SOUL_SCORCHER_STALL.get());
            }

            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        if (!level.isClientSide) {
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    private boolean hasFuel(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        ItemStack offHandItem = player.getOffhandItem();

        if (FUEL_TYPE.containsKey(offHandItem.getItem())) {
            return true;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (FUEL_TYPE.containsKey(itemStack.getItem())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack $$0) {
        return super.getUseAnimation($$0);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int remainingUseDuration) {

        if (level.isClientSide) return;

        if (!(entity instanceof Player player)) return;

        int chargeTime = getUseDuration(itemStack) - remainingUseDuration;
        long gameTime = player.level().getGameTime();
        float overHeatedDuration = ScorcherAnimationDuration.SCORCHER_OVERHEAT / ScorcherAnimationDuration.SCORCHER_SHOOT;

        int activeAnimDuration = (int) (ScorcherAnimationDuration.SCORCHER_ACTIVATE * 20);
        int shootAnimDuration = (int) (ScorcherAnimationDuration.SCORCHER_SHOOT * 20 + activeAnimDuration);


        if (ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        if (chargeTime == 0) {
            ItemAnimationState.start(itemStack, ScorcherAnimationState.SCORCHER_ACTIVATED.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_ACTIVATE * 20L), false, true);
            playScorcherSounds(itemStack, player, DNLSounds.SCORCHER_START.get(), DNLSounds.SOUL_SCORCHER_START.get());
        }

        if (chargeTime == activeAnimDuration) {
            ItemAnimationState.start(itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_SHOOT * 20L), false, true);
            playScorcherSounds(itemStack, player, DNLSounds.SCORCHER_SHOOT.get(), DNLSounds.SOUL_SCORCHER_SHOOT.get());

        }

        if (chargeTime == shootAnimDuration) {
            ItemAnimationState.start(itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_OVERHEAT * 20L), false, true);
            playScorcherSounds(itemStack, player, DNLSounds.SCORCHER_OVERHEAT.get(), DNLSounds.SOUL_SCORCHER_OVERHEAT.get());
            setHeatLevel(itemStack, overHeatedDuration, gameTime);
            //((Player) player).getCooldowns().addCooldown(this, 160);
            player.releaseUsingItem();
            return;
        }

        float maxHeatDuration = ScorcherAnimationDuration.SCORCHER_SHOOT * 20;
        float heatIncreasePerTick = 1.0f / maxHeatDuration;

        if (ItemAnimationState.isAnimating(itemStack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {

            if (getBurnTime(itemStack) <= 0) {
                ItemStack fuelItemStack = getFuel(player);
                if (fuelItemStack.isEmpty()) {
                    player.releaseUsingItem();
                    return;
                }
                setFuelNBT(itemStack, fuelItemStack.getItem());
                FuelProperties properties = FUEL_TYPE.get(fuelItemStack.getItem());
                if (properties == null) {
                    player.releaseUsingItem();
                    return;
                }
                setBurnTime(itemStack, properties.getBurnTime());
                if (getBurnTime(itemStack) <= 0) {
                    player.releaseUsingItem();
                    return;
                }
                depleteFuel(fuelItemStack, player);
            }

            if (getBurnTime(itemStack) > 0) {
                setBurnTime(itemStack, getBurnTime(itemStack) - 1);
            }

            if (getBurnTime(itemStack) % 20 == 0) {
                itemStack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
                player.awardStat(Stats.ITEM_USED.get(this));
            }


            shootFlame(level, player, itemStack, getFuelType(itemStack));

            float heat = getHeatLevel(itemStack);
            heat = Math.min(1.0F, heat + heatIncreasePerTick);
            setHeatLevel(itemStack, heat, gameTime);

            if (heat >= 1.0F) {
                ItemAnimationState.start(itemStack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_OVERHEAT * 20L), false, true);
                playScorcherSounds(itemStack, player, DNLSounds.SCORCHER_OVERHEAT.get(), DNLSounds.SOUL_SCORCHER_OVERHEAT.get());
                stopScorcherSounds(itemStack, player, DNLSounds.SCORCHER_SHOOT.get(), DNLSounds.SOUL_SCORCHER_SHOOT.get());
                setHeatLevel(itemStack, overHeatedDuration, gameTime);
                player.releaseUsingItem();
                return;
            }
        }
    }

    private ItemStack getFuel(Player player) {
        ItemStack offHandItem = player.getOffhandItem();

        if (FUEL_TYPE.containsKey(offHandItem.getItem())) {
            return offHandItem;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (FUEL_TYPE.containsKey(itemStack.getItem())) {
                return itemStack;
            }
        }

        if (player.getAbilities().instabuild) {
            return new ItemStack(Items.COAL);
        }

        return ItemStack.EMPTY;
    }

    private void depleteFuel(ItemStack fuelStack, Player player) {
        if (!player.getAbilities().instabuild) {
            if (fuelStack.getCount() > 1) {
                fuelStack.shrink(1);
            } else {
                fuelStack.setCount(0);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        long gameTime = level.getGameTime();

        if (!level.isClientSide && stack.getItem() instanceof ScorcherItem && entity instanceof Player player) {
            long storedGameTime = getTimeStamp(stack);

            if (storedGameTime != gameTime) {
                float heat = getHeatLevel(stack);
                long timeElapsed = gameTime - storedGameTime;
                float heatDecayPerTick = 1.0f / (6.0f * 20);

                heat = Math.max(0.0F, heat - (timeElapsed * heatDecayPerTick));

               /* if (ItemAnimationState.isAnimatingOrHanging(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ItemAnimationState.stopAll(stack);
                }*/
                setHeatLevel(stack, heat, gameTime);
            }
            if (!isSelected && !(player.getMainHandItem().is(stack.getItem()) && (player.isUsingItem() || ItemAnimationState.isAnimating(player.getMainHandItem(), ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)))) {
                stopScorcherLingeringSounds(stack, player);
            }

            if (!isSelected && !ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), gameTime)) {
                if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_SHOOT.getName(), gameTime)) {
                    ItemAnimationState.start(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_STOP * 20L), false, true);
                } else if (!ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime)) {
                    ItemAnimationState.stopAll(stack);
                }
            }
        }
    }

    @Override
    public void playDroppedAnimation(Player player, ItemStack itemStack) {
        long gameTime = player.level().getGameTime();
        stopScorcherLingeringSounds(itemStack, player);
        ItemAnimationState.start(itemStack, ScorcherAnimationState.SCORCHER_STOP.getName(), gameTime, (long) (ScorcherAnimationDuration.SCORCHER_STOP * 20L), false, true);
    }

    private void shootFlame(Level level, LivingEntity player, ItemStack itemStack, Item fuelItem) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 rightVector = new Vec3(-viewVector.z, 0, viewVector.x).normalize();

        FuelProperties properties = FUEL_TYPE.get(fuelItem);

        if (properties == null) return;

        double rightOffset = 0.175;
        double verticalOffset = -0.25;

        Vec3 spawnPosition = eyePosition
                .add(viewVector.scale(0.6))
                .add(rightVector.scale(rightOffset))
                .add(0, verticalOffset, 0);

        Vec3 targetPosition = eyePosition.add(viewVector.scale(30))
                .add(0, -verticalOffset, 0);

        Vec3 correctedDirection = targetPosition.subtract(spawnPosition).normalize();

        // Apply random spread
        double spread = properties.getSpread(); // Adjust spread intensity
        RandomSource random = level.getRandom();
        double spreadX = (random.nextDouble() - 0.5) * spread;
        double spreadY = (random.nextDouble() - 0.5) * spread;
        double spreadZ = (random.nextDouble() - 0.5) * spread;

        Vec3 spreadVector = new Vec3(spreadX, spreadY, spreadZ);
        correctedDirection = correctedDirection.add(spreadVector).normalize();

        FlameProjectileEntity flame = new FlameProjectileEntity(player, level);
        flame.setOwner(player);
        flame.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);

        if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
            flame.setDamage(5.0F + properties.getBonusDamage());
            flame.setSoul(true);
        } else {
            flame.setDamage(4.0F + properties.getBonusDamage());
        }

        flame.setDeltaMovement(correctedDirection.scale(properties.getBulletSpeed()));

        level.addFreshEntity(flame);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {

        if (level.isClientSide) return;

        if (ItemAnimationState.isAnimating(stack, ScorcherAnimationState.SCORCHER_OVERHEAT.getName(), level.getGameTime())) {
            return;
        }

        ItemAnimationState.start(stack, ScorcherAnimationState.SCORCHER_STOP.getName(), level.getGameTime(), (long) (ScorcherAnimationDuration.SCORCHER_STOP * 20L), false, true);
        playScorcherSounds(stack, (Player) entity, DNLSounds.SCORCHER_STOP.get(), DNLSounds.SOUL_SCORCHER_STOP.get());
        stopScorcherSounds(stack, (Player) entity, DNLSounds.SCORCHER_SHOOT.get(), DNLSounds.SOUL_SCORCHER_SHOOT.get());
    }

    private void playScorcherSounds(ItemStack itemStack, Player player, SoundEvent scorcher, SoundEvent soulScorcher) {
        float radius = 32.0f;
        AABB detectionBox = player.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        ResourceLocation sound;
        if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
            sound = soulScorcher.getLocation();
        } else {
            sound = scorcher.getLocation();
        }
        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStartTickingSoundPacket(player.getId(), sound, SoundSource.PLAYERS), otherPlayer);
        }
    }

    private void stopScorcherLingeringSounds(ItemStack itemStack, Player player) {
        float radius = 32.0f;
        AABB detectionBox = player.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        List<ResourceLocation> soundsToStop = new ArrayList<>(List.of());
        if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
            soundsToStop.add(DNLSounds.SOUL_SCORCHER_START.get().getLocation());
            soundsToStop.add(DNLSounds.SOUL_SCORCHER_SHOOT.get().getLocation());
            soundsToStop.add(DNLSounds.SOUL_SCORCHER_OVERHEAT.get().getLocation());
        } else {
            soundsToStop.add(DNLSounds.SCORCHER_START.get().getLocation());
            soundsToStop.add(DNLSounds.SCORCHER_SHOOT.get().getLocation());
            soundsToStop.add(DNLSounds.SCORCHER_OVERHEAT.get().getLocation());
        }
        for (ServerPlayer otherPlayer : nearbyPlayers) {
            for (ResourceLocation sound : soundsToStop) {
                Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(player.getId(), sound, 20, true), otherPlayer);
            }
        }
    }

    private void stopScorcherSounds(ItemStack itemStack, Player player, SoundEvent scorcher, SoundEvent soulScorcher) {
        float radius = 32.0f;
        AABB detectionBox = player.getBoundingBox().inflate(radius);
        List<ServerPlayer> nearbyPlayers = player.level().getEntitiesOfClass(
                ServerPlayer.class,
                detectionBox
        );
        ResourceLocation sound;
        if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
            sound = soulScorcher.getLocation();
        } else {
            sound = scorcher.getLocation();
        }
        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Services.NETWORK.sendToPlayer(new S2CStopTickingSoundPacket(player.getId(), sound, 20, true), otherPlayer);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            //components.add(Component.translatable("item.dungeonnowloading.scorcher.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            if (itemStack.is(DNLItems.SCORCHER.get())) {
                components.add(Component.translatable("item.dungeonnowloading.scorcher.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
                components.add(Component.translatable("item.dungeonnowloading.scorcher.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            } else if (itemStack.is(DNLItems.SOUL_SCORCHER.get())) {
                components.add(Component.translatable("item.dungeonnowloading.soul_scorcher.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
                components.add(Component.translatable("item.dungeonnowloading.soul_scorcher.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            }
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.scorcher_common.tooltip.cost").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scorcher_common.tooltip.cost.coal").withStyle(ChatFormatting.DARK_GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scorcher_common.tooltip.cost.charcoal").withStyle(ChatFormatting.DARK_GRAY));


        }
    }

    public static void setHeatLevel(ItemStack stack, float heatLevel, long timeStamp) {
        StackNbt.update(stack, t -> t.putFloat(HEAT_TAG, heatLevel));
        StackNbt.update(stack, t -> t.putLong(HEAT_TIME_STAMP, timeStamp));
    }

    private static void ensureUUID(ItemStack stack) {
        if (!StackNbt.hasTag(stack)) {
            stack.setTag(new CompoundTag()); // ✅ Create tag if missing
        }
        if (!StackNbt.getTag(stack).contains("ScorcherUUID")) {
            StackNbt.update(stack, t -> t.putUUID("ScorcherUUID", UUID.randomUUID())); // ✅ Assign unique UUID
        }
    }

    private void setFuelNBT(ItemStack weaponItemStack, Item fuelItem) {
        CompoundTag tag = StackNbt.getOrCreateTag(weaponItemStack);
        ResourceLocation fuelId = BuiltInRegistries.ITEM.getKey(fuelItem);

        if (fuelId != null) {
            tag.putString("FuelType", fuelId.toString());
        }
    }

    public static Item getFuelType(ItemStack stack) {
        CompoundTag tag = StackNbt.getTag(stack);
        if (tag != null && tag.contains("FuelType")) {
            ResourceLocation fuelId = ResourceLocation.parse(tag.getString("FuelType"));
            return BuiltInRegistries.ITEM.get(fuelId);
        }
        return Items.AIR;
    }

    private void setBurnTime(ItemStack itemStack, int tick) {
        StackNbt.update(itemStack, t -> t.putInt(BURN_TIME, tick));
    }

    private int getBurnTime(ItemStack itemStack) {
        CompoundTag tag = StackNbt.getTag(itemStack);
        if (tag != null && tag.contains(BURN_TIME)) {
            return tag.getInt(BURN_TIME);
        }
        return 0;
    }

    public static float getHeatLevel(ItemStack stack) {
        return StackNbt.hasTag(stack) ? StackNbt.getTag(stack).getFloat(HEAT_TAG) : 0.0F;
    }

    public static long getTimeStamp(ItemStack stack) {
        return StackNbt.hasTag(stack) ? StackNbt.getTag(stack).getLong(HEAT_TIME_STAMP) : 0L;
    }

    @Override
    public Class<ScorcherAnimationState> getAnimationEnum() {
        return ScorcherAnimationState.class;
    }

    public enum ScorcherAnimationState implements DNLAnimationState {
        SCORCHER_ACTIVATED("scorcher_activated"),
        SCORCHER_SHOOT("scorcher_shoot"),
        SCORCHER_STOP("scorcher_stop"),
        SCORCHER_STALLING("scorcher_stalling"),
        SCORCHER_OVERHEAT("scorcher_overheat"),
        SCORCHER_BASE("scorcher_base");

        private final String name;

        ScorcherAnimationState(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class FuelProperties {
        private final double spread;
        private final float bonusDamage;
        private final double bulletSpeed;
        private final int burnTime;

        public FuelProperties(double spread, float bonusDamage, double bulletSpeed, int burnTime) {
            this.spread = spread;
            this.bonusDamage = bonusDamage;
            this.bulletSpeed = bulletSpeed;
            this.burnTime = burnTime;
        }

        public double getSpread() { return spread; }
        public float getBonusDamage() { return bonusDamage; }
        public double getBulletSpeed() { return bulletSpeed; }
        public int getBurnTime() { return burnTime; }
    }
}
