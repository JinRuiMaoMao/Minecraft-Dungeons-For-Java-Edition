package jinrui.mcdar.api;

import jinrui.mcdar.enums.*;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class GroupedObjectsHelper {
    public static final Set<ResourceLocation> ILLAGER_ARTIFACT_GENERAL_LOOT_TABLES =
            Set.of(BuiltInLootTables.WOODLAND_MANSION, BuiltInLootTables.PILLAGER_OUTPOST);

    public static final Set<ResourceLocation> VILLAGER_ARTIFACT_GENERAL_LOOT_TABLES =
            Set.of(BuiltInLootTables.VILLAGE_ARMORER, BuiltInLootTables.VILLAGE_BUTCHER,
                    BuiltInLootTables.VILLAGE_CARTOGRAPHER, BuiltInLootTables.VILLAGE_FISHER,
                    BuiltInLootTables.VILLAGE_FLETCHER, BuiltInLootTables.VILLAGE_DESERT_HOUSE,
                    BuiltInLootTables.VILLAGE_MASON, BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
                    BuiltInLootTables.VILLAGE_SAVANNA_HOUSE, BuiltInLootTables.VILLAGE_SHEPHERD,
                    BuiltInLootTables.VILLAGE_SNOWY_HOUSE, BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
                    BuiltInLootTables.VILLAGE_TANNERY, BuiltInLootTables.VILLAGE_TEMPLE,
                    BuiltInLootTables.VILLAGE_TOOLSMITH);
    public static final Set<ResourceLocation> ALL_ARTIFACTS_DUNGEON_LOOT_TABLES =
            Set.of(BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.SIMPLE_DUNGEON, BuiltInLootTables.SHIPWRECK_TREASURE);

    public static final List<IArtifactItem> illagerArtifacts = List.of(
            AgilityArtifactID.DEATH_CAP_MUSHROOM,
            DamagingArtifactID.BLAST_FUNGUS,
            DamagingArtifactID.HARVESTER,
            DamagingArtifactID.LIGHTNING_ROD,
            DamagingArtifactID.POWERSHAKER,
            DefensiveArtifactID.ENCHANTERS_TOME,
            DefensiveArtifactID.SOUL_HEALER,
            DefensiveArtifactID.TOTEM_OF_REGENERATION,
            DefensiveArtifactID.TOTEM_OF_SHIELDING,
            DefensiveArtifactID.TOTEM_OF_SOUL_PROTECTION,
            QuiverArtifactID.HARPOON_QUIVER,
            QuiverArtifactID.THUNDERING_QUIVER,
            QuiverArtifactID.TORMENT_QUIVER,
            StatusInflictingArtifactID.CORRUPTED_SEEDS,
            StatusInflictingArtifactID.GONG_OF_WEAKENING,
            StatusInflictingArtifactID.LOVE_MEDALLION,
            StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS,
            StatusInflictingArtifactID.SHOCK_POWDER
    );

    public static final List<IArtifactItem> villagerArtifacts = List.of(
            AgilityArtifactID.BOOTS_OF_SWIFTNESS,
            AgilityArtifactID.GHOST_CLOAK,
            AgilityArtifactID.LIGHT_FEATHER,
            DamagingArtifactID.UPDRAFT_TOME,
            DefensiveArtifactID.IRON_HIDE_AMULET,
            DefensiveArtifactID.WIND_HORN,
            QuiverArtifactID.FLAMING_QUIVER,
            SummoningArtifactID.BUZZY_NEST,
            SummoningArtifactID.ENCHANTED_GRASS,
            SummoningArtifactID.GOLEM_KIT,
            SummoningArtifactID.TASTY_BONE,
            SummoningArtifactID.WONDERFUL_WHEAT
    );

    public static final List<IArtifactItem> allArtifacts = List.of(
            AgilityArtifactID.BOOTS_OF_SWIFTNESS,
            AgilityArtifactID.DEATH_CAP_MUSHROOM,
            AgilityArtifactID.GHOST_CLOAK,
            AgilityArtifactID.LIGHT_FEATHER,
            DamagingArtifactID.BLAST_FUNGUS,
            DamagingArtifactID.HARVESTER,
            DamagingArtifactID.LIGHTNING_ROD,
            DamagingArtifactID.POWERSHAKER,
            DamagingArtifactID.UPDRAFT_TOME,
            DefensiveArtifactID.ENCHANTERS_TOME,
            DefensiveArtifactID.IRON_HIDE_AMULET,
            DefensiveArtifactID.SOUL_HEALER,
            DefensiveArtifactID.TOTEM_OF_REGENERATION,
            DefensiveArtifactID.TOTEM_OF_SHIELDING,
            DefensiveArtifactID.TOTEM_OF_SOUL_PROTECTION,
            DefensiveArtifactID.WIND_HORN,
            QuiverArtifactID.FLAMING_QUIVER,
            QuiverArtifactID.HARPOON_QUIVER,
            QuiverArtifactID.THUNDERING_QUIVER,
            QuiverArtifactID.TORMENT_QUIVER,
            StatusInflictingArtifactID.CORRUPTED_SEEDS,
            StatusInflictingArtifactID.GONG_OF_WEAKENING,
            StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS,
            StatusInflictingArtifactID.SHOCK_POWDER,
            SummoningArtifactID.BUZZY_NEST,
            SummoningArtifactID.ENCHANTED_GRASS,
            SummoningArtifactID.GOLEM_KIT,
            SummoningArtifactID.TASTY_BONE,
            SummoningArtifactID.WONDERFUL_WHEAT);
}
