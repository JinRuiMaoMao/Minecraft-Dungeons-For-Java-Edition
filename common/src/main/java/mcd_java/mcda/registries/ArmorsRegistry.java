package mcd_java.mcda.registries;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.items.ArmorSetItem;
import mcd_java.mcda.items.ArmorSets;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import java.util.EnumMap;
import java.util.EnumSet;

public class ArmorsRegistry {
    // (set, slot) -> item
    public static final EnumMap<ArmorSets, EnumMap<ArmorItem.Type, Item>> armorItems = new EnumMap<>(ArmorSets.class);

    @SuppressWarnings("UnnecessaryDefault")
    protected static String armorID(ArmorSets set, ArmorItem.Type slot) {
        String slotID = switch (slot) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
            default -> throw new IllegalArgumentException("armor with non-armor equipment slot");
        };

        return set.getSetName() + "_" + slotID;
    }

    protected static void registerArmor(ArmorSets set, EnumSet<ArmorItem.Type> slots) {
        EnumMap<ArmorItem.Type, Item> slotMap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type slot : slots) {
            ArmorSetItem item = new ArmorSetItem(set, slot);
            slotMap.put(slot, item);
            Registry.register(Registries.ITEM, Mcda.ID(armorID(set, slot)), item);
        }

        armorItems.put(set, slotMap);
    }

    public static void register() {
        for (ArmorSets set : ArmorSets.values())
            if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(set))
                registerArmor(set, set.getSlots());
    }
}
