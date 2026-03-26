/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwShortbow;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static mcd_java.mcdw.Mcdw.CONFIG;

public enum ShortbowsID implements IRangedWeaponID, IInnateEnchantment {
    BOW_LOVE_SPELL_BOW(     true, Tiers.IRON, 3, 9, 8f, "minecraft:iron_ingot"),
    BOW_MECHANICAL_SHORTBOW(true, Tiers.IRON, 4, 9, 9f, "minecraft:iron_ingot"),
    BOW_PURPLE_STORM(       true, Tiers.IRON, 3, 9, 8f, "minecraft:iron_ingot"),
    BOW_SHORTBOW(           true, Tiers.IRON, 3, 9, 8f, "minecraft:planks");

    private final boolean isEnabled;
    private final Tier material;
    private final double projectileDamage;
    private final int drawSpeed;
    private final float range;
    private final String[] repairIngredient;

    @SuppressWarnings("SameParameterValue")
    ShortbowsID(boolean isEnabled, Tier material, double projectileDamage, int drawSpeed, float range, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            this.projectileDamage = projectileDamage;
        } else {
            this.projectileDamage = 0;
        }
        this.drawSpeed = drawSpeed;
        this.range = range;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<ShortbowsID, McdwShortbow> getItemsEnum() {
        return ItemsRegistry.SHORTBOW_ITEMS;
    }

    public static HashMap<ShortbowsID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.SHORTBOW_SPAWN_RATES;
    }

    public static HashMap<ShortbowsID, RangedStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.shortbowStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.shortbowStats.get(this).isEnabled;    }

    @Override
    public McdwShortbow getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<ShortbowsID, RangedStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.shortbowStats;
    }

    @Override
    public RangedStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public RangedStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.shortbowStats.get(this);
    }

    @Override
    public Tier getMaterial() {
        return material;
    }

    @Override
    public double getProjectileDamage() {
        if (FabricLoader.getInstance().isModLoaded("ranged_weapon_api")) {
            return projectileDamage;
        } else {
            return 0;
        }
    }

    @Override
    public int getDrawSpeed() {
        return drawSpeed;
    }

    @Override
    public float getRange() {
        return range;
    }

    @Override
    public String[] getRepairIngredient() {
        return repairIngredient;
    }

    @Override
    public RangedStats getRangedStats() {
        return new IRangedWeaponID.RangedStats().rangedStats(isEnabled, CleanlinessHelper.materialToString(material), projectileDamage, drawSpeed, range, repairIngredient);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        return switch (this) {
            case BOW_LOVE_SPELL_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.WILD_RAGE);
            case BOW_MECHANICAL_SHORTBOW, BOW_PURPLE_STORM -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.ACCELERATE);
            case BOW_SHORTBOW -> Map.of();
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public McdwShortbow makeWeapon() {
        McdwShortbow mcdwShortbow = new McdwShortbow(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().drawSpeed, this.getWeaponItemStats().range, this.getWeaponItemStats().repairIngredient);
        getItemsEnum().put(this, mcdwShortbow);
        return mcdwShortbow;
    }
}