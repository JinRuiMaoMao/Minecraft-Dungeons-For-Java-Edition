package jinrui.mcdar.enums;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.registries.ArtifactsRegistry;
import java.util.EnumMap;
import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;

public enum DefensiveArtifactID implements IArtifactItem{
    ENCHANTERS_TOME,
    IRON_HIDE_AMULET,
    //SATCHEL_OF_ELIXIRS,
    //SATCHEL_OF_SNACKS,
    SOUL_HEALER,
    TOTEM_OF_REGENERATION,
    TOTEM_OF_SHIELDING,
    TOTEM_OF_SOUL_PROTECTION,
    WIND_HORN;

    public static EnumMap<DefensiveArtifactID, Item> getItemsEnum() {
        return ArtifactsRegistry.DEFENSIVE_ARTIFACT;
    }

    @Override
    public Boolean mcdar$isEnabled() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS.get(this).mcdar$getIsEnabled();
    }

    @Override
    public Item mcdar$getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Set<String> mcdar$getGeneralLootTables() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS.get(this).mcdar$getGeneralLootTables();
    }

    @Override
    public Set<String> mcdar$getDungeonLootTables() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS.get(this).mcdar$getDungeonLootTables();
    }
    @Override
    public void mcdar$insertIntoGeneralLootPool(LootPool.Builder lootPoolBuilder, Identifier id) {
        if (this.mcdar$getGeneralLootTables().contains(id.toString())) {
            lootPoolBuilder
                    .add(ItemEntry.lootTableItem(this.mcdar$getItem())
                            .setWeight(Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                                    .get(this).mcdar$getGeneralArtifactSpawnWeight()));
        }
    }

    @Override
    public void mcdar$insertIntoDungeonLootPool(LootPool.Builder lootPoolBuilder, Identifier id) {
        if (this.mcdar$getDungeonLootTables().contains(id.toString())) {
            lootPoolBuilder
                    .add(ItemEntry.lootTableItem(this.mcdar$getItem())
                            .setWeight(Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                                    .get(this).mcdar$getDungeonArtifactSpawnWeight()));
        }
    }
}
