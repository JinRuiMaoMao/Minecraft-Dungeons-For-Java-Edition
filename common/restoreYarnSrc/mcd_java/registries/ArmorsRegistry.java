package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.items.ArmorSetItem;
import mcd_java.items.ArmorSets;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
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
            Registry.register(BuiltInRegistries.ITEM, Mcda.ID(armorID(set, slot)), item);
        }

        armorItems.put(set, slotMap);
    }

    public static void register() {
        for (ArmorSets set : ArmorSets.values())
            if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(set))
                registerArmor(set, set.getSlots());
    }
}
