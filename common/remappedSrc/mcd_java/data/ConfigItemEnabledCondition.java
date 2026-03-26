package mcd_java.data;

import mcd_java.Mcda;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigItemEnabledCondition {

    public static void init() {
        ResourceConditions.register(new ResourceLocation(Mcda.MOD_ID, "config_enabled"), jsonObject -> {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");
            List<Boolean> booleanArrayList = new ArrayList<>();

            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonPrimitive()) {
                    try {
                        String jsonElementString = jsonElement.getAsString();
                        List<String> configClasses = Arrays.stream(jsonElementString.split("\\.")).toList();
                        if (configClasses.size() > 1) {
                            booleanArrayList.add(Mcda.CONFIG.getClass().getField(configClasses.get(0)).get(Mcda.CONFIG).getClass().getField(configClasses.get(1)).getBoolean(Mcda.CONFIG.getClass().getField(configClasses.get(0)).get(Mcda.CONFIG)));
                        } else
                            booleanArrayList.add(Mcda.CONFIG.getClass().getField(jsonElementString).getBoolean(Mcda.CONFIG));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return booleanArrayList.stream().allMatch(aBoolean -> aBoolean);
        });

        ResourceConditions.register(new ResourceLocation(Mcda.MOD_ID, "item_enabled"), jsonObject -> {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");

            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonPrimitive()) {
                    return BuiltInRegistries.ITEM.get(new ResourceLocation(jsonElement.getAsString())) != Items.AIR;
                }
            }
            return true;
        });
    }
}
