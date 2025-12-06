package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.block.entity.PotionBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PotionBarrelBlock extends GenericExplosiveBarrelBlock implements EntityBlock {
    public PotionBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (!(level instanceof ServerLevel sl)) return;
        PotionBarrelBlockEntity be = getBlockEntity(sl, pos);
        MobEffect effect = be != null ? be.getEffect() : null;
        if (effect == null) {
            sl.playSound(null, pos, SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            return;
        }
        AreaEffectCloud cloud = new AreaEffectCloud(sl, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        cloud.setOwner(owner);
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(640);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.setFixedColor(effect.getColor());
        cloud.addEffect(new MobEffectInstance(effect, 200));
        sl.addFreshEntity(cloud);
        sl.playSound(null, pos, SoundEvents.LINGERING_POTION_THROW, SoundSource.BLOCKS, 0.7F, 1.0F);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionBarrelBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null; // no ticking needed beyond the block's own fuse logic
    }
    private static @Nullable PotionBarrelBlockEntity getBlockEntity(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof PotionBarrelBlockEntity pb ? pb : null;
    }
}
