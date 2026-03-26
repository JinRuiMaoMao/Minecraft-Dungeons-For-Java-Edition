/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.configs.McdwNewStatsConfig;
import net.minecraft.item.Tier;
import java.util.HashMap;

public interface IShieldID extends IMcdwWeaponID {

    static IShieldID[] values() {
        return IMcdwWeaponID.shieldValues();
    }


    HashMap<ShieldsID, ShieldStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig);

    ShieldStats getWeaponItemStats();

    ShieldStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig);

    boolean getIsEnabled();

    Tier getMaterial();

    String[] getRepairIngredient();

    ShieldStats getShieldStats();

    class ShieldStats {
        boolean isEnabled = true;
        String material = "diamond";
        String[] repairIngredient = new String[]{};

        public ShieldStats shieldStats(boolean isEnabled, String material, String[] repairIngredient) {
            this.isEnabled = isEnabled;
            this.material = material;
            this.repairIngredient = repairIngredient;
            return this;
        }
    }
}