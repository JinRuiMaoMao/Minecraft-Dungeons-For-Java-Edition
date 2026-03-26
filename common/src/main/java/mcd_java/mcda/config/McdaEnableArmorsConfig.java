package mcd_java.mcda.config;

import mcd_java.mcda.items.ArmorSets;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

import java.util.HashMap;
@Config(name = "mcda_enable_armors")
public class McdaEnableArmorsConfig implements ConfigData {
    public final HashMap<ArmorSets, Boolean> ARMORS_SETS_ENABLED = new HashMap<>();

    public McdaEnableArmorsConfig() {
        for (ArmorSets armorSets : ArmorSets.values()) {
            ARMORS_SETS_ENABLED.put(armorSets, true);
        }
    }
}
