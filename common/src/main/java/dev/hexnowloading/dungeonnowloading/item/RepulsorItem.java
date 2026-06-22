package dev.hexnowloading.dungeonnowloading.item;

import net.minecraft.world.item.Item;
import dev.hexnowloading.dungeonnowloading.config.GeneralConfig;
import dev.hexnowloading.dungeonnowloading.entity.misc.RepulsorEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.supporter.DNLSupporters;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RepulsorItem extends Item {

    // --- Cosmetic mode NBT like Copper Detonator (2 states only) ---
    private static final String TAG_COSMETIC_MODE = "CosmeticMode";
    private static final String MODE_DEFAULT = "default";
    private static final String MODE_GOLDEN  = "golden";

    public RepulsorItem(Item.Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Direction face = ctx.getClickedFace();
        Level level = ctx.getLevel();
        ItemStack stack = ctx.getItemInHand();
        Player player = ctx.getPlayer();

        // Check the block you actually clicked
        BlockPos clickedPos = ctx.getClickedPos();
        if (level.getBlockState(clickedPos).is(Blocks.GOLD_BLOCK)) {
            if (!level.isClientSide && player != null) {
                UUID uuid = player.getUUID();
                boolean allowed = DNLSupporters.hasSkin(uuid, "repulsor_golden") || DNLSupporters.isSupporter(uuid);
                if (allowed) {
                    cycleCosmeticMode(stack);
                    String mode = getCosmeticMode(stack);
                    String nice = Character.toUpperCase(mode.charAt(0)) + mode.substring(1);
                    player.displayClientMessage(Component.literal("Current Repulsor: " + nice).withStyle(ChatFormatting.YELLOW), true);
                }
            }
            // Don’t place when toggling
            return InteractionResult.SUCCESS;
        }

        // placement path (unchanged, but rebuild the BlockPlaceContext only here)
        if (face == Direction.DOWN) return InteractionResult.FAIL;

        BlockPlaceContext placeCtx = new BlockPlaceContext(ctx);
        BlockPos placePos = placeCtx.getClickedPos();
        Vec3 center = Vec3.atBottomCenterOf(placePos);
        AABB aabb = DNLEntityTypes.REPULSOR.get().getDimensions().makeBoundingBox(center.x(), center.y(), center.z());
        if (!level.noCollision((Entity)null, aabb) || !level.getEntities((Entity)null, aabb).isEmpty())
            return InteractionResult.FAIL;

        if (level instanceof ServerLevel server) {
            Consumer<RepulsorEntity> consumer = EntityType.createDefaultStackConfig(server, stack, ctx.getPlayer());
            RepulsorEntity rep = (RepulsorEntity)DNLEntityTypes.REPULSOR.get().create(server, StackNbt.getTag(stack), consumer, placePos, MobSpawnType.SPAWN_EGG, true, true);
            if (rep == null) return InteractionResult.FAIL;

            // Force exact placement + orientation
            rep.moveTo(rep.getX(), rep.getY(), rep.getZ(), 0.0F, 0.0F);
            rep.setYRot(0.0F);
            rep.setYHeadRot(0.0F);

            // init health from item durability
            rep.setShieldHealth(this.getMaxDamage() - stack.getDamageValue());

            // Attach full source stack so enchants/NBT can be preserved when used up
            rep.setSourceStack(stack);

            // cosmetic gating
            boolean canGolden = player != null && (DNLSupporters.hasSkin(player.getUUID(), "repulsor_golden")
                    || DNLSupporters.isSupporter(player.getUUID()));
            rep.setSkin(canGolden && isGoldenMode(stack) ? RepulsorEntity.Skin.GOLDEN : RepulsorEntity.Skin.DEFAULT);
            rep.setSkinValidation(true);

            server.addFreshEntityWithPassengers(rep);

            // DO NOT add again; spawn() already added it to the world
            level.playSound(null, rep.getX(), rep.getY(), rep.getZ(),
                    SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
            rep.gameEvent(GameEvent.ENTITY_PLACE, player);
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    // --- Cosmetic helpers (same style as Copper Detonator) ---
    private static void cycleCosmeticMode(ItemStack stack) {
        String current = getCosmeticMode(stack);
        String next = current.equals(MODE_DEFAULT) ? MODE_GOLDEN : MODE_DEFAULT;
        StackNbt.update(stack, t -> t.putString(TAG_COSMETIC_MODE, next));
    }

    public static String getCosmeticMode(ItemStack stack) {
        String v = StackNbt.getOrCreateTag(stack).getString(TAG_COSMETIC_MODE);
        return v.isEmpty() ? MODE_DEFAULT : v;
    }

    public static boolean isGoldenMode(ItemStack stack) {
        return MODE_GOLDEN.equals(getCosmeticMode(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext level, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);
        if (GeneralConfig.TOGGLE_HELPFUL_ITEM_TOOLTIP.get()) {
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.ability_name").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.ability_description").withStyle(ChatFormatting.DARK_GRAY));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.right_click").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.right_click.description").withStyle(ChatFormatting.DARK_GREEN));
            components.add(CommonComponents.EMPTY);
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.right_click_with_redstone_dust").withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable("item.dungeonnowloading.repulsor.tooltip.right_click_with_redstone_dust.description").withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    public static void setCosmeticMode(ItemStack stack, String mode) {
        StackNbt.update(stack, t -> t.putString(TAG_COSMETIC_MODE, mode));
    }
    public static void setGolden(ItemStack stack)  { setCosmeticMode(stack, MODE_GOLDEN); }
    public static void setDefault(ItemStack stack) { setCosmeticMode(stack, MODE_DEFAULT); }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairItem) {
        return repairItem.is(Items.REDSTONE) || super.isValidRepairItem(stack, repairItem);
    }
}
