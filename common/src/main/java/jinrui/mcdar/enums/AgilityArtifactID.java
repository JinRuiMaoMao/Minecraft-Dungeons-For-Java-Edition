package jinrui.mcdar.enums;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.registries.ArtifactsRegistry;
import java.util.EnumMap;
import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;

public enum AgilityArtifactID implements IArtifactItem {
    BOOTS_OF_SWIFTNESS,
    DEATH_CAP_MUSHROOM,
    GHOST_CLOAK,
    LIGHT_FEATHER;

    public static EnumMap<AgilityArtifactID, Item> getItemsEnum() {
        return ArtifactsRegistry.AGILITY_ARTIFACT;
    }

    @Override
    public Boolean mcdar$isEnabled() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS.get(this).mcdar$getIsEnabled();
    }

    @Override
    public Item mcdar$getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Set<String> mcdar$getGeneralLootTables() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS.get(this).mcdar$getGeneralLootTables();
    }
    @Override
    public Set<String> mcdar$getDungeonLootTables() {
        return Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS.get(this).mcdar$getDungeonLootTables();
    }

    @Override
    public void mcdar$insertIntoGeneralLootPool(LootPool.Builder lootPoolBuilder, Identifier id) {
        if (this.mcdar$getGeneralLootTables().contains(id.toString())) {
            lootPoolBuilder
                    .add(ItemEntry.lootTableItem(this.mcdar$getItem())
                            .setWeight(Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS
                                    .get(this).mcdar$getGeneralArtifactSpawnWeight()));
        }
    }

    @Override
    public void mcdar$insertIntoDungeonLootPool(LootPool.Builder lootPoolBuilder, Identifier id) {
        if (this.mcdar$getDungeonLootTables().contains(id.toString())) {
            lootPoolBuilder
                    .add(ItemEntry.lootTableItem(this.mcdar$getItem())
                            .setWeight(Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS
                                    .get(this).mcdar$getDungeonArtifactSpawnWeight()));
        }
    }
}
