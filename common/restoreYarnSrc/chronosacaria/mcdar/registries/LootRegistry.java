package jinrui.mcdar.registries;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.GroupedObjectsHelper;
import jinrui.mcdar.enums.IArtifactItem;
import net.fabricmc.fabric.api.loot.v2.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.List;

public class LootRegistry {

    public static void register(){
            LootTableEvents.MODIFY.register(((resourceManager, lootManager, id, tableBuilder, source) -> {
                mcdar$artifactsGeneralLootBuilder(GroupedObjectsHelper.illagerArtifacts, id, tableBuilder);
                mcdar$artifactsGeneralLootBuilder(GroupedObjectsHelper.villagerArtifacts, id, tableBuilder);
                mcdar$artifactsDungeonLootBuilder(id, tableBuilder);
        }));
    }

    private static void mcdar$artifactsGeneralLootBuilder(List<IArtifactItem> artifactItemList, ResourceLocation id, FabricLootTableBuilder tableBuilder) {
        LootPool.Builder lootPoolBuilder = LootPool.lootPool();
        lootPoolBuilder.setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(Mcdar.CONFIG.mcdarArtifactsStatsConfig.ARTIFACT_GENERAL_SPAWN_CHANCE));
        for (IArtifactItem artifactItem : artifactItemList) {
            if (!artifactItem.mcdar$isEnabled())
                continue;
            artifactItem.mcdar$insertIntoGeneralLootPool(lootPoolBuilder, id);
        }
        tableBuilder.pool(lootPoolBuilder.build());
    }
    private static void mcdar$artifactsDungeonLootBuilder(ResourceLocation id, FabricLootTableBuilder tableBuilder) {
        LootPool.Builder lootPoolBuilder = LootPool.lootPool();
        lootPoolBuilder.setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(Mcdar.CONFIG.mcdarArtifactsStatsConfig.ARTIFACT_DUNGEON_SPAWN_CHANCE));
        for (IArtifactItem artifactItem : GroupedObjectsHelper.allArtifacts) {
            if (!artifactItem.mcdar$isEnabled())
                continue;
            artifactItem.mcdar$insertIntoDungeonLootPool(lootPoolBuilder, id);
        }
        tableBuilder.pool(lootPoolBuilder.build());
    }
}
