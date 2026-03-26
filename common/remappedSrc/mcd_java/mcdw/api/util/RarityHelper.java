/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class RarityHelper {
    public static Rarity fromToolMaterial(Tier material){
        return
                material == Tiers.NETHERITE ? Rarity.EPIC :
                material == Tiers.DIAMOND ? Rarity.RARE :
                material == Tiers.GOLD ? Rarity.UNCOMMON :
                material == Tiers.IRON ? Rarity.UNCOMMON : Rarity.COMMON;
    }
}
