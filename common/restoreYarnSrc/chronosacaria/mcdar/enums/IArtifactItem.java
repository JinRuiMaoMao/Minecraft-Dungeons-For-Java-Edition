package jinrui.mcdar.enums;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;

public interface IArtifactItem {

    Boolean mcdar$isEnabled();

    Item mcdar$getItem();

    Set<String> mcdar$getGeneralLootTables();
    Set<String> mcdar$getDungeonLootTables();

    void mcdar$insertIntoGeneralLootPool(LootPool.Builder lootPoolBuilder, ResourceLocation id);
    void mcdar$insertIntoDungeonLootPool(LootPool.Builder lootPoolBuilder, ResourceLocation id);
}
