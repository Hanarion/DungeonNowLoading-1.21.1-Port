package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ScepterOfSealedChaosItem extends Item {

    private static final String MODE_BASIC = "SealedChaosBasic";

    public ScepterOfSealedChaosItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        Player player = useOnContext.getPlayer();
        ItemStack itemStack = useOnContext.getItemInHand();
        BlockPos clicked = useOnContext.getClickedPos();
        BlockState clickedState = level.getBlockState(clicked);

// cycle mode if amethyst cluster is clicked
        if (clickedState.is(net.minecraft.world.level.block.Blocks.AMETHYST_CLUSTER)) {
            if (!level.isClientSide && player != null) {
                boolean basic = itemStack.getOrCreateTag().getBoolean(MODE_BASIC);
                basic = !basic;
                itemStack.getOrCreateTag().putBoolean(MODE_BASIC, basic);

                player.displayClientMessage(
                        Component.translatable(
                                basic
                                        ? "item.dungeonnowloading.scepter_of_sealed_chaos.mode.basic"
                                        : "item.dungeonnowloading.scepter_of_sealed_chaos.mode.normal"
                        ),
                        true
                );
                level.playSound(null, clicked, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0F, basic ? 1.2F : 0.9F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (itemStack.getDamageValue() == itemStack.getMaxDamage()) {
            return InteractionResult.FAIL;
        } else {
            UUID uuid = Objects.requireNonNull(player.getUUID());
            player.getCooldowns().addCooldown(this, 600);
            BlockPos playerPos = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
            level.playSound(player, playerPos, DNLSounds.SEALED_CHAOS_PLACE.get(), SoundSource.PLAYERS, 0.5F, 0.8F);
            //level.playSound(player, playerPos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS);
            if (!level.isClientSide) {
                BlockPos blockPos = useOnContext.getClickedPos();
                Direction direction = useOnContext.getClickedFace();
                BlockState blockState = level.getBlockState(blockPos);
                BlockPos blockPos1;
                if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
                    blockPos1 = blockPos;
                } else {
                    blockPos1 = blockPos.relative(direction);
                }
                ((ServerLevel) level).sendParticles(ParticleTypes.POOF, blockPos1.getX() + 0.5F, blockPos1.getY() + 0.5F, blockPos1.getZ() + 0.5F, 20, 0.3D, 0.3D, 0.3D, 0.0D);
                ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, blockPos1.getX() + 0.5F, blockPos1.getY() + 0.5F, blockPos1.getZ() + 0.5F, 10, 0.3D, 0.3D, 0.3D, 0.0D);
                SealedChaosEntity sealedChaosEntity = DNLEntityTypes.SEALED_CHAOS.get().create(level);
                if (sealedChaosEntity != null) {
                    sealedChaosEntity.moveTo(blockPos1, 0.0F, 0.0F);
                    sealedChaosEntity.setOwnerUUID(player.getUUID());

                    boolean basic = itemStack.getOrCreateTag().getBoolean(MODE_BASIC);
                    sealedChaosEntity.setBasicVariant(basic);

                    level.addFreshEntity(sealedChaosEntity);
                }
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                itemStack.hurt(1, level.random, null);
            }
            return InteractionResult.SUCCESS;
        }
    }



    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        return super.use(level, player, interactionHand);
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairItemStack) {
        return repairItemStack.is(DNLItems.CHAOTIC_HEXAHEDRON.get()) || super.isValidRepairItem(itemStack, repairItemStack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_block").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_block.description").withStyle(ChatFormatting.DARK_GREEN));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_sealed_chaos").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.scepter_of_sealed_chaos.tooltip.right_click_sealed_chaos.description").withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
