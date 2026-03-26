package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.factories.BasicTradeFactory;
import mcd_java.items.ArmorSets;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.List;

public class TradesRegistry {

    public static void registerVillagerOffers() {
        for (ArmorItem.Type armorItemType : List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS)) {
            if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(ArmorSets.CHAMPION)) {
                BasicTradeFactory.registerVillagerTrade(VillagerProfession.ARMORER, 5,
                        Items.EMERALD, 64,
                        ArmorsRegistry.armorItems.get(ArmorSets.CHAMPION).get(armorItemType), 1,
                        1, 30, 0.2F);
            }
        }
    }

    public static void registerWanderingTrades(){
        for (ArmorItem.Type armorItemType : List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS)) {
            if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(ArmorSets.ENTERTAINER)) {
                BasicTradeFactory.registerWanderingTrade(
                        Items.EMERALD, 64,
                        ArmorsRegistry.armorItems.get(ArmorSets.ENTERTAINER).get(armorItemType), 1,
                        1, 2, 0.0F);
            }
        }
        BasicTradeFactory.registerWanderingTrade(
                Items.EMERALD, 16,
                ItemsRegistry.FOX_PELT, 1,
                1, 2, 0.0F);
        for (Item item : List.of(ItemsRegistry.OCELOT_PELT, ItemsRegistry.WOLF_PELT, ItemsRegistry.GOAT_PELT)) {
            BasicTradeFactory.registerWanderingTrade(
                    Items.EMERALD, 16,
                    item, 1,
                    4, 2, 0.0F);
        }
        for (Item item : List.of(ItemsRegistry.FOX_PELT_ARCTIC, ItemsRegistry.OCELOT_PELT_BLACK, ItemsRegistry.WOLF_PELT_BLACK)) {
            BasicTradeFactory.registerWanderingTrade(
                    Items.EMERALD, 24,
                    item, 1,
                    1, 2, 0.0F);
        }
    }
}
