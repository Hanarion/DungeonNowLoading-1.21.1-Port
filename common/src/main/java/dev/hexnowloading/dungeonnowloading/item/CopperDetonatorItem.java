package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.UUID;

public class CopperDetonatorItem extends Item {
    private static final double TRIGGER_RADIUS = 16.0;
    private static final int SUMMON_COOLDOWN = 5;
    private static final int IGNITE_COOLDOWN = 200;

    public CopperDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos targetPos = context.getClickedPos();
        Direction targetFace = context.getClickedFace();

        if (player == null) return InteractionResult.PASS;

        List<CopperCreepEntity> creepsInRange = findNearbyCreeps(player, TRIGGER_RADIUS);

        if (creepsInRange.isEmpty()) {
            if (consumeCopperBlockIfAvailable(player)) {
                summonCreep(level, targetPos.relative(targetFace), player.getUUID());
                player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        igniteCreeps(creepsInRange);
        player.getCooldowns().addCooldown(this, IGNITE_COOLDOWN);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        List<CopperCreepEntity> creepsInRange = findNearbyCreeps(player, TRIGGER_RADIUS);

        if (creepsInRange.isEmpty()) {
            if (consumeCopperBlockIfAvailable(player)) {
                launchCreep(level, player);
                player.getCooldowns().addCooldown(this, SUMMON_COOLDOWN);
                itemStack.hurtAndBreak(1, player, player1 -> player1.broadcastBreakEvent(hand));
                return InteractionResultHolder.success(itemStack);
            }
            return InteractionResultHolder.fail(itemStack);
        }

        igniteCreeps(creepsInRange);
        player.getCooldowns().addCooldown(this, IGNITE_COOLDOWN);
        return InteractionResultHolder.success(itemStack);
    }

    private void launchCreep(Level level, Player player) {
        CopperCreepEntity creep = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (creep == null) return;

        double offset = 1.0;
        double launchX = player.getX() - Math.sin(Math.toRadians(player.getYRot())) * offset;
        double launchY = player.getY() + player.getEyeHeight() * 0.6;
        double launchZ = player.getZ() + Math.cos(Math.toRadians(player.getYRot())) * offset;
        creep.moveTo(launchX, launchY, launchZ, player.getYRot(), player.getXRot());

        double velocity = 1.0;
        double motionX = -Math.sin(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        double motionY = -Math.sin(Math.toRadians(player.getXRot())) * velocity;
        double motionZ = Math.cos(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot())) * velocity;
        creep.setDeltaMovement(motionX, motionY, motionZ);

        level.addFreshEntity(creep);
    }

    private List<CopperCreepEntity> findNearbyCreeps(Player player, double radius) {
        return player.level()
                .getEntitiesOfClass(CopperCreepEntity.class, player.getBoundingBox().inflate(radius))
                .stream()
                .filter(creep -> !creep.isDefused() && !creep.isDeadOrDying() && player.distanceToSqr(creep) <= radius * radius)
                .toList();
    }

    private void summonCreep(Level level, BlockPos position, UUID summonerUUID) {
        CopperCreepEntity creep = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (creep == null) return;

        creep.moveTo(position, 0.0f, 0.0f);
        creep.setSummonerUUID(summonerUUID);
        level.addFreshEntity(creep);
    }

    private boolean consumeCopperBlockIfAvailable(Player player) {
        if (player.isCreative()) return true;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.COPPER_BLOCK && stack.getCount() > 0) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void igniteCreeps(List<CopperCreepEntity> creeps) {
        creeps.forEach(CopperCreepEntity::ignite);
    }
}