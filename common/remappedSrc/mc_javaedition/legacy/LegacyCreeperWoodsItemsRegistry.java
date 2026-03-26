package mc_javaedition.legacy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public final class LegacyCreeperWoodsItemsRegistry {
    private static final String LEGACY_NAMESPACE = "mcd_javaedition";

    private static final String[] LEGACY_ITEM_IDS = new String[] {
            "apple", "archerarmorhelmet", "bootsofswiftness", "bread", "deathcapmushroom", "firewor",
            "foxarmorbody", "foxarmorboots", "foxarmorhelmet", "healthpot", "highlandarmorbody",
            "highlandarmorhelmet", "hunterarmorbody", "nothing", "porkchop", "potionhealth", "potionspeed",
            "scalemailarmorbody", "shadowbrew", "strengthpotion", "tnt", "wolfsarmorbody", "wolfsarmorboots",
            "wolfsarmorhelmet"
    };

    private static final FoodProperties BASIC_FOOD = new FoodProperties.Builder().nutrition(4).saturationMod(0.3f).build();

    private LegacyCreeperWoodsItemsRegistry() {}

    public static void register() {
        for (String id : LEGACY_ITEM_IDS) {
            registerItem(id);
        }
    }

    private static void registerItem(String path) {
        ResourceLocation id = new ResourceLocation(LEGACY_NAMESPACE, path);
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            return;
        }

        Item.Properties settings = new Item.Properties();
        if ("apple".equals(path) || "bread".equals(path) || "porkchop".equals(path)) {
            settings.food(BASIC_FOOD);
        } else if (path.contains("potion") || "healthpot".equals(path) || "shadowbrew".equals(path)) {
            settings.stacksTo(1);
        } else if (path.contains("armor")) {
            settings.stacksTo(1);
        }

        Registry.register(BuiltInRegistries.ITEM, id, new Item(settings));
    }
}
