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
                // Items - Boss Related
                output.accept(DNLItems.GREAT_EXPERIENCE_BOTTLE.get());
                output.accept(DNLItems.SKULL_OF_CHAOS.get());
                output.accept(DNLItems.REDSTONE_IDOL.get());
                output.accept(DNLItems.FAIRKEEPER_SERPENT_CALLER.get());
                // Item - Music Disc
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

                // Blocks - Mechanical
                output.accept(DNLItems.DUNGEON_WALL_TORCH.get());
                output.accept(DNLItems.BOOK_PILE.get());
                output.accept(DNLItems.EXPLOSIVE_BARREL.get());
                output.accept(DNLItems.SILVERFISH_BARREL.get());
//                output.accept(DNLItems.CRYO_BARREL.get());
//                output.accept(DNLItems.PYRO_BARREL.get());
//                output.accept(DNLItems.CALTROP_BARREL.get());

                // Potion Barrels
//                try {
//                    BuiltInRegistries.MOB_EFFECT.forEach(effect -> {
//                        if (effect == null) return;
//                        ItemStack stack = DNLItems.POTION_BARREL.get().getDefaultInstance();
//                        CompoundTag beTag = new CompoundTag();
//                        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
//                        if (id == null) return;
//                        beTag.putString("Effect", id.toString());
//                        CompoundTag tag = new CompoundTag();
//                        tag.put("BlockEntityTag", beTag);
//                        stack.setTag(tag);
//                        stack.setHoverName(Component.translatable("item.dungeonnowloading.potion_barrel.effect", net.minecraft.network.chat.Component.translatable(effect.getDescriptionId())));
//                        output.accept(stack);
//                    });
//                } catch (Exception ignored) {}

                output.accept(DNLItems.COBBLESTONE_PEBBLE.get());
                output.accept(DNLItems.MOSSY_COBBLESTONE_PEBBLE.get());
                output.accept(DNLItems.IRON_INGOT_PILE.get());
                output.accept(DNLItems.GOLD_INGOT_PILE.get());
                output.accept(DNLItems.WOODEN_WALL_RACK.get());
                output.accept(DNLItems.WOODEN_WALL_PLATFORM.get());
                output.accept(DNLItems.SPIKES.get());
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
                output.accept(DNLItems.SCUTTLE_STATUE.get());
                output.accept(DNLItems.BALLISTA_GOLEM_STATUE.get());
                output.accept(DNLItems.VERTEX_PILLAR.get());
                output.accept(DNLItems.MENDING_AURA.get());
                output.accept(DNLItems.MENDING_AURA_STAIRS.get());
                output.accept(DNLItems.MENDING_AURA_SLAB.get());
                output.accept(DNLItems.MENDING_AURA_FENCE.get());
                output.accept(DNLItems.MENDING_AURA_WALL.get());
                output.accept(DNLItems.MENDING_AURA_PATH.get());
                output.accept(DNLItems.MENDING_AURA_PANE.get());
                output.accept(DNLItems.STONE_PRESERVER.get());
                output.accept(DNLItems.DURITE_CLUSTER.get());
                output.accept(DNLItems.LARGE_DURITE_BUD.get());
                output.accept(DNLItems.MEDIUM_DURITE_BUD.get());
                output.accept(DNLItems.SMALL_DURITE_BUD.get());
                output.accept(DNLItems.MENDSTONE_CHALK_MARK.get());
                output.accept(DNLItems.MENDING_TABLE.get());
                output.accept(DNLItems.DURITE_QUELLER.get());

                // Blocks - Trophies
                output.accept(DNLItems.DNL_LOGO.get());
                output.accept(DNLItems.LABYRINTH_TROPHY.get());
                output.accept(DNLItems.TEMPLE_OF_DUALITY_TROPHY.get());

                // Azuro Set
                /*output.accept(DNLItems.AZURO_OAK_LOG.get());
                output.accept(DNLItems.STRIPPED_AZURO_OAK_LOG.get());
                output.accept(DNLItems.AZURO_OAK_PLANKS.get());
                output.accept(DNLItems.AZURO_LEAVES.get());
                output.accept(DNLItems.AZURO_HANGING_LEAVES_TIP.get());
                output.accept(DNLItems.AZURO_OAK_PLANK_STAIRS.get());
                output.accept(DNLItems.AZURO_OAK_PLANK_SLAB.get());
                output.accept(DNLItems.AZURO_OAK_PLANK_FENCE.get());
                output.accept(DNLItems.AZURO_OAK_PLANK_FENCE_GATE.get());
                output.accept(DNLItems.AZURO_OAK_BUTTON.get());
                output.accept(DNLItems.AZURO_OAK_PRESSURE_PLATE.get());
                output.accept(DNLItems.AZURO_OAK_DOOR.get());*/
                output.accept(DNLItems.PLAYER_STATUE.get());
            });

    public static Supplier<CreativeModeTab> register(String name, Supplier<ItemStack> iconSupplier, CreativeModeTab.DisplayItemsGenerator itemsGenerator) {
        return Services.REGISTRY.registerCreativeTab(name, iconSupplier, itemsGenerator);
    }

    public static void init() {}
}
