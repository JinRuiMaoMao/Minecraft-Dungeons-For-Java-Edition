/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwLongbow;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static mcd_java.mcdw.Mcdw.CONFIG;

public enum LongbowsID implements IRangedWeaponID, IInnateEnchantment {
    BOW_GUARDIAN_BOW(true, Tiers.DIAMOND, 8, 30, 19f, "minecraft:diamond"),
    BOW_LONGBOW(     true, Tiers.IRON,    7, 25, 17f, "minecraft:planks"),
    BOW_RED_SNAKE(   true, Tiers.DIAMOND, 7, 30, 18f, "minecraft:diamond");

    public final boolean isEnabled;
    public final Tier material;
    public final double projectileDamage;
    public final int drawSpeed;
    public final float range;
    private final String[] repairIngredient;

    LongbowsID(boolean isEnabled, Tier material, double projectileDamage, int drawSpeed, float range, String... repairIngredient) {
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
    public static EnumMap<LongbowsID, McdwLongbow> getItemsEnum() {
        return ItemsRegistry.LONGBOW_ITEMS;
    }

    public static HashMap<LongbowsID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.LONGBOW_SPAWN_RATES;
    }

    public static HashMap<LongbowsID, RangedStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.longbowStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.longbowStats.get(this).isEnabled;
    }

    @Override
    public McdwLongbow getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<LongbowsID, RangedStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.longbowStats;
    }

    @Override
    public RangedStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public RangedStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.longbowStats.get(this);
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
            case BOW_LONGBOW -> Map.of();
            case BOW_GUARDIAN_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(2, Enchantments.POWER_ARROWS);
            case BOW_RED_SNAKE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.POWER_ARROWS, EnchantmentsID.FUSE_SHOT);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public McdwLongbow makeWeapon() {
        McdwLongbow mcdwLongbow = new McdwLongbow(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().drawSpeed, this.getWeaponItemStats().range, this.getWeaponItemStats().repairIngredient);
        getItemsEnum().put(this, mcdwLongbow);
        return mcdwLongbow;
    }
}