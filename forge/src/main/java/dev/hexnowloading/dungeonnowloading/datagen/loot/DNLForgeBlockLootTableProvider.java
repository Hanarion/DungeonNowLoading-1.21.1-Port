package dev.hexnowloading.dungeonnowloading.datagen.loot;

import dev.hexnowloading.dungeonnowloading.block.FairkeeperChestBlock;
import dev.hexnowloading.dungeonnowloading.block.PileBlock;
import dev.hexnowloading.dungeonnowloading.platform.ForgeCommonRegistryHelper;
import dev.hexnowloading.dungeonnowloading.registry.DNLBlocks;
import dev.hexnowloading.dungeonnowloading.registry.DNLItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class DNLForgeBlockLootTableProvider extends BlockLootSubProvider {
    public DNLForgeBlockLootTableProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(DNLBlocks.COILING_STONE_PILLAR.get());
        this.dropSelf(DNLBlocks.COILING_STONE_PILLAR_STAIRS.get());
        this.add(DNLBlocks.COILING_STONE_PILLAR_SLAB.get(), block -> createSlabItemTable(DNLBlocks.COILING_STONE_PILLAR_SLAB.get()));
        this.dropSelf(DNLBlocks.COILING_STONE_PILLAR_WALL.get());
        this.dropSelf(DNLBlocks.CHISELED_COILING_STONE_PILLAR.get());
        this.dropSelf(DNLBlocks.COILING_STONE_PILLAR_CAPITAL.get());
        this.dropSelf(DNLBlocks.STONE_TILES.get());
        this.dropSelf(DNLBlocks.CRACKED_STONE_TILES.get());
        this.dropSelf(DNLBlocks.STONE_TILE_STAIRS.get());
        this.add(DNLBlocks.STONE_TILE_SLAB.get(), block -> createSlabItemTable(DNLBlocks.STONE_TILE_SLAB.get()));
        this.dropSelf(DNLBlocks.STONE_TILE_WALL.get());
        this.add(DNLBlocks.MOSS.get(), block -> this.createMultifaceBlockDrops(block, HAS_SHEARS));
        this.dropSelf(DNLBlocks.SIGNALING_STONE_EMBLEM.get());
        this.dropSelf(DNLBlocks.DUELING_STONE_EMBLEM.get());
        this.dropSelf(DNLBlocks.PUZZLING_STONE_EMBLEM.get());
        this.dropSelf(DNLBlocks.POLISHED_STONE.get());
        this.dropSelf(DNLBlocks.BORDERED_STONE.get());
        this.dropSelf(DNLBlocks.STONE_NOTCH.get());
        this.dropSelf(DNLBlocks.OVERCHARGED_REDSTONE_BLOCK.get());
        this.dropSelf(DNLBlocks.REDSTONE_LANE_I.get());
        this.dropSelf(DNLBlocks.REDSTONE_LANE_L.get());
        this.dropSelf(DNLBlocks.REDSTONE_LANE_T.get());
        this.dropSelf(DNLBlocks.ROTATOR_PRESSURE_PLATE.get());
        this.add(DNLBlocks.COAL_STONE_NOTCH.get(), block -> notchBlock(block, Items.COAL));
        this.add(DNLBlocks.COPPER_STONE_NOTCH.get(), block -> notchBlock(block, Items.COPPER_INGOT));
        this.add(DNLBlocks.IRON_STONE_NOTCH.get(), block -> notchBlock(block, Items.IRON_INGOT));
        this.add(DNLBlocks.GOLD_STONE_NOTCH.get(), block -> notchBlock(block, Items.GOLD_INGOT));
        this.add(DNLBlocks.REDSTONE_STONE_NOTCH.get(), block -> notchBlock(block, Items.REDSTONE));
        this.add(DNLBlocks.AMETHYST_STONE_NOTCH.get(), block -> notchBlock(block, Items.AMETHYST_SHARD));
        this.add(DNLBlocks.LAPIS_STONE_NOTCH.get(), block -> notchBlock(block, Items.LAPIS_LAZULI));
        this.add(DNLBlocks.EMERALD_STONE_NOTCH.get(), block -> notchBlock(block, Items.EMERALD));
        this.add(DNLBlocks.QUARTZ_STONE_NOTCH.get(), block -> notchBlock(block, Items.QUARTZ));
        this.add(DNLBlocks.GLOWSTONE_STONE_NOTCH.get(), block -> notchBlock(block, Items.GLOWSTONE_DUST));
        this.add(DNLBlocks.PRISMARINE_STONE_NOTCH.get(), block -> notchBlock(block, Items.PRISMARINE_SHARD));
        this.add(DNLBlocks.CHORUS_STONE_NOTCH.get(), block -> notchBlock(block, Items.POPPED_CHORUS_FRUIT));
        this.add(DNLBlocks.ECHO_STONE_NOTCH.get(), block -> notchBlock(block, Items.ECHO_SHARD));
        this.add(DNLBlocks.DIAMOND_STONE_NOTCH.get(), block -> notchBlock(block, Items.DIAMOND));
        this.add(DNLBlocks.NETHERITE_STONE_NOTCH.get(), block -> notchBlock(block, Items.NETHERITE_INGOT));
        this.dropSelf(DNLBlocks.SIGNAL_GATE.get());
        this.add(DNLBlocks.BOOK_PILE.get(), block -> bookPile());
        this.add(DNLBlocks.COBBLESTONE_PEBBLES.get(), block -> pileBlock(block, DNLItems.COBBLESTONE_PEBBLE.get()));
        this.add(DNLBlocks.MOSSY_COBBLESTONE_PEBBLES.get(), block -> pileBlock(block, DNLItems.MOSSY_COBBLESTONE_PEBBLE.get()));
        this.dropSelf(DNLBlocks.DUNGEON_WALL_TORCH.get());
        this.add(DNLBlocks.EXPLOSIVE_BARREL.get(), block -> createSingleItemTable(Items.GUNPOWDER, UniformGenerator.between(1.0F, 3.0F)));
        this.add(DNLBlocks.IRON_INGOT_PILE.get(), block -> pileBlock(block, Items.IRON_INGOT));
        this.add(DNLBlocks.GOLD_INGOT_PILE.get(), block -> pileBlock(block, Items.GOLD_INGOT));
        this.dropSelf(DNLBlocks.WOODEN_WALL_RACK.get());
        this.dropSelf(DNLBlocks.WOODEN_WALL_PLATFORM.get());
        this.add(DNLBlocks.FAIRKEEPER_CHEST.get(), this::fairkeeperChestBlock);
        this.dropSelf(DNLBlocks.WISE_FAIRKEEPER_CHEST.get());
        this.dropSelf(DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get());
        this.add(DNLBlocks.BALLISTA_GOLEM_STATUE.get(), createSilkTouchOnlyTable(DNLItems.BALLISTA_GOLEM_STATUE.get()));
        this.add(DNLBlocks.BALLISTA_GOLEM_STATUE_PART.get(), createSilkTouchOnlyTable(DNLItems.BALLISTA_GOLEM_STATUE.get()));
        this.add(DNLBlocks.SCUTTLE_STATUE.get(), createSilkTouchOnlyTable(DNLItems.SCUTTLE_STATUE.get()));
        this.add(DNLBlocks.REDSTONE_IDOL.get(), block -> createSingleItemTable(DNLItems.REDSTONE_IDOL.get()));
        this.dropSelf(DNLBlocks.LABYRINTH_TROPHY.get());
        this.dropSelf(DNLBlocks.TEMPLE_OF_DUALITY_TROPHY.get());
        this.dropSelf(DNLBlocks.MENDING_TABLE.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_LOG.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_PLANKS.get());
        this.dropSelf(DNLBlocks.STRIPPED_AZURO_OAK_LOG.get());
        this.add(DNLBlocks.AZURO_LEAVES.get(),createShearsOnlyDrop(DNLBlocks.AZURO_LEAVES.get()));
        this.add(DNLBlocks.AZURO_HANGING_LEAVES.get(),createShearsOnlyDrop(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get()));
        this.add(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get(),createShearsOnlyDrop(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get()));
        this.dropSelf(DNLBlocks.AZURO_OAK_PLANK_STAIRS.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_PLANK_SLAB.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_PLANK_FENCE.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_PLANK_FENCE_GATE.get());
        this.add(DNLBlocks.AZURO_OAK_DOOR.get(), block -> LootTable.lootTable().withPool(LootPool.lootPool().when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoorBlock.HALF, DoubleBlockHalf.LOWER))).add(LootItem.lootTableItem(block))));
        this.dropSelf(DNLBlocks.AZURO_OAK_BUTTON.get());
        this.dropSelf(DNLBlocks.AZURO_OAK_PRESSURE_PLATE.get());
