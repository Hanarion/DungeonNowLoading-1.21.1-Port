package dev.hexnowloading.dungeonnowloading.block;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExplosiveBarrelBlock extends GenericExplosiveBarrelBlock {
    public ExplosiveBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onDetonate(Level level, BlockPos pos, LivingEntity owner) {
        if (GeneralConfig.TOGGLE_DESTRUCTIVE_BLOCKS.get()) {
            level.explode(owner, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 4.0F, Level.ExplosionInteraction.TNT);
        } else {
            level.explode(owner, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
        }
    }
}
