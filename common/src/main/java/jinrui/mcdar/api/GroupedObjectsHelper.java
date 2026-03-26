package jinrui.mcdar.api;

import jinrui.mcdar.enums.*;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.loot.LootTables;

public class GroupedObjectsHelper {
    public static final Set<Identifier> ILLAGER_ARTIFACT_GENERAL_LOOT_TABLES =
            Set.of(LootTables.WOODLAND_MANSION, LootTables.PILLAGER_OUTPOST);

    public static final Set<Identifier> VILLAGER_ARTIFACT_GENERAL_LOOT_TABLES =
            Set.of(LootTables.VILLAGE_ARMORER, LootTables.VILLAGE_BUTCHER,
                    LootTables.VILLAGE_CARTOGRAPHER, LootTables.VILLAGE_FISHER,
                    LootTables.VILLAGE_FLETCHER, LootTables.VILLAGE_DESERT_HOUSE,
                    LootTables.VILLAGE_MASON, LootTables.VILLAGE_PLAINS_HOUSE,
                    LootTables.VILLAGE_SAVANNA_HOUSE, LootTables.VILLAGE_SHEPHERD,
                    LootTables.VILLAGE_SNOWY_HOUSE, LootTables.VILLAGE_TAIGA_HOUSE,
                    LootTables.VILLAGE_TANNERY, LootTables.VILLAGE_TEMPLE,
                    LootTables.VILLAGE_TOOLSMITH);
    public static final Set<Identifier> ALL_ARTIFACTS_DUNGEON_LOOT_TABLES =
            Set.of(LootTables.ABANDONED_MINESHAFT, LootTables.SIMPLE_DUNGEON, LootTables.SHIPWRECK_TREASURE);

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
