package jinrui.mcdar.enums;

import java.util.Set;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;

public interface IArtifactItem {

    Boolean mcdar$isEnabled();

    Item mcdar$getItem();

    Set<String> mcdar$getGeneralLootTables();
    Set<String> mcdar$getDungeonLootTables();

    void mcdar$insertIntoGeneralLootPool(LootPool.Builder lootPoolBuilder, Identifier id);
    void mcdar$insertIntoDungeonLootPool(LootPool.Builder lootPoolBuilder, Identifier id);
}
