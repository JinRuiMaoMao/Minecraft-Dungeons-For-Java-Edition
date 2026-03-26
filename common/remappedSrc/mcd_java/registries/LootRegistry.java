package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.items.ArmorSets;
import mcd_java.items.itemhelpers.DropHelper;
import mcd_java.items.itemhelpers.ItemSettingsHelper;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static mcd_java.Mcda.CONFIG;

public class LootRegistry {

    private static final LootItemCondition.Builder WITHOUT_SILK_TOUCH;

    public static final Collection<ResourceLocation> BASTION_LOOT_TABLES = List.of(BuiltInLootTables.BASTION_BRIDGE,
            BuiltInLootTables.BASTION_HOGLIN_STABLE, BuiltInLootTables.BASTION_OTHER, BuiltInLootTables.BASTION_TREASURE);

    public static final Collection<ResourceLocation> PIGLIN_TRADING_LOOT_TABLES = Collections.singletonList(
            BuiltInLootTables.PIGLIN_BARTERING);

    public static final Collection<ResourceLocation> NETHER_FORTRESS_LOOT_TABLES = Collections.singletonList(
            BuiltInLootTables.NETHER_BRIDGE);

    public static final Collection<ResourceLocation> PILLAGER_TOWER_LOOT_TABLES = List.of(BuiltInLootTables.PILLAGER_OUTPOST,
            BuiltInLootTables.WOODLAND_MANSION);

    public static final Collection<ResourceLocation> VILLAGE_SMITH_LOOT_TABLES = List.of(BuiltInLootTables.VILLAGE_TOOLSMITH,
            BuiltInLootTables.VILLAGE_WEAPONSMITH);

    public static final Collection<ResourceLocation> SUNKEN_SHIP_LOOT_TABLES = List.of(BuiltInLootTables.SHIPWRECK_TREASURE,
            BuiltInLootTables.SHIPWRECK_SUPPLY);

    public static final Collection<ResourceLocation> MINESHAFT_LOOT_TABLES = Collections.singletonList(
            BuiltInLootTables.ABANDONED_MINESHAFT);

    public static final Collection<ResourceLocation> HERO_OF_THE_VILLAGE_LOOT_TABLES = Collections.singletonList(
            BuiltInLootTables.ARMORER_GIFT);

    public static final Collection<ResourceLocation> STRONGHOLD_LOOT_TABLES = List.of(BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING, BuiltInLootTables.STRONGHOLD_LIBRARY);

    public static final ArrayList<String> COMMON_LOOT_TABLES =
            new ArrayList<>(List.of(CONFIG.mcdaLootTablesConfig.commonLootTables.get(ItemSettingsHelper.COMMON_LOOT_TABLES)));
    public static final ArrayList<String> UNCOMMON_LOOT_TABLES =
            new ArrayList<>(List.of(CONFIG.mcdaLootTablesConfig.uncommonLootTables.get(ItemSettingsHelper.UNCOMMON_LOOT_TABLES)));
    public static final ArrayList<String> RARE_LOOT_TABLES =
            new ArrayList<>(List.of(CONFIG.mcdaLootTablesConfig.rareLootTables.get(ItemSettingsHelper.RARE_LOOT_TABLES)));
    public static final ArrayList<String> EPIC_LOOT_TABLES =
            new ArrayList<>(List.of(CONFIG.mcdaLootTablesConfig.epicLootTables.get(ItemSettingsHelper.EPIC_LOOT_TABLES)));

