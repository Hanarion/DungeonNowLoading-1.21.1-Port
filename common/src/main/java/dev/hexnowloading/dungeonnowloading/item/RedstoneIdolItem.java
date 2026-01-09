package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.boss.FairkeeperSerpentCallerEntity;
import dev.hexnowloading.dungeonnowloading.entity.misc.SeepingSoulEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedstoneIdolItem extends BlockItem {

    public RedstoneIdolItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        AABB aabb = (new AABB(player.blockPosition())).inflate(32);

        // 1) Try recall first: find nearby souls that belong to Chaos Spawner
        ResourceLocation fairkeepersId = new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeepers");

        List<SeepingSoulEntity> souls = level.getEntitiesOfClass(SeepingSoulEntity.class, aabb);
        for (SeepingSoulEntity soul : souls) {
            if (!fairkeepersId.equals(soul.getBossId())) continue;

            if (soul.tryStartChanneling(player, stack)) {
                level.playSound(null, player.blockPosition(), DNLSounds.FAIRKEEPER_SERPENT_CALLER_ACTIVATED.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);

                player.getCooldowns().addCooldown(this, 7);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.consume(stack);
            }
        }

        List<FairkeeperSerpentCallerEntity> targets = level.getEntitiesOfClass(FairkeeperSerpentCallerEntity.class, aabb);
        List<FairkeeperSerpentCallerEntity> sleepingTargets = targets.stream().filter(entity -> !entity.isActivated()).toList();
        if (!sleepingTargets.isEmpty()) {
            player.startUsingItem(hand);
            player.getCooldowns().addCooldown(this, 7);
            player.awardStat(Stats.ITEM_USED.get(this));
            for (FairkeeperSerpentCallerEntity serpentCallerEntity : targets) {
                if (!level.isClientSide) {
                    serpentCallerEntity.startBossFight();
                }
            }
            /*if (player instanceof ServerPlayer) {
                itemStack.hurtAndBreak(1, player, (player1 -> player1.broadcastBreakEvent(hand)));
            }*/
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.redstone_idol.tooltip").withStyle(ChatFormatting.GRAY));
        }
    }
}
