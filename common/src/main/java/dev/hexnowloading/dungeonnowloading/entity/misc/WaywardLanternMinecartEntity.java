package dev.hexnowloading.dungeonnowloading.entity.misc;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;

public class WaywardLanternMinecartEntity extends AbstractMinecart {
    public static final double WARD_RADIUS = 5.0;
    private static final int REPEL_PERIOD_TICKS = 5;

    public WaywardLanternMinecartEntity(EntityType<? extends WaywardLanternMinecartEntity> type, Level level) {
        super(type, level);
    }

    public WaywardLanternMinecartEntity(Level level, double x, double y, double z) {
        super(DNLEntityTypes.WAYWARD_LANTERN_MINECART.get(), level, x, y, z);
    }

    @Override
    public Item getDropItem() {
        // swap to your custom minecart item later if you want
        return Items.MINECART;
    }

    @Override
    public Type getMinecartType() {
        return Type.RIDEABLE;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0D;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (this.tickCount % REPEL_PERIOD_TICKS != 0) return;

        AABB box = this.getBoundingBox().inflate(WARD_RADIUS);

        // Kill nearby wisps
        Predicate<WispEntity> aliveWisp = Entity::isAlive;
        List<WispEntity> wisps = level().getEntitiesOfClass(WispEntity.class, box, aliveWisp);
        for (WispEntity wisp : wisps) {
            wisp.hurt(level().damageSources().magic(), Float.MAX_VALUE);
        }

        // 🔥 Grant Fire Resistance to nearby players
        Predicate<Player> alivePlayer = Entity::isAlive;
        List<Player> players = level().getEntitiesOfClass(Player.class, box, alivePlayer);
        for (Player player : players) {
            // 5 seconds = 100 ticks. Applied every 5 ticks so it'll be constantly refreshed.
            player.addEffect(new MobEffectInstance(
                    MobEffects.FIRE_RESISTANCE,
                    100,       // duration in ticks
                    0,         // amplifier (0 = level I)
                    true,      // ambient (true = smaller particles, used for "passive" effects)
                    false,     // showParticles
                    true       // showIcon
            ));
        }
    }
}