    public static void register() {
        LootTableEvents.MODIFY.register(((resourceManager, lootManager, id, tableBuilder, source) -> {

            if (EntityType.PHANTOM.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, ItemsRegistry.PHANTOM_BONES, DropHelper.PHANTOM_BONES, 0.5f);
                addItemLootPool(tableBuilder, ItemsRegistry.PHANTOM_SKIN, DropHelper.PHANTOM_SKIN);
            }
            if (EntityType.OCELOT.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, ItemsRegistry.OCELOT_PELT, DropHelper.OCELOT_PELT, 0.5f);
                addItemLootPool(tableBuilder, ItemsRegistry.OCELOT_PELT_BLACK, DropHelper.OCELOT_PELT_BLACK);
            }
            if (EntityType.SKELETON.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, Items.SKELETON_SKULL, DropHelper.SKELETON_SKULL);
            }
            if (EntityType.WOLF.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, ItemsRegistry.WOLF_PELT, DropHelper.WOLF_PELT, 0.5f);
                addItemLootPool(tableBuilder, ItemsRegistry.WOLF_PELT_BLACK, DropHelper.WOLF_PELT_BLACK);
            }
            if (EntityType.FOX.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, ItemsRegistry.FOX_PELT, DropHelper.FOX_PELT, 0.5f);
                addItemLootPool(tableBuilder, ItemsRegistry.FOX_PELT_ARCTIC, DropHelper.FOX_PELT_ARCTIC);
            }
            if (EntityType.EVOKER.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                LootPool.Builder lootPoolBuilder = LootPool.lootPool();
                lootPoolBuilder.setRolls(ConstantValue.exactly(CONFIG.mcdaItemDropsConfig.numberOfRollsOnLootTable.get(DropHelper.EVOCATION_ROBE)));
                lootPoolBuilder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(
                        CONFIG.mcdaItemDropsConfig.dropBaseChance.get(DropHelper.EVOCATION_ROBE),
                        CONFIG.mcdaItemDropsConfig.dropChancePerLootingLevel.get(DropHelper.EVOCATION_ROBE)));
                if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(ArmorSets.EVOCATION)) {
                    ArmorsRegistry.armorItems.get(ArmorSets.EVOCATION).values()
                            .forEach((item -> lootPoolBuilder.add(LootItem.lootTableItem(item))));
                }
                tableBuilder.pool(lootPoolBuilder.build());
            }
            if (EntityType.GOAT.getDefaultLootTable().equals(id) && source.isBuiltin()) {
                addItemLootPool(tableBuilder, ItemsRegistry.GOAT_PELT, DropHelper.GOAT_PELT, 0.5f);
            }
            if (Blocks.BLUE_ICE.getLootTable().equals(id) && source.isBuiltin()) {
                LootPool.Builder lootPoolBuilder = LootPool.lootPool();
                lootPoolBuilder.setRolls(ConstantValue.exactly(CONFIG.mcdaItemDropsConfig.numberOfRollsOnLootTable.get(DropHelper.FROST_CRYSTAL)));
                lootPoolBuilder.when(LootItemRandomChanceCondition.randomChance(CONFIG.mcdaItemDropsConfig.dropBaseChance.get(DropHelper.FROST_CRYSTAL))).when(WITHOUT_SILK_TOUCH);
                lootPoolBuilder.add(LootItem.lootTableItem(ItemsRegistry.FROST_CRYSTAL));
                tableBuilder.pool(lootPoolBuilder.build());
            }


            if (CONFIG.mcdaLootTablesConfig.enableTieredLootTables.get(ItemSettingsHelper.ENABLE_TIERED_LOOT_TABLES)) {
                LootPool.Builder lootPoolBuilder = LootPool.lootPool();
                lootPoolBuilder.setRolls(ConstantValue.exactly(1));
                lootPoolBuilder.when(LootItemRandomChanceCondition.randomChance(CONFIG.mcdaLootTablesConfig.findArmorChance));
                lootPoolBuilder.setBonusRolls(ConstantValue.exactly(CONFIG.mcdaLootTablesConfig.bonusRollsWithLuck));
                if (COMMON_LOOT_TABLES.contains(id.toString())) {
                    for (ArmorSets armorSets : List.of(ArmorSets.BATTLE, ArmorSets.BEENEST, ArmorSets.CLIMBING_GEAR, ArmorSets.EVOCATION,
                            ArmorSets.GHOSTLY, ArmorSets.HUNTER, ArmorSets.SCALE_MAIL, ArmorSets.SNOW, ArmorSets.SOUL_ROBE, ArmorSets.SPELUNKER,
                            ArmorSets.SQUID, ArmorSets.VANGUARD, ArmorSets.WOLF)) {
                        addArmorSet(lootPoolBuilder, armorSets);
                    }
                }
                if (UNCOMMON_LOOT_TABLES.contains(id.toString())) {
                    for (ArmorSets armorSets : List.of(ArmorSets.CHAMPION, ArmorSets.EMERALD, ArmorSets.ENTERTAINER, ArmorSets.GOAT, ArmorSets.GRIM,
                            ArmorSets.GUARDS, ArmorSets.MERCENARY, ArmorSets.OCELOT, ArmorSets.PHANTOM, ArmorSets.PIGLIN, ArmorSets.PLATE, ArmorSets.REINFORCED_MAIL,
                            ArmorSets.SWEET_TOOTH, ArmorSets.TELEPORTATION, ArmorSets.THIEF, ArmorSets.TURTLE)) {
                        addArmorSet(lootPoolBuilder, armorSets);
                    }
                }
                if (RARE_LOOT_TABLES.contains(id.toString())) {
                    for (ArmorSets armorSets : List.of(ArmorSets.SPLENDID, ArmorSets.BEEHIVE, ArmorSets.RUGGED_CLIMBING_GEAR, ArmorSets.DARK, ArmorSets.TROUBADOUR,
                            ArmorSets.EMBER, ArmorSets.VERDANT, ArmorSets.GHOST_KINDLER, ArmorSets.WITHER, ArmorSets.ARCHER, ArmorSets.LIVING_VINES, ArmorSets.SPROUT, ArmorSets.RENEGADE,
                            ArmorSets.GOLDEN_PIGLIN, ArmorSets.STALWART_MAIL, ArmorSets.HIGHLAND, ArmorSets.FROST, ArmorSets.SOULDANCER, ArmorSets.CAVE_CRAWLER, ArmorSets.GLOW_SQUID,
                            ArmorSets.SPIDER, ArmorSets.BLACK_WOLF, ArmorSets.FOX)) {
                        addArmorSet(lootPoolBuilder, armorSets);
                    }
                }
                if (EPIC_LOOT_TABLES.contains(id.toString())) {
                    for (ArmorSets armorSets : List.of(ArmorSets.HERO, ArmorSets.TITAN, ArmorSets.ROYAL, ArmorSets.GILDED, ArmorSets.OPULENT, ArmorSets.GOURDIAN,
                            ArmorSets.CURIOUS, ArmorSets.HUNGRY_HORROR, ArmorSets.RED_MYSTERY, ArmorSets.MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.GREEN_MYSTERY,
                            ArmorSets.PURPLE_MYSTERY, ArmorSets.SHADOW_WALKER, ArmorSets.FROST_BITE, ArmorSets.FULL_METAL, ArmorSets.CAULDRON, ArmorSets.SHULKER,
                            ArmorSets.STURDY_SHULKER, ArmorSets.UNSTABLE, ArmorSets.NIMBLE_TURTLE, ArmorSets.ARCTIC_FOX)) {
                        addArmorSet(lootPoolBuilder, armorSets);
                    }
                }
                tableBuilder.pool(lootPoolBuilder.build());
            } else {
                LootPool.Builder lootPoolBuilder = LootPool.lootPool();
                if (PIGLIN_TRADING_LOOT_TABLES.contains(id)) {
                    lootPoolBuilder.setRolls(ConstantValue.exactly(CONFIG.mcdaItemDropsConfig.numberOfRollsOnLootTable.get(DropHelper.MYSTERY_GEMSTONE)));
                    lootPoolBuilder.when(LootItemRandomChanceCondition.randomChance(CONFIG.mcdaItemDropsConfig.dropBaseChance.get(DropHelper.MYSTERY_GEMSTONE)));
                    for (Item item : List.of(ItemsRegistry.GEMSTONE_WHITE, ItemsRegistry.GEMSTONE_RED, ItemsRegistry.GEMSTONE_GREEN, ItemsRegistry.GEMSTONE_BLUE, ItemsRegistry.GEMSTONE_PURPLE)) {
                        lootPoolBuilder.add(LootItem.lootTableItem(item));
                    }
                } else {
                    lootPoolBuilder.setRolls(ConstantValue.exactly(1));
                    lootPoolBuilder.when(LootItemRandomChanceCondition.randomChance(CONFIG.mcdaLootTablesConfig.findArmorChance));
                    lootPoolBuilder.setBonusRolls(ConstantValue.exactly(CONFIG.mcdaLootTablesConfig.bonusRollsWithLuck));
                    if (BASTION_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.GREEN_MYSTERY, ArmorSets.PURPLE_MYSTERY, ArmorSets.RED_MYSTERY,
                                ArmorSets.GHOSTLY, ArmorSets.GHOST_KINDLER)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (NETHER_FORTRESS_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.GREEN_MYSTERY, ArmorSets.PURPLE_MYSTERY, ArmorSets.RED_MYSTERY,
                                ArmorSets.GRIM, ArmorSets.VANGUARD)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (PILLAGER_TOWER_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.DARK, ArmorSets.THIEF, ArmorSets.ROYAL, ArmorSets.TITAN)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (VILLAGE_SMITH_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.PLATE, ArmorSets.FULL_METAL, ArmorSets.SNOW, ArmorSets.WOLF, ArmorSets.FOX, ArmorSets.REINFORCED_MAIL,
                                ArmorSets.STALWART_MAIL)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (SUNKEN_SHIP_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.GREEN_MYSTERY, ArmorSets.PURPLE_MYSTERY, ArmorSets.RED_MYSTERY,
                                ArmorSets.SCALE_MAIL, ArmorSets.MERCENARY)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (MINESHAFT_LOOT_TABLES.contains(id)) {
                        for (ArmorSets armorSets : List.of(ArmorSets.MYSTERY, ArmorSets.BLUE_MYSTERY, ArmorSets.GREEN_MYSTERY, ArmorSets.PURPLE_MYSTERY, ArmorSets.RED_MYSTERY,
                                ArmorSets.SPELUNKER, ArmorSets.CAVE_CRAWLER)) {
                            addArmorSet(lootPoolBuilder, armorSets);
                        }
                    } else if (HERO_OF_THE_VILLAGE_LOOT_TABLES.contains(id)) {
                        addArmorSet(lootPoolBuilder, ArmorSets.HERO);
                        addArmorSet(lootPoolBuilder, ArmorSets.GILDED);
                    } else if (STRONGHOLD_LOOT_TABLES.contains(id)) {
                        addArmorSet(lootPoolBuilder, ArmorSets.TELEPORTATION);
                        addArmorSet(lootPoolBuilder, ArmorSets.UNSTABLE);
                    }
                }
                tableBuilder.pool(lootPoolBuilder.build());
            }
        }));
    }

    private static void addArmorSet(LootPool.Builder poolBuilder, ArmorSets set) {
        int weight = CONFIG.mcdaLootTablesConfig.enableTieredLootTables.get(ItemSettingsHelper.ENABLE_TIERED_LOOT_TABLES) ?
                CONFIG.mcdaLootTablesConfig.armorLootTableSpawnRates.get(set) :
                CONFIG.mcdaLootTablesConfig.armorSpawnRates.get(set);
        if (CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(set)) {
            ArmorsRegistry.armorItems.get(set).values()
                    .forEach((item -> poolBuilder.add(LootItem.lootTableItem(item).setWeight(weight))));
        }
    }

    private static void addItemLootPool(LootTable.Builder tableBuilder, Item item, DropHelper dropHelper, float lootingProviderMax) {
        LootPool.Builder lootPoolBuilder = LootPool.lootPool();
        lootPoolBuilder.setRolls(ConstantValue.exactly(CONFIG.mcdaItemDropsConfig.numberOfRollsOnLootTable.get(dropHelper)));
        lootPoolBuilder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(
                CONFIG.mcdaItemDropsConfig.dropBaseChance.get(dropHelper),
                CONFIG.mcdaItemDropsConfig.dropChancePerLootingLevel.get(dropHelper)));
        lootPoolBuilder.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, lootingProviderMax)));
        lootPoolBuilder.add(LootItem.lootTableItem(item));
        tableBuilder.pool(lootPoolBuilder.build());
    }

    private static void addItemLootPool(LootTable.Builder tableBuilder, Item item, DropHelper dropHelper) {
        LootPool.Builder lootPoolBuilder = LootPool.lootPool();
        lootPoolBuilder.setRolls(ConstantValue.exactly(CONFIG.mcdaItemDropsConfig.numberOfRollsOnLootTable.get(dropHelper)));
        lootPoolBuilder.when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(
                CONFIG.mcdaItemDropsConfig.dropBaseChance.get(dropHelper),
                CONFIG.mcdaItemDropsConfig.dropChancePerLootingLevel.get(dropHelper)));
        lootPoolBuilder.add(LootItem.lootTableItem(item));
        tableBuilder.pool(lootPoolBuilder.build());
    }

    static {
        WITHOUT_SILK_TOUCH = MatchTool.toolMatches(
                        ItemPredicate.Builder.item()
                                .hasEnchantment(
                                        new EnchantmentPredicate(
                                                Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))))
                .invert();
    }
}
