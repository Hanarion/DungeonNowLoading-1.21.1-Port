package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.projectile.BulletEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.BulletType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BulletItem extends Item {

    private static final String NBT_BULLET_TYPE = "BulletType";

    public BulletItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // --- SHIFT-RIGHT-CLICK: cycle bullet type ---
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                BulletType current = getBulletType(stack);
                BulletType next = nextType(current);
                setBulletType(stack, next);

                // Show mode in action bar (or chat if you flip the boolean)
                player.displayClientMessage(
                        Component.literal("Bullet: " + next.name()), // change to translatable if you want
                        true
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // --- NORMAL RIGHT-CLICK: fire bullet ---
        player.disableShield(false);
        BulletType type = getBulletType(stack);

        if (!level.isClientSide) {
            BulletEntity bullet = new BulletEntity(level, player, type);
            bullet.setItem(stack.copy()); // for rendering, optional

            bullet.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    type.getVelocity(),
                    type.getInaccuracy()
            );

            level.addFreshEntity(bullet);

            level.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CROSSBOW_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // --------------------
    // NBT helpers
    // --------------------

    public static BulletType getBulletType(ItemStack stack) {
        if (!stack.hasTag() || !stack.getOrCreateTag().contains(NBT_BULLET_TYPE)) {
            return BulletType.IRON; // default
        }
        String name = stack.getOrCreateTag().getString(NBT_BULLET_TYPE);
        return BulletType.byName(name); // falls back to IRON in byName()
    }

    public static void setBulletType(ItemStack stack, BulletType type) {
        stack.getOrCreateTag().putString(NBT_BULLET_TYPE, type.name());
    }

    private static BulletType nextType(BulletType current) {
        return switch (current) {
            case IRON -> BulletType.GOLD;
            case GOLD -> BulletType.COPPER;
            case COPPER -> BulletType.IRON;
        };
    }
}