//        this.dropSelf(DNLBlocks.AZURO_OAK_TRAPDOOR.get());
        this.addPlayerStatueDrop(DNLBlocks.PLAYER_STATUE.get());
        this.add(DNLBlocks.DURITE_CLUSTER.get(), b -> duriteClusterWithOverflow(b, DNLItems.DURITE.get()));
        this.add(DNLBlocks.LARGE_DURITE_BUD.get(),  b -> duriteBudWithChanceOverflow(b, DNLItems.DURITE.get(), 0.75f));
        this.add(DNLBlocks.MEDIUM_DURITE_BUD.get(), b -> duriteBudWithChanceOverflow(b, DNLItems.DURITE.get(), 0.50f));
        this.add(DNLBlocks.SMALL_DURITE_BUD.get(),  b -> duriteBudWithChanceOverflow(b, DNLItems.DURITE.get(), 0.25f));
        this.add(DNLBlocks.DURITE_QUELLER.get(), block -> createSingleItemTable(Items.OBSIDIAN));
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_MAGENTA.get(), DNLItems.DUNGEON_BANNER_SPAWNER_MAGENTA.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_BLACK.get(),   DNLItems.DUNGEON_BANNER_SPAWNER_BLACK.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_BLUE.get(),    DNLItems.DUNGEON_BANNER_SPAWNER_BLUE.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_PURPLE.get(),  DNLItems.DUNGEON_BANNER_SPAWNER_PURPLE.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_GREEN.get(),   DNLItems.DUNGEON_BANNER_SPAWNER_GREEN.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_HOLLOW.get(),          DNLItems.DUNGEON_BANNER_HOLLOW.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SPAWNER_CARRIER.get(), DNLItems.DUNGEON_BANNER_SPAWNER_CARRIER.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_EXPERIENCE_BOTTLE.get(), DNLItems.DUNGEON_BANNER_EXPERIENCE_BOTTLE.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_CHAOS_SPAWNER.get(),    DNLItems.DUNGEON_BANNER_CHAOS_SPAWNER.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_WHIMPER_LANTERN.get(),  DNLItems.DUNGEON_BANNER_WHIMPER_LANTERN.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_GARHOLD_UPSIDEDOWN.get(), DNLItems.DUNGEON_BANNER_GARHOLD_UPSIDEDOWN.get());
        dungeonBannerDrops(DNLBlocks.DUNGEON_BANNER_SKULL_OF_CHAOS.get(),   DNLItems.DUNGEON_BANNER_SKULL_OF_CHAOS.get());
    }

    private void dungeonBannerDrops(Block bannerBlock, Item drop) {
        LootTable.Builder table = LootTable.lootTable();

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(drop));

        table.withPool(pool);
        add(bannerBlock, table);
    }

    private LootTable.Builder fairkeeperChestBlock(Block block) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(DNLItems.WISE_FAIRKEEPER_CHEST.get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.FAIRKEEPER_CHEST.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(FairkeeperChestBlock.FAIRKEEPER_ALERT, false)))))
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(DNLItems.FIERCE_FAIRKEEPER_CHEST.get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.FAIRKEEPER_CHEST.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(FairkeeperChestBlock.FAIRKEEPER_ALERT, true)))));
        //.when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.FAIRKEEPER_CHEST.get())
        //.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 1))))))
    }

    private LootTable.Builder notchBlock(Block block, Item item) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(HAS_NO_SILK_TOUCH)))
                .withPool(this.applyExplosionCondition(item, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(DNLItems.STONE_NOTCH.get())
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(HAS_NO_SILK_TOUCH)))
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(block)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(HAS_SILK_TOUCH)));
    }

    private LootTable.Builder bookPile() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(0.25F)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.BOOK_PILE.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 1))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(2.0F))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(0.25F)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.BOOK_PILE.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 2))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(3.0F))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(0.25F)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.BOOK_PILE.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 3))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(4.0F))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .apply(EnchantRandomlyFunction.randomEnchantment().when(LootItemRandomChanceCondition.randomChance(0.25F)))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(DNLBlocks.BOOK_PILE.get())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 4))));
    }

    private LootTable.Builder pileBlock(Block block, ItemLike itemLike) {
        return LootTable.lootTable()
                .withPool(this.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(itemLike)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 1))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 2))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 3))))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F))
                                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PileBlock.PILE, 4))))
                        )));
    }

    private LootTable.Builder duriteClusterWithOverflow(Block clusterBlock, Item duriteItem) {
        // first drop: always 100% regardless of fortune
        float[] first = new float[] { 1f, 1f, 1f, 1f };
        // overflow for the second: F0=0, F1=0.50, F2=0.75, F3=1.00
        float[] extra = new float[] { 0f, 0.50f, 0.75f, 1.00f };

        return LootTable.lootTable()
                // Silk Touch → drop the cluster block
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_SILK_TOUCH)
                        .add(LootItem.lootTableItem(clusterBlock)))

                // First Durite (guaranteed 1 on no-silk)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_NO_SILK_TOUCH)
                        .add(LootItem.lootTableItem(duriteItem)
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, first))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                .apply(ApplyExplosionDecay.explosionDecay())))

                // Second Durite (overflow chance by Fortune level)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_NO_SILK_TOUCH)
                        .add(LootItem.lootTableItem(duriteItem)
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, extra))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                .apply(ApplyExplosionDecay.explosionDecay())));
    }

    private LootTable.Builder duriteBudWithChanceOverflow(Block budBlock, Item duriteItem, float baseChance /*0..1*/) {
        // +25% per level
        float[] first = new float[] {
                clamp01(baseChance + 0.25f * 0), // no fortune
                clamp01(baseChance + 0.25f * 1), // F1
                clamp01(baseChance + 0.25f * 2), // F2
                clamp01(baseChance + 0.25f * 3), // F3
        };
        float[] extra = new float[] {
                overflow(baseChance + 0.25f * 0), // max(0, chance-1)
                overflow(baseChance + 0.25f * 1),
                overflow(baseChance + 0.25f * 2),
                overflow(baseChance + 0.25f * 3),
        };

        return LootTable.lootTable()
                // Silk Touch → drop bud block
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_SILK_TOUCH)
                        .add(LootItem.lootTableItem(budBlock)))

                // First Durite (capped at 100%)
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_NO_SILK_TOUCH)
                        .add(LootItem.lootTableItem(duriteItem)
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, first))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                .apply(ApplyExplosionDecay.explosionDecay())))

                // Overflow → second Durite (chance = max(0, chance-1))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_NO_SILK_TOUCH)
                        .add(LootItem.lootTableItem(duriteItem)
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, extra))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                .apply(ApplyExplosionDecay.explosionDecay())));
    }

    private static float clamp01(float f) { return f < 0f ? 0f : (f > 1f ? 1f : f); }
    private static float overflow(float f) {
        float over = f - 1f;
        return over <= 0f ? 0f : (over >= 1f ? 1f : over);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeCommonRegistryHelper.getRegistryMap().getDeferred(BuiltInRegistries.BLOCK).getEntries()
                .stream()
                .flatMap(RegistryObject::stream)
                ::iterator;
    }

    private void addPlayerStatueDrop(Block block) {
        LootTable.Builder table = LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(ExplosionCondition.survivesExplosion())
                        .add(LootItem.lootTableItem(DNLBlocks.PLAYER_STATUE.get())
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("Owner", "Owner")
                                        .copy("Owner.Name", "SkullOwner")
                                        .copy("PoseVariant", "DNL_Pose")
                                        .copy("NotchTier", "DNL_Notch")
                                        .copy("Offering", "Offering")
                                )
                        )
        );
        this.add(block, table);
    }
}
