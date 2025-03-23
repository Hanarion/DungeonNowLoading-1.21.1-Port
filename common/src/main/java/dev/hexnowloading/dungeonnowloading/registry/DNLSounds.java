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
    public static final Supplier<SoundEvent> REPULSOR_BARRIER_BUILD = registerSoundEvent("repulsor_barrier_build");
    public static final Supplier<SoundEvent> REPULSOR_FIZZLE = registerSoundEvent("repulsor_fizzle");
    public static final Supplier<SoundEvent> REPULSOR_ALERT = registerSoundEvent("repulsor_alert");
    public static final Supplier<SoundEvent> REPULSOR_BLINK = registerSoundEvent("repulsor_blink");
    public static final Supplier<SoundEvent> REPULSOR_BREAK = registerSoundEvent("repulsor_break");
    public static final Supplier<SoundEvent> REPULSOR_PLACE = registerSoundEvent("repulsor_place");
    public static final Supplier<SoundEvent> VERTEX_ARROW_BOOTUP = registerSoundEvent("vertex_arrow_bootup");
    public static final Supplier<SoundEvent> VERTEX_NODE_CONNECT = registerSoundEvent("vertex_node_connect");
    public static final Supplier<SoundEvent> VERTEX_TRANSMISSION_DAMAGE = registerSoundEvent("vertex_transmission_damage");
    public static final Supplier<SoundEvent> VERTEX_BOW_ARROW_CONVERT = registerSoundEvent("vertex_bow_arrow_convert");
    public static final Supplier<SoundEvent> VERTEX_BOW_PULL = registerSoundEvent("vertex_bow_pull");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_COMPONENT_DETONATION = registerSoundEvent("overcharged_redstone_block_component_detonation");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_DUST_COMBUSTION = registerSoundEvent("overcharged_redstone_block_dust_combustion");
    public static final Supplier<SoundEvent> OVERCHARGED_REDSTONE_BLOCK_TNT_EXPLOSION = registerSoundEvent("overcharged_redstone_block_tnt_explosion");
    public static final Supplier<SoundEvent> MENDING_AURA_POP = registerSoundEvent("mending_aura_pop");
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
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_BEAM = registerSoundEvent("fairkeeper_boros_beam");
    public static final Supplier<SoundEvent> FAIRKEEPER_BOROS_HEAL = registerSoundEvent("fairkeeper_boros_heal");


    private static <T extends SoundEvent> Supplier<SoundEvent> registerSoundEvent(String string) {
        return Services.REGISTRY.register(BuiltInRegistries.SOUND_EVENT, string, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(DungeonNowLoading.MOD_ID, string)));
    }

    private static Supplier<Holder<SoundEvent>> registerSoundEventHolder(String string) {
        Supplier<SoundEvent> soundEvent = registerSoundEvent(string);
        return () -> BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent.get());
    }


    public static void init() {}

}
