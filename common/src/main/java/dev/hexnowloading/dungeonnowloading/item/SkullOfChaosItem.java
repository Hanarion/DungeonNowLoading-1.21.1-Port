package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.boss.ChaosSpawnerEntity;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class SkullOfChaosItem extends Item implements BossSummoningItem, Vanishable {

    public SkullOfChaosItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        AABB aabb = new AABB(player.blockPosition()).inflate(16);

        // 1) Try recall first: find nearby souls that belong to Chaos Spawner
        ResourceLocation chaosSpawnerId = new ResourceLocation(DungeonNowLoading.MOD_ID, "chaos_spawner");

        List<SeepingSoulEntity> souls = level.getEntitiesOfClass(SeepingSoulEntity.class, aabb);
        for (SeepingSoulEntity soul : souls) {
            if (!chaosSpawnerId.equals(soul.getBossId())) continue;

            if (soul.tryStartChanneling(player, stack)) {
                level.playSound(null, player.blockPosition(), DNLSounds.CHAOS_SPAWNER_LAUGHTER.get(),
                        SoundSource.PLAYERS, 1.0F, 2.0F);

                player.getCooldowns().addCooldown(this, 7);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.consume(stack);
            }
        }

        // 2) Fallback: your existing "wake sleeping Chaos Spawner" behavior
        List<ChaosSpawnerEntity> targets = level.getEntitiesOfClass(ChaosSpawnerEntity.class, aabb);
        List<ChaosSpawnerEntity> sleepingTargets = targets.stream()
                .filter(e -> e.getState() == ChaosSpawnerEntity.State.SLEEPING)
                .collect(Collectors.toList());

        if (!sleepingTargets.isEmpty()) {
            player.startUsingItem(hand);
            level.playSound(null, player.blockPosition(), DNLSounds.CHAOS_SPAWNER_LAUGHTER.get(),
                    SoundSource.PLAYERS, 1.0F, 2.0F);

            player.getCooldowns().addCooldown(this, 7);
            player.awardStat(Stats.ITEM_USED.get(this));

            for (ChaosSpawnerEntity e : sleepingTargets) {
                e.startBossFight(stack);
            }

            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.fail(stack);
    }


    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.skull_of_chaos.tooltip").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return itemStack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}
