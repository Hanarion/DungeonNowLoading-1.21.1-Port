package dev.hexnowloading.dungeonnowloading.block.entity;

import dev.hexnowloading.dungeonnowloading.registry.DNLBlockEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

public class SoulExtractorBlockEntity extends BlockEntity {

    /* --------- Tuning knobs (prototype) --------- */
    private static final int   SAP_TICK_INTERVAL       = 10;     // every 10 ticks
    private static final float SAP_DAMAGE_PER_PULSE    = 5.0F;   // 0.5 heart per pulse
    private static final double SCAN_EXPAND            = 0.10D;  // small fudge so tall mobs still count

    /* --------- State --------- */
    private int souls = 0;
    private boolean paused = false;
    private boolean locked = false;
    private ResourceLocation filterEntityId = null; // null => no filter

    private int tickCounter = 0;

    public SoulExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(DNLBlockEntityTypes.SOUL_EXTRACTOR.get(), pos, state);
    }

    /* --------- Public API --------- */

    public int getSouls() {
        return souls;
    }

    public void addSoul(int amount) {
        if (amount <= 0) return;
        this.souls += amount;
        setChanged();
        updateComparator();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        setChanged();
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setChanged();
    }

    public void setFilter(EntityType<?> type) {
        this.filterEntityId = EntityType.getKey(type);
        setChanged();
    }

    public void clearFilter() {
        this.filterEntityId = null;
        setChanged();
    }

    public Optional<EntityType<?>> getFilterType() {
        if (filterEntityId == null || level == null) return Optional.empty();
        return Optional.ofNullable(level.registryAccess()
                .registryOrThrow(Registries.ENTITY_TYPE)
                .get(filterEntityId));
    }

    private void updateComparator() {
        if (level != null) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    /* --------- Tick --------- */

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoulExtractorBlockEntity be) {
        if (!(level instanceof ServerLevel server)) return;

        // Always funnel down first to support stacks
        be.transferDownward(server);

        if (be.paused) return;

        be.tickCounter++;
        if (be.tickCounter % SAP_TICK_INTERVAL != 0) return;

        // Scan entities occupying this block space
        AABB box = new AABB(pos).inflate(SCAN_EXPAND);
        List<Entity> entities = server.getEntities(null, box);

        for (Entity e : entities) {
            if (!(e instanceof LivingEntity living)) continue;

            // Players: require Luck; remove it; +1 soul; never damage players
            if (living instanceof Player player) {
                if (hasLuck(player)) {
                    removeLuck(player);
                    be.addSoul(1);
                }
                continue;
            }

            // Non-player: respect filter (if any)
            if (!be.passesFilter(living.getType())) continue;

            // Damage pulse; award soul if the pulse killed it
            boolean hurt = living.hurt(server.damageSources().magic(), SAP_DAMAGE_PER_PULSE);
            if (!hurt) continue;

            if (!living.isAlive() || living.getHealth() <= 0.0F || living.isDeadOrDying()) {
                be.addSoul(1);
            }
        }
    }

    private boolean passesFilter(EntityType<?> type) {
        if (filterEntityId == null) return true;
        return EntityType.getKey(type).equals(filterEntityId);
    }

    private static boolean hasLuck(Player player) {
        return player.hasEffect(MobEffects.LUCK);
    }

    private static void removeLuck(Player player) {
        if (player.hasEffect(MobEffects.LUCK)) {
            player.removeEffect(MobEffects.LUCK);
        }
    }

    /**
     * Extended extractor: if there is another extractor directly below, transfer all souls there.
     */
    private void transferDownward(ServerLevel server) {
        if (this.souls <= 0) return;

        BlockPos belowPos = worldPosition.below();
        BlockState belowState = server.getBlockState(belowPos);
        if (!belowState.is(DNLBlocks.SOUL_EXTRACTOR.get())) return;

        BlockEntity beBelow = server.getBlockEntity(belowPos);
        if (beBelow instanceof SoulExtractorBlockEntity target) {
            target.addSoul(this.souls);
            this.souls = 0;
            setChanged();
            updateComparator();
        }
    }

    /* --------- NBT --------- */

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Souls", souls);
        tag.putBoolean("Paused", paused);
        tag.putBoolean("Locked", locked);
        if (filterEntityId != null) {
            tag.putString("FilterId", filterEntityId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.souls = tag.getInt("Souls");
        this.paused = tag.getBoolean("Paused");
        this.locked = tag.getBoolean("Locked");
        this.filterEntityId = tag.contains("FilterId") ? new ResourceLocation(tag.getString("FilterId")) : null;
    }
}
