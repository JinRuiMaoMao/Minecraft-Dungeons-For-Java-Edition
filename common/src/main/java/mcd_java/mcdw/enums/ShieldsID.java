/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwShield;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.item.Tier;
import net.minecraft.item.Tiers;
import java.util.EnumMap;
import java.util.HashMap;

import static mcd_java.mcdw.Mcdw.CONFIG;

public enum ShieldsID implements IShieldID {
    SHIELD_ROYAL_GUARD(true, Tiers.DIAMOND, "minecraft:iron_ingot", "minecraft:gold_ingot"),
    SHIELD_TOWER_GUARD(true, Tiers.DIAMOND, "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:copper_ingot"),
    SHIELD_VANGUARD(true, Tiers.DIAMOND, "minecraft:planks", "minecraft:iron_ingot");

    private final boolean isEnabled;
    private final Tier material;
    private final String[] repairIngredient;

    @SuppressWarnings("SameParameterValue")
    ShieldsID(boolean isEnabled, Tier material, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<ShieldsID, McdwShield> getItemsEnum() {
        return ItemsRegistry.SHIELD_ITEMS;
    }

    public static HashMap<ShieldsID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.SHIELD_SPAWN_RATES;
    }

    public static HashMap<ShieldsID, ShieldStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.shieldStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.shieldStats.get(this).isEnabled;    }

    @Override
    public McdwShield getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<ShieldsID, ShieldStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.shieldStats;
    }

    public ShieldStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public ShieldStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return getWeaponStats(mcdwNewStatsConfig).get(this);
    }

    @Override
    public Tier getMaterial() {
        return material;
    }

    @Override
    public String[] getRepairIngredient() {
        return repairIngredient;
    }

    @Override
    public ShieldStats getShieldStats() {
        return new IShieldID.ShieldStats().shieldStats(isEnabled, CleanlinessHelper.materialToString(material), repairIngredient);
    }

    @Override
    public McdwShield makeWeapon() {
        McdwShield mcdwShield = new McdwShield(CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material), this.getWeaponItemStats().repairIngredient);

        getItemsEnum().put(this, mcdwShield);
        return mcdwShield;
    }
}
