package dev.hexnowloading.dungeonnowloading.registry;

import dev.hexnowloading.dungeonnowloading.platform.Services;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.function.Supplier;

public class DNLCreativeModeTabs {

    public static final Supplier<CreativeModeTab> DUNGEONNOWLOADING_TAB = register("main",
            () -> DNLItems.DNL_LOGO.get().getDefaultInstance(),
            (itemDisplayParameters, output) -> {
                // Spawn Eggs
                output.accept(DNLItems.CHAOS_SPAWNER_SPAWNEGG.get());
                //output.accept(DNLItems.FAIRKEEPER_SPAWNEGG.get());
                //output.accept(DNLItems.FAIRKEEPER_OUROS_SPAWNEGG.get());
                output.accept(DNLItems.HOLLOW_SPAWNEGG.get());
                output.accept(DNLItems.SPAWNER_CARRIER_SPAWNEGG.get());
                output.accept(DNLItems.GARHOLD_SPAWNEGG.get());
                output.accept(DNLItems.SCUTTLE_SPAWNEGG.get());
                output.accept(DNLItems.BALLISTA_GOLEM_SPAWNEGG.get());
                output.accept(DNLItems.SEALED_CHAOS_SPAWNEGG.get());
                output.accept(DNLItems.WHIMPER_SPAWNEGG.get());
                output.accept(DNLItems.WISP_SPAWNEGG.get());
                output.accept(DNLItems.WISP_LANTERN_SPAWNEGG.get());
                output.accept(DNLItems.SILK_SPIDER_SPAWNEGG.get());
                output.accept(DNLItems.REAPER_SPIDER_SPAWNEGG.get());
                // Items - Ingredients
                output.accept(DNLItems.SPAWNER_FRAGMENT.get());
                output.accept(DNLItems.SPAWNER_FRAME.get());
                output.accept(DNLItems.SPAWNER_BLADE.get());
                output.accept(DNLItems.SOUL_CLOTH.get());
                output.accept(DNLItems.SOUL_SILK.get());
                output.accept(DNLItems.CHAOTIC_HEXAHEDRON.get());
                output.accept(DNLItems.REDSTONE_CHIP.get());
                output.accept(DNLItems.REDSTONE_SUPPRESSOR.get());
                output.accept(DNLItems.REDSTONE_CIRCUIT.get());
                output.accept(DNLItems.REDSTONE_CORE.get());
                output.accept(DNLItems.COMBUSTION_CELL.get());
                output.accept(DNLItems.CATALYZED_REDSTONE.get());
                output.accept(DNLItems.DURITE.get());
                output.accept(DNLItems.MENDSTONE.get());

                // Items - Combat (Boss Item, then non-boss Item order)
                output.accept(DNLItems.SCEPTER_OF_SEALED_CHAOS.get());
                output.accept(DNLItems.LIFE_STEALER.get());
                output.accept(DNLItems.SPAWNER_SWORD.get());
                output.accept(DNLItems.VERTEX_BOW.get());
                output.accept(DNLItems.SCORCHER.get());
                output.accept(DNLItems.SOUL_SCORCHER.get());
                output.accept(DNLItems.COPPER_DETONATOR.get());
                output.accept(DNLItems.REPULSOR.get());
                output.accept(DNLItems.MIMICART.get());
                output.accept(DNLItems.WISPWARD_LANTERN_CART.get());
                output.accept(DNLItems.TIMED_WISPWARD_LANTERN_CART.get());
                output.accept(DNLItems.WISPLIGHT_ROD.get());
                output.accept(DNLItems.MIMIC_MUCUS.get());
                output.accept(DNLItems.RABBITLESS_RABBIT_STEW.get());
                output.accept(DNLItems.MIMICLING.get());
                // Items - Tools
                output.accept(DNLItems.MENDSTONE_CHALK.get());
                output.accept(DNLItems.MENDSTONE_PICKAXE.get());
                // Items - Armors
                output.accept(DNLItems.SPAWNER_HELMET.get());
                output.accept(DNLItems.SPAWNER_CHESTPLATE.get());
                output.accept(DNLItems.SPAWNER_LEGGINGS.get());
                output.accept(DNLItems.SPAWNER_BOOTS.get());
                // Enchantments - Books
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.BREAK_PROTECTION.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.AMPLIFICATION.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.NULLIFICATION.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.GIGANTISM.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.OVERWORKED.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.PACK_BLESSING.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.ARC_SHOT.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.PULSE_SHOT.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.SACRIFICE.get(), 1)));
                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(DNLEnchantments.RECKLESS.get(), 1)));

                // Items - Boss Related
                output.accept(DNLItems.GREAT_EXPERIENCE_BOTTLE.get());
                output.accept(DNLItems.SKULL_OF_CHAOS.get());
                output.accept(DNLItems.REDSTONE_IDOL.get());
                output.accept(DNLItems.FAIRKEEPER_SERPENT_CALLER.get());
                // Item - Music Disc
                output.accept(DNLItems.MUSIC_DISC_HELLSPAWN.get());
                output.accept(DNLItems.MUSIC_DISC_AOTSUGI.get());
                output.accept(DNLItems.MUSIC_DISC_BROKEN_AOTSUGI.get());
                output.accept(DNLItems.MUSIC_DISC_OUROS.get());
                output.accept(DNLItems.MUSIC_DISC_BOROS.get());
                output.accept(DNLItems.MUSIC_DISC_PYTHONIC_OVERDRIVE.get());

                // Blocks

                // Blocks - Design
                output.accept(DNLItems.COILING_STONE_PILLAR.get());
                output.accept(DNLItems.COILING_STONE_PILLAR_STAIRS.get());
                output.accept(DNLItems.COILING_STONE_PILLAR_SLAB.get());
                output.accept(DNLItems.COILING_STONE_PILLAR_WALL.get());
                output.accept(DNLItems.CHISELED_COILING_STONE_PILLAR.get());
                output.accept(DNLItems.COILING_STONE_PILLAR_CAPITAL.get());
                output.accept(DNLItems.STONE_TILES.get());
                output.accept(DNLItems.CRACKED_STONE_TILES.get());
                output.accept(DNLItems.STONE_TILE_STAIRS.get());
                output.accept(DNLItems.STONE_TILE_SLAB.get());
                output.accept(DNLItems.STONE_TILE_WALL.get());
                output.accept(DNLItems.SIGNALING_STONE_EMBLEM.get());
                output.accept(DNLItems.DUELING_STONE_EMBLEM.get());
                output.accept(DNLItems.PUZZLING_STONE_EMBLEM.get());
                output.accept(DNLItems.POLISHED_STONE.get());
                output.accept(DNLItems.BORDERED_STONE.get());
                output.accept(DNLItems.MOSS.get());
                output.accept(DNLItems.ACACIA_WOODEN_BOARD.get());
                output.accept(DNLItems.BAMBOO_WOODEN_BOARD.get());
                output.accept(DNLItems.BIRCH_WOODEN_BOARD.get());
                output.accept(DNLItems.CHERRY_WOODEN_BOARD.get());
                output.accept(DNLItems.CRIMSON_WOODEN_BOARD.get());
                output.accept(DNLItems.DARK_OAK_WOODEN_BOARD.get());
                output.accept(DNLItems.JUNGLE_WOODEN_BOARD.get());
                output.accept(DNLItems.MANGROVE_WOODEN_BOARD.get());
                output.accept(DNLItems.OAK_WOODEN_BOARD.get());
                output.accept(DNLItems.PALE_OAK_WOODEN_BOARD.get());
                output.accept(DNLItems.SPRUCE_WOODEN_BOARD.get());
                output.accept(DNLItems.WARPED_WOODEN_BOARD.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_MAGENTA.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_BLACK.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_BLUE.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_PURPLE.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_GREEN.get());
                output.accept(DNLItems.DUNGEON_BANNER_HOLLOW.get());
                output.accept(DNLItems.DUNGEON_BANNER_SPAWNER_CARRIER.get());
                output.accept(DNLItems.DUNGEON_BANNER_EXPERIENCE_BOTTLE.get());
                output.accept(DNLItems.DUNGEON_BANNER_CHAOS_SPAWNER.get());
                output.accept(DNLItems.DUNGEON_BANNER_WHIMPER_LANTERN.get());
                output.accept(DNLItems.DUNGEON_BANNER_GARHOLD_UPSIDEDOWN.get());
                output.accept(DNLItems.DUNGEON_BANNER_SKULL_OF_CHAOS.get());

                // Blocks - Mechanical
                output.accept(DNLItems.DUNGEON_WALL_TORCH.get());
                output.accept(DNLItems.BOOK_PILE.get());
                output.accept(DNLItems.EXPLOSIVE_BARREL.get());
                output.accept(DNLItems.SILVERFISH_BARREL.get());

                output.accept(DNLItems.COBBLESTONE_PEBBLE.get());
                output.accept(DNLItems.MOSSY_COBBLESTONE_PEBBLE.get());
                output.accept(DNLItems.IRON_INGOT_PILE.get());
                output.accept(DNLItems.GOLD_INGOT_PILE.get());
                output.accept(DNLItems.WOODEN_WALL_RACK.get());
                output.accept(DNLItems.WOODEN_WALL_PLATFORM.get());
                output.accept(DNLItems.SPIKES.get());
                output.accept(DNLItems.HAZARD_SIGN_BOTTLE.get());
                output.accept(DNLItems.HAZARD_SIGN_BUBBLE.get());
                output.accept(DNLItems.HAZARD_SIGN_CALTROP.get());
                output.accept(DNLItems.HAZARD_SIGN_DOWN.get());
                output.accept(DNLItems.HAZARD_SIGN_EXCLAMATION.get());
                output.accept(DNLItems.HAZARD_SIGN_FIRE.get());
                output.accept(DNLItems.HAZARD_SIGN_ICE.get());
                output.accept(DNLItems.HAZARD_SIGN_LEFT.get());
                output.accept(DNLItems.HAZARD_SIGN_MONSTER.get());
                output.accept(DNLItems.HAZARD_SIGN_PICKAXE.get());
                output.accept(DNLItems.HAZARD_SIGN_RIGHT.get());
                output.accept(DNLItems.HAZARD_SIGN_SOUND.get());
                output.accept(DNLItems.HAZARD_SIGN_SPIKES.get());
                output.accept(DNLItems.HAZARD_SIGN_SPIRAL.get());
                output.accept(DNLItems.HAZARD_SIGN_SWORD.get());
                output.accept(DNLItems.HAZARD_SIGN_UP.get());
                output.accept(DNLItems.CHAOS_SPAWNER_EDGE.get());
                output.accept(DNLItems.CHAOS_SPAWNER_DIAMOND_EDGE.get());
                output.accept(DNLItems.CHAOS_SPAWNER_DIAMOND_VERTEX.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BROKEN_EDGE.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BARRIER_CENTER.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BARRIER_EDGE.get());
                output.accept(DNLItems.CHAOS_SPAWNER_BARRIER_VERTEX.get());
                output.accept(DNLItems.FAIRKEEPER_CHEST.get());
                output.accept(DNLItems.WISE_FAIRKEEPER_CHEST.get());
                output.accept(DNLItems.FIERCE_FAIRKEEPER_CHEST.get());
                output.accept(DNLItems.FAIRKEEPER_SPAWNER.get());
                output.accept(DNLItems.OVERCHARGED_REDSTONE_BLOCK.get());
                output.accept(DNLItems.REDSTONE_LANE_I.get());
                output.accept(DNLItems.REDSTONE_LANE_L.get());
                output.accept(DNLItems.REDSTONE_LANE_T.get());
                output.accept(DNLItems.ROTATOR_PRESSURE_PLATE.get());
                output.accept(DNLItems.STONE_NOTCH.get());
                output.accept(DNLItems.COAL_STONE_NOTCH.get());
                output.accept(DNLItems.COPPER_STONE_NOTCH.get());
                output.accept(DNLItems.IRON_STONE_NOTCH.get());
                output.accept(DNLItems.GOLD_STONE_NOTCH.get());
                output.accept(DNLItems.REDSTONE_STONE_NOTCH.get());
                output.accept(DNLItems.AMETHYST_STONE_NOTCH.get());
                output.accept(DNLItems.LAPIS_STONE_NOTCH.get());
                output.accept(DNLItems.EMERALD_STONE_NOTCH.get());
                output.accept(DNLItems.QUARTZ_STONE_NOTCH.get());
                output.accept(DNLItems.GLOWSTONE_STONE_NOTCH.get());
                output.accept(DNLItems.PRISMARINE_STONE_NOTCH.get());
                output.accept(DNLItems.CHORUS_STONE_NOTCH.get());
                output.accept(DNLItems.ECHO_STONE_NOTCH.get());
                output.accept(DNLItems.DIAMOND_STONE_NOTCH.get());
                output.accept(DNLItems.NETHERITE_STONE_NOTCH.get());
                output.accept(DNLItems.SIGNAL_GATE.get());
                output.accept(DNLItems.SIGNAL_RAIL.get());
                output.accept(DNLItems.SCUTTLE_STATUE.get());
                output.accept(DNLItems.BALLISTA_GOLEM_STATUE.get());
                output.accept(DNLItems.VERTEX_PILLAR.get());
                output.accept(DNLItems.STONE_PRESERVER.get());
                output.accept(DNLItems.DURITE_CLUSTER.get());
                output.accept(DNLItems.LARGE_DURITE_BUD.get());
                output.accept(DNLItems.MEDIUM_DURITE_BUD.get());
                output.accept(DNLItems.SMALL_DURITE_BUD.get());
                output.accept(DNLItems.MENDSTONE_CHALK_MARK.get());
                output.accept(DNLItems.MENDING_TABLE.get());
                output.accept(DNLItems.DURITE_QUELLER.get());
                output.accept(DNLItems.DUNGEON_DIRECTOR.get());
                output.accept(DNLItems.SPAWN_NODE.get());
                output.accept(DNLItems.ZONE_WAND.get());

                output.accept(DNLItems.BRITTLESTONE.get());
                output.accept(DNLItems.DEEPSTEEL_BLOCK.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_FRAME.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_FLOATING.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_FRAME_TOP.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_SUSPENDED.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get());
                output.accept(DNLItems.DEEPSTEEL_SLOPED_PLATFORM_FLOATING.get());
                output.accept(DNLItems.DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL.get());
                output.accept(DNLItems.DEEPSTEEL_PLATFORM_ENCLOSED_STAIRS.get());
                output.accept(DNLItems.WEB_CARPET.get());
                output.accept(DNLItems.WEBBING_BLOCK.get());
                output.accept(DNLItems.WEBBING_NEST_BLOCK.get());
                output.accept(DNLItems.SUSPENDED_WEB.get());
                output.accept(DNLItems.BURNACLE.get());
                output.accept(DNLItems.WISPWARD_CHEST.get());
                output.accept(DNLItems.WISPWARD_LANTERN.get());
                output.accept(DNLItems.TIMED_WISPWARD_LANTERN.get());
                // Blocks - Trophies
                output.accept(DNLItems.DNL_LOGO.get());
                output.accept(DNLItems.LABYRINTH_TROPHY.get());
                output.accept(DNLItems.TEMPLE_OF_DUALITY_TROPHY.get());

                output.accept(DNLItems.PLAYER_STATUE.get());
            });

    public static Supplier<CreativeModeTab> register(String name, Supplier<ItemStack> iconSupplier, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return Services.REGISTRY.registerCreativeTab(name, iconSupplier, itemsGenerator);
    }

    public static void init() {}
}
