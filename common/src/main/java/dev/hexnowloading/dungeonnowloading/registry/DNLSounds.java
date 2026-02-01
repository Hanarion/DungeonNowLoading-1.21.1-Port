package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class DNLSounds {
    public static final Supplier<Holder<SoundEvent>> MUSIC_TEMPLE_OF_DUALITY = registerSoundEventHolder("music_temple_of_duality");
    public static final Supplier<SoundEvent> MUSIC_CLASH_OF_DUALITY_BASE = registerSoundEvent("music_clash_of_duality_base");
    public static final Supplier<SoundEvent> MENDING_TABLE_MEND = registerSoundEvent("mending_table_mend");
    public static final Supplier<SoundEvent> MUSIC_CLASH_OF_DUALITY_BOROS = registerSoundEvent("music_clash_of_duality_boros");
    public static final Supplier<SoundEvent> MUSIC_CLASH_OF_DUALITY_OUROS = registerSoundEvent("music_clash_of_duality_ouros");
    public static final Supplier<SoundEvent> DISC_CLASH_OF_DUALITY_BOROS = registerSoundEvent("disc_clash_of_duality_boros");
    public static final Supplier<SoundEvent> DISC_CLASH_OF_DUALITY_OUROS = registerSoundEvent("disc_clash_of_duality_ouros");
    public static final Supplier<SoundEvent> DISC_CLASH_OF_DUALITY_PYTHONIC_OVERDRIVE = registerSoundEvent("disc_clash_of_duality_pythonic_overdrive");
    public static final Supplier<SoundEvent> HIKE = registerSoundEvent("hike");
    public static final Supplier<SoundEvent> SCORCHER_OVERHEAT = registerSoundEvent("scorcher_overheat");
    public static final Supplier<SoundEvent> SCORCHER_SHOOT = registerSoundEvent("scorcher_shoot");
    public static final Supplier<SoundEvent> SCORCHER_STALL = registerSoundEvent("scorcher_stall");
    public static final Supplier<SoundEvent> SCORCHER_START = registerSoundEvent("scorcher_start");
    public static final Supplier<SoundEvent> SCORCHER_STOP = registerSoundEvent("scorcher_stop");
    public static final Supplier<SoundEvent> SCORCHER_UNSTABLE = registerSoundEvent("scorcher_unstable");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_OVERHEAT = registerSoundEvent("soul_scorcher_overheat");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_SHOOT = registerSoundEvent("soul_scorcher_shoot");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_STALL = registerSoundEvent("soul_scorcher_stall");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_START = registerSoundEvent("soul_scorcher_start");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_STOP = registerSoundEvent("soul_scorcher_stop");
    public static final Supplier<SoundEvent> SOUL_SCORCHER_UNSTABLE = registerSoundEvent("soul_scorcher_unstable");
    public static final Supplier<SoundEvent> FAIRKEEPER_SERPENT_CALLER_ACTIVATED = registerSoundEvent("fairkeeper_serpent_caller_activated");
    public static final Supplier<SoundEvent> REPULSOR_BARRIER_BUILD = registerSoundEvent("repulsor_barrier_build");
    public static final Supplier<SoundEvent> REPULSOR_FIZZLE = registerSoundEvent("repulsor_fizzle");
    public static final Supplier<SoundEvent> REPULSOR_ALERT = registerSoundEvent("repulsor_alert");
    public static final Supplier<SoundEvent> REPULSOR_BLINK = registerSoundEvent("repulsor_blink");
    public static final Supplier<SoundEvent> REPULSOR_BREAK = registerSoundEvent("repulsor_break");
    public static final Supplier<SoundEvent> REPULSOR_PLACE = registerSoundEvent("repulsor_place");
    public static final Supplier<SoundEvent> REPULSOR_RECHARGE = registerSoundEvent("repulsor_recharge");
    public static final Supplier<SoundEvent> SEEPING_SOUL_AMBIENT = registerSoundEvent("seeping_soul_ambient");
    public static final Supplier<SoundEvent> SEEPING_SOUL_DISSIPATE = registerSoundEvent("seeping_soul_dissipate");
    public static final Supplier<SoundEvent> SEEPING_SOUL_EXPAND = registerSoundEvent("seeping_soul_expand");
    public static final Supplier<SoundEvent> SEEPING_SOUL_HURT = registerSoundEvent("seeping_soul_hurt");
    public static final Supplier<SoundEvent> SEEPING_SOUL_RECALL = registerSoundEvent("seeping_soul_recall");
    public static final Supplier<SoundEvent> SEEPING_SOUL_REPLACE = registerSoundEvent("seeping_soul_replace");
    public static final Supplier<SoundEvent> VERTEX_ARROW_BOOTUP = registerSoundEvent("vertex_arrow_bootup");
    public static final Supplier<SoundEvent> VERTEX_NODE_CONNECT = registerSoundEvent("vertex_node_connect");
    public static final Supplier<SoundEvent> VERTEX_ARROW_DAMAGE = registerSoundEvent("vertex_arrow_damage");
    public static final Supplier<SoundEvent> VERTEX_BOW_ARROW_CONVERT = registerSoundEvent("vertex_bow_arrow_convert");
    public static final Supplier<SoundEvent> VERTEX_BOW_PULL = registerSoundEvent("vertex_bow_pull");
    public static final Supplier<SoundEvent> MENDSTONE_CHALK_DRAW = registerSoundEvent("mendstone_chalk_draw");
    public static final Supplier<SoundEvent> MENDSTONE_CHALK_MARK_BREAK = registerSoundEvent("mendstone_chalk_mark_break");
    public static final Supplier<SoundEvent> MENDSTONE_CHALK_MARK_CRACK = registerSoundEvent("mendstone_chalk_mark_crack");
    public static final Supplier<SoundEvent> SIGNAL_GATE_CLICK = registerSoundEvent("signal_gate_click");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION = registerSoundEvent("overcharged_redstone_block_component_detonation");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_DUST_COMBUSTION = registerSoundEvent("overcharged_redstone_block_dust_combustion");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_TNT_EXPLOSION = registerSoundEvent("overcharged_redstone_block_tnt_explosion");
    public static final Supplier<SoundEvent> DURITE_QUELLER_ACTIVATE = registerSoundEvent("durite_queller_activate");
    public static final Supplier<SoundEvent> DURITE_QUELLER_CRYSTAL_GROW = registerSoundEvent("durite_queller_crystal_grow");
    public static final Supplier<SoundEvent> DURITE_QUELLER_REPLACE_PRESERVER = registerSoundEvent("durite_queller_replace_preserver");
    public static final Supplier<SoundEvent> MENDING_AURA_POP = registerSoundEvent("mending_aura_pop");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_DIAMOND_NOTCH_BREAK = registerSoundEvent("chaos_spawner_diamond_notch_break");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_DIAMOND_NOTCH_HIT = registerSoundEvent("chaos_spawner_diamond_notch_hit");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_DIAMOND_NOTCH_REGENERATE = registerSoundEvent("chaos_spawner_diamond_notch_regenerate");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_BARRIER_BREAK = registerSoundEvent("chaos_spawner_barrier_break");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_BARRIER_REGENERATE = registerSoundEvent("chaos_spawner_barrier_regenerate");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_BUILD_UP = registerSoundEvent("chaos_spawner_build_up");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_SHOCKWAVE = registerSoundEvent("chaos_spawner_shockwave");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_SHOOT = registerSoundEvent("chaos_spawner_shoot");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_LAUGHTER = registerSoundEvent("chaos_spawner_laughter");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_CHAIN_BREAK = registerSoundEvent("chaos_spawner_chain_break");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_HURT = registerSoundEvent("chaos_spawner_hurt");
    public static final Supplier<SoundEvent> CHAOS_SPAWNER_DEATH = registerSoundEvent("chaos_spawner_death");
    public static final Supplier<SoundEvent> HOLLOW_AMBIENT = registerSoundEvent("hollow_ambient");
    public static final Supplier<SoundEvent> HOLLOW_HURT = registerSoundEvent("hollow_hurt");
    public static final Supplier<SoundEvent> HOLLOW_DEATH = registerSoundEvent("hollow_death");
    public static final Supplier<SoundEvent> WHIMPER_AMBIENT = registerSoundEvent("whimper_ambient");
    public static final Supplier<SoundEvent> WHIMPER_HURT = registerSoundEvent("whimper_hurt");
    public static final Supplier<SoundEvent> WHIMPER_DEATH = registerSoundEvent("whimper_death");
    public static final Supplier<SoundEvent> SCUTTLE_WAKING = registerSoundEvent("scuttle_waking");
    public static final Supplier<SoundEvent> SCUTTLE_STEP = registerSoundEvent("scuttle_step");
    public static final Supplier<SoundEvent> SCUTTLE_AMBIENT = registerSoundEvent("scuttle_idle");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_OPEN = registerSoundEvent("scuttle_shooting_open");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_CHARGE = registerSoundEvent("scuttle_shooting_charge");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_BURST = registerSoundEvent("scuttle_shooting_burst");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_FLAME = registerSoundEvent("scuttle_shooting_flame");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_STOP = registerSoundEvent("scuttle_shooting_stop");
    public static final Supplier<SoundEvent> SCUTTLE_SHOOTING_CLOSE = registerSoundEvent("scuttle_shooting_close");
    public static final Supplier<SoundEvent> SCUTTLE_DEFLECT = registerSoundEvent("scuttle_deflect");
    public static final Supplier<SoundEvent> SCUTTLE_HURT = registerSoundEvent("scuttle_hurt");
    public static final Supplier<SoundEvent> SCUTTLE_DEATH = registerSoundEvent("scuttle_death");
    public static final Supplier<SoundEvent> SEALED_CHAOS_BULLET_HIT = registerSoundEvent("sealed_chaos_bullet_hit");
    public static final Supplier<SoundEvent> SEALED_CHAOS_BULLET_SHOOT = registerSoundEvent("sealed_chaos_bullet_shoot");
    public static final Supplier<SoundEvent> SEALED_CHAOS_PLACE = registerSoundEvent("sealed_chaos_place");
    public static final Supplier<SoundEvent> SEALED_CHAOS_TAKE = registerSoundEvent("sealed_chaos_take");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_CHARGE = registerSoundEvent("spawner_carrier_charge");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_DEATH = registerSoundEvent("spawner_carrier_death");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_HURT = registerSoundEvent("spawner_carrier_hurt");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_IDLE = registerSoundEvent("spawner_carrier_idle");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_SMASH = registerSoundEvent("spawner_carrier_smash");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_SPAWN_MOB = registerSoundEvent("spawner_carrier_spawn_mob");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_SPAWNER_BREAK = registerSoundEvent("spawner_carrier_spawner_break");
    public static final Supplier<SoundEvent> SPAWNER_CARRIER_STEP = registerSoundEvent("spawner_carrier_step");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_DEATH = registerSoundEvent("ballista_golem_death");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_HURT = registerSoundEvent("ballista_golem_hurt");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_SHOOT = registerSoundEvent("ballista_golem_shoot");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_RELOAD = registerSoundEvent("ballista_golem_reload");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_STEP = registerSoundEvent("ballista_golem_step");
    public static final Supplier<SoundEvent> BALLISTA_GOLEM_WAKING = registerSoundEvent("ballista_golem_waking");
    public static final Supplier<SoundEvent> COPPER_CREEP_DEATH = registerSoundEvent("copper_creep_death");
    public static final Supplier<SoundEvent> COPPER_CREEP_HIT = registerSoundEvent("copper_creep_hit");
    public static final Supplier<SoundEvent> COPPER_CREEP_LAND = registerSoundEvent("copper_creep_land");
    public static final Supplier<SoundEvent> COPPER_CREEP_PRIME = registerSoundEvent("copper_creep_prime");
    public static final Supplier<SoundEvent> COPPER_CREEP_SIT_DOWN = registerSoundEvent("copper_creep_sit_down");
    public static final Supplier<SoundEvent> COPPER_CREEP_SPAWN = registerSoundEvent("copper_creep_spawn");
    public static final Supplier<SoundEvent> COPPER_CREEP_STAND_UP = registerSoundEvent("copper_creep_stand_up");
    public static final Supplier<SoundEvent> COPPER_CREEP_STEP = registerSoundEvent("copper_creep_step");
    public static final Supplier<SoundEvent> COPPER_DETONATOR_BEEP = registerSoundEvent("copper_detonator_beep");
    public static final Supplier<SoundEvent> COPPER_DETONATOR_READY = registerSoundEvent("copper_detonator_ready");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_HEAL = registerSoundEvent("fairkeeper_boros_heal");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_BEAM = registerSoundEvent("fairkeeper_boros_beam");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_ARMOR_BREAK = registerSoundEvent("fairkeeper_boros_armor_break");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_ARMOR_HIT = registerSoundEvent("fairkeeper_boros_armor_hit");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_ARROW_SHOOT = registerSoundEvent("fairkeeper_boros_arrow_shoot");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_ARROW_WARNING = registerSoundEvent("fairkeeper_boros_arrow_warning");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_DEATH = registerSoundEvent("fairkeeper_boros_death");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_FIRE_ATTACK = registerSoundEvent("fairkeeper_boros_fire_attack");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_HURT = registerSoundEvent("fairkeeper_boros_hurt");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_SLITHER = registerSoundEvent("fairkeeper_boros_slither");

    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_CANNON_BREAK = registerSoundEvent("fairkeeper_ouros_cannon_break");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_SHOOT_VERTEX_DOMAIN = registerSoundEvent("fairkeeper_ouros_shoot_vertex_domain");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_DEATH = registerSoundEvent("fairkeeper_ouros_death");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_HURT = registerSoundEvent("fairkeeper_ouros_hurt");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_SLITHER = registerSoundEvent("fairkeeper_ouros_slither");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_CANNON_DOOR_OPEN = registerSoundEvent("fairkeeper_ouros_door_cannon_spawn");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_DOOR_CLOSE = registerSoundEvent("fairkeeper_ouros_door_close");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_PILLAR_DOOR_OPEN = registerSoundEvent("fairkeeper_ouros_door_pillar_spawn");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_SCUTTLE_DOOR_OPEN = registerSoundEvent("fairkeeper_ouros_door_scuttle_spawn");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_CANNON_SHOOT = registerSoundEvent("fairkeeper_ouros_cannon_shoot");
    public static final Supplier<SoundEvent> FAIRKEEPER_OUROS_PILLAR_LAND = registerSoundEvent("fairkeeper_ouros_pillar_land");
    public static final Supplier<SoundEvent> FAIRKEEPERS_INTRO = registerSoundEvent("fairkeepers_intro");
    public static final Supplier<SoundEvent> FAIRKEEPER_MOUTH_CLOSE = registerSoundEvent("fairkeeper_mouth_close");
    public static final Supplier<SoundEvent> FAIRKEEPER_MOUTH_OPEN = registerSoundEvent("fairkeeper_mouth_open");













    private static <T extends SoundEvent> Supplier<SoundEvent> registerSoundEvent(String string) {
        return Services.REGISTRY.register(BuiltInRegistries.SOUND_EVENT, string, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DungeonNowLoading.MOD_ID, string)));
    }

    private static Supplier<Holder<SoundEvent>> registerSoundEventHolder(String string) {
        Supplier<SoundEvent> soundEvent = registerSoundEvent(string);
        return () -> BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent.get());
    }

    public static void init() {}

}
