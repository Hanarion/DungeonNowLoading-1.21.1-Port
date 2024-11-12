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
    private static final double CREEP_TRIGGER_RADIUS = 16.0d;

    public CopperDetonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos clickedPos = ctx.getClickedPos();
        Direction clickedFace = ctx.getClickedFace();

        boolean playerHasCopperBlock = this.checkPlayerHasCopperBlock(player);
        List<CopperCreepEntity> nearbyCreeps = this.getNearbyCreeps(player, CREEP_TRIGGER_RADIUS);

        if (nearbyCreeps.isEmpty()) {
            this.consumeCopperBlock(player);
            this.summonCopperCreep(level, clickedPos, clickedFace, player.getUUID());
            player.getCooldowns().addCooldown(this, 5);
        } else {
            if ((!player.isCreative() && !playerHasCopperBlock)) {
                // player.displayClientMessage(Component.translatable("item.dungeonnowloading.no_copper_block"), true);
                return InteractionResult.FAIL;
            }

            nearbyCreeps.forEach(CopperCreepEntity::ignite);
            player.getCooldowns().addCooldown(this, 200);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        List<CopperCreepEntity> nearbyCreeps = this.getNearbyCreeps(player, CREEP_TRIGGER_RADIUS);
        if (nearbyCreeps.isEmpty()) {
            return InteractionResultHolder.fail(itemStack);
        }

        nearbyCreeps.forEach(CopperCreepEntity::ignite);
        player.getCooldowns().addCooldown(this, 200);

        return InteractionResultHolder.success(itemStack);
    }

    private List<CopperCreepEntity> getNearbyCreeps(Player player, double creepTriggerRadius) {
        return player.level().getEntitiesOfClass(
                CopperCreepEntity.class,
                player.getBoundingBox().inflate(creepTriggerRadius)
        )
                .stream()
                .filter(entity -> !entity.isDefused())
                .filter(entity -> !entity.isDeadOrDying())
                .filter(entity -> player.distanceToSqr(entity) <= (creepTriggerRadius * creepTriggerRadius))
                .toList();
    }

    private void summonCopperCreep(Level level, BlockPos pos, Direction clickedFace, UUID summonerUUID) {
        pos = pos.relative(clickedFace);

        CopperCreepEntity entity = DNLEntityTypes.COPPER_CREEP.get().create(level);
        if (entity != null) {
            entity.moveTo(pos, 0.0f, 0.0f);
            entity.setSummonerUUID(summonerUUID);
            level.addFreshEntity(entity);
        }
    }

    private boolean checkPlayerHasCopperBlock(Player player) {
        boolean hasCopperBlock = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.COPPER_BLOCK && stack.getCount() > 0) {
                hasCopperBlock = true;
                break;
            }
        }
        return hasCopperBlock;
    }

    private void consumeCopperBlock(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.COPPER_BLOCK && stack.getCount() > 0) {
                stack.shrink(1); // Removes 1 copper block
                break;
            }
        }
    }

//        return InteractionResultHolder.fail(itemStack);

//        ItemStack itemStack = player.getItemInHand(hand);
//        AABB aabb = (new AABB(player.blockPosition())).inflate(16);
//        List<FairkeeperEntity> targets = level.getEntitiesOfClass(FairkeeperEntity.class, aabb);
//        List<FairkeeperEntity> sleepingTargets = targets.stream().filter(FairkeeperEntity::isSlumbering).toList();
//        if (!sleepingTargets.isEmpty()) {
//            player.startUsingItem(hand);
//            level.playSound(player, player, DNLSounds.CHAOS_SPAWNER_LAUGHTER.get(), SoundSource.RECORDS, 1.0F, 2.0F);
//            player.getCooldowns().addCooldown(this, 7);
//            player.awardStat(Stats.ITEM_USED.get(this));
//            for (FairkeeperEntity FairkeeperEntity : targets) {
//                FairkeeperEntity.startBossFight();
//            }
//            if (player instanceof ServerPlayer) {
//                itemStack.hurtAndBreak(1, player, (player1 -> player1.broadcastBreakEvent(hand)));
//            }
//            return InteractionResultHolder.consume(itemStack);
//        }
//        return InteractionResultHolder.fail(itemStack);


//    @Override
//    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
//        super.appendHoverText(itemStack, level, components, tooltipFlag);
//        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
//            components.add(Component.translatable("item.dungeonnowloading.redstone_catalyst.tooltip").withStyle(ChatFormatting.GRAY));
//        }
//    }
}
