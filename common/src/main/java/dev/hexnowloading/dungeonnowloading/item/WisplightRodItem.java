package dev.hexnowloading.dungeonnowloading.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.hexnowloading.dungeonnowloading.block.WispBlock;
import dev.hexnowloading.dungeonnowloading.block.entity.WispBlockEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.LargeWispProjectileEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.WispProjectileEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.world.WispBlockTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class WisplightRodItem extends Item {
    public static final float SMALL_WISP_DAMAGE = 2.0F;
    public static final float NORMAL_PROJECTILE_DAMAGE = 4.0F;
    private static final float LARGE_PROJECTILE_DAMAGE = 8.0F;
    private static final String FUEL_CONSUMED_TAG = "FuelConsumed";
    private static final String LARGE_FUEL_CONSUMED_TAG = "LargeFuelConsumed";
    private static final double ATTACK_DAMAGE_MODIFIER = 0.0D;
    private static final double ATTACK_SPEED_MODIFIER = -3.0D;
    private static final int BURN_SECONDS = 8;
    private static final int CHARGE_TIME_TICKS = 10;
    private static final int LARGE_CHARGE_TIME_TICKS = 30;
    private static final int WISP_BLOCK_PLACE_COOLDOWN_TICKS = 10;
    private static final double TARGET_RANGE = 32.0D;
    private static final float PROJECTILE_SPEED = 1.1F;

    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public WisplightRodItem(Properties properties) {
        super(properties);
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", ATTACK_DAMAGE_MODIFIER, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", ATTACK_SPEED_MODIFIER, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(BURN_SECONDS);
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        if (!this.hasFuel(player)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos placePos = placeContext.getClickedPos();
        BlockState replacedState = level.getBlockState(placePos);
        if (!replacedState.canBeReplaced(placeContext) || !player.mayUseItemAt(placePos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }

        Block block = DNLBlocks.WISP_BLOCK.get();
        BlockState placedState = block.getStateForPlacement(placeContext);
        if (placedState == null) {
            placedState = block.defaultBlockState();
        }

        if (!placedState.canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            if (!level.setBlock(placePos, placedState, Block.UPDATE_ALL)) {
                return InteractionResult.FAIL;
            }

            if (level instanceof ServerLevel serverLevel) {
                if (serverLevel.getBlockEntity(placePos) instanceof WispBlockEntity wispBlockEntity) {
                    long placedGameTime = serverLevel.getGameTime();
                    wispBlockEntity.setOwner(player.getUUID(), placedGameTime);
                    WispBlockTracker.get(serverLevel).registerPlaced(serverLevel, placePos, player.getUUID(), placedGameTime);
                }
                WispBlock.playPlacementEffects(serverLevel, placePos);
            }
            level.gameEvent(player, GameEvent.BLOCK_PLACE, placePos);
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(context.getHand()));
            player.getCooldowns().addCooldown(this, WISP_BLOCK_PLACE_COOLDOWN_TICKS);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.hasFuel(player)) {
            return InteractionResultHolder.fail(stack);
        }

        this.setFuelConsumed(stack, false);
        this.setLargeFuelConsumed(stack, false);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) {
            return;
        }

        int chargeTime = this.getUseDuration(stack) - remainingUseDuration;
        if (!this.hasConsumedFuel(stack) && chargeTime >= CHARGE_TIME_TICKS) {
            if (!this.hasFuel(player)) {
                player.releaseUsingItem();
                return;
            }

            this.consumeFuel(player);
            this.setFuelConsumed(stack, true);
        }

        if (!this.hasLargeFuelConsumed(stack) && chargeTime >= LARGE_CHARGE_TIME_TICKS) {
            if (!this.hasFuel(player)) {
                player.releaseUsingItem();
                return;
            }

            this.consumeFuel(player);
            this.setLargeFuelConsumed(stack, true);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(livingEntity instanceof Player player)) {
            this.setFuelConsumed(stack, false);
            this.setLargeFuelConsumed(stack, false);
            return;
        }

        int chargeTime = this.getUseDuration(stack) - timeCharged;
        boolean fullyCharged = chargeTime >= CHARGE_TIME_TICKS;
        boolean fuelConsumed = this.hasConsumedFuel(stack);
        boolean largeFuelConsumed = this.hasLargeFuelConsumed(stack);
        this.setFuelConsumed(stack, false);
        this.setLargeFuelConsumed(stack, false);

        if (!fullyCharged || !fuelConsumed || level.isClientSide) {
            return;
        }

        if (chargeTime >= LARGE_CHARGE_TIME_TICKS && largeFuelConsumed) {
            this.shootLargeWisp(level, player, stack, player.getUsedItemHand());
        } else {
            this.shootWisp(level, player, stack, player.getUsedItemHand());
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.75F, 1.1F + level.random.nextFloat() * 0.2F);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private void shootWisp(Level level, Player player, ItemStack stack, InteractionHand hand) {
        WispProjectileEntity projectile = new WispProjectileEntity(level, player);
        projectile.setAttackDamage(NORMAL_PROJECTILE_DAMAGE);
        this.configureShotProjectile(level, player, projectile);
        level.addFreshEntity(projectile);
        stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
    }

    private void shootLargeWisp(Level level, Player player, ItemStack stack, InteractionHand hand) {
        LargeWispProjectileEntity projectile = new LargeWispProjectileEntity(level, player);
        projectile.setAttackDamage(LARGE_PROJECTILE_DAMAGE);
        this.configureShotProjectile(level, player, projectile);
        level.addFreshEntity(projectile);
        stack.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
    }

    private void configureShotProjectile(Level level, Player player, WispProjectileEntity projectile) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        LivingEntity target = this.findLookTarget(level, player, eyePosition, viewVector);
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.3D, player.getZ());
        projectile.setDiscardWhenTargetMissing(false);
        projectile.setHomingTarget(target);
        projectile.shoot(viewVector.x, viewVector.y, viewVector.z, PROJECTILE_SPEED, 0.0F);
        projectile.setXRot(player.getXRot());
        projectile.setYRot(player.getYRot());
        projectile.xRotO = player.xRotO;
        projectile.yRotO = player.yRotO;
    }

    private LivingEntity findLookTarget(Level level, Player player, Vec3 eyePosition, Vec3 viewVector) {
        Vec3 maxReachPosition = eyePosition.add(viewVector.scale(TARGET_RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(eyePosition, maxReachPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double targetRangeSqr = TARGET_RANGE * TARGET_RANGE;
        if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            targetRangeSqr = blockHit.getLocation().distanceToSqr(eyePosition);
        }

        Vec3 entityReachPosition = eyePosition.add(viewVector.scale(Math.sqrt(targetRangeSqr)));
        AABB searchBox = player.getBoundingBox().expandTowards(viewVector.scale(TARGET_RANGE)).inflate(1.0D);
        Predicate<Entity> predicate = entity -> entity instanceof LivingEntity
                && entity != player
                && entity.isAlive()
                && entity.isPickable()
                && !entity.isSpectator();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, eyePosition, entityReachPosition, searchBox, predicate);
        if (entityHit != null && entityHit.getLocation().distanceToSqr(eyePosition) <= targetRangeSqr && entityHit.getEntity() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    private boolean hasFuel(Player player) {
        return player.getAbilities().instabuild || !this.getFuel(player).isEmpty();
    }

    private void consumeFuel(Player player) {
        if (player.getAbilities().instabuild) {
            return;
        }

        ItemStack fuel = this.getFuel(player);
        if (!fuel.isEmpty()) {
            fuel.shrink(1);
        }
    }

    private ItemStack getFuel(Player player) {
        ItemStack offhandItem = player.getOffhandItem();
        if (isFuel(offhandItem)) {
            return offhandItem;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack itemStack = player.getInventory().getItem(i);
            if (isFuel(itemStack)) {
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    private boolean hasConsumedFuel(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(FUEL_CONSUMED_TAG);
    }

    private void setFuelConsumed(ItemStack stack, boolean consumed) {
        stack.getOrCreateTag().putBoolean(FUEL_CONSUMED_TAG, consumed);
    }

    private boolean hasLargeFuelConsumed(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(LARGE_FUEL_CONSUMED_TAG);
    }

    private void setLargeFuelConsumed(ItemStack stack, boolean consumed) {
        stack.getOrCreateTag().putBoolean(LARGE_FUEL_CONSUMED_TAG, consumed);
    }
}
