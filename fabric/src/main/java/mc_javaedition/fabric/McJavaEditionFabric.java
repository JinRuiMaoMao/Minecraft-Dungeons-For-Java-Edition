package mc_javaedition.fabric;

import mc_javaedition.McJavaEditionCommon;
import mc_javaedition.fabric.placeholder.PlaceholderItemsRegistryFabric;
import mc_javaedition.fabric.placeholder.PlaceholderCreativeTabFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class McJavaEditionFabric implements ModInitializer {
    private static void registerItemEnabledCondition(String namespace) {
        ResourceConditions.register(new Identifier(namespace, "item_enabled"), jsonObject -> {
            var values = JsonHelper.getArray(jsonObject, "values");
            for (int i = 0; i < values.size(); i++) {
                var element = values.get(i);
                if (element.isJsonPrimitive()) {
                    return Registries.ITEM.get(new Identifier(element.getAsString())) != Items.AIR;
                }
            }
            return true;
        });
    }

    @Override
    public void onInitialize() {
        // Register recipe conditions referenced by generated JSON recipes.
        registerItemEnabledCondition("mcd_java");
        registerItemEnabledCondition("mcda");
        // Real registries are incomplete during migration; placeholders guarantee /give recognition.
        PlaceholderItemsRegistryFabric.register();
        // Put placeholder items into a dedicated creative tab.
        PlaceholderCreativeTabFabric.register();
        LoveMedallionProtectionFabric.register();
        McJavaEditionCommon.init();
    }
}
