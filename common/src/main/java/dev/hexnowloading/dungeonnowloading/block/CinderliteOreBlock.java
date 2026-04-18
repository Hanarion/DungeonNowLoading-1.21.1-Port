package dev.hexnowloading.dungeonnowloading.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CinderliteOreBlock extends DropExperienceBlock {

    public CinderliteOreBlock(Properties properties, IntProvider exp) {
        super(properties, exp);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        // keep normal drops/xp behavior
        super.playerDestroy(level, player, pos, state, blockEntity, tool);

        if (level.isClientSide) {
            return;
        }

        // Check for Fire Resistance
        MobEffectInstance fireRes = player.getEffect(MobEffects.FIRE_RESISTANCE);

        if (fireRes != null) {
            // Reduce Fire Resistance duration by 1 minute (20 ticks * 60 seconds)
            int reduction = 20 * 60;
            int remaining = Math.max(0, fireRes.getDuration() - reduction);

            // Remove current effect
            player.removeEffect(MobEffects.FIRE_RESISTANCE);

            // Re-apply with reduced duration if any time remains
            if (remaining > 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        remaining,
                        fireRes.getAmplifier(),
                        fireRes.isAmbient(),
                        fireRes.isVisible(),
                        fireRes.showIcon()
                ));
            }

            // No damage when the player has Fire Resistance
        } else {
            // No Fire Resistance → hurt and burn
            player.hurt(level.damageSources().hotFloor(), 5.0F); // 5 damage = 2.5 hearts
            player.setSecondsOnFire(4); // burn a bit for flavor
        }
    }
}
