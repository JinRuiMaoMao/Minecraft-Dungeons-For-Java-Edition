/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwAxe;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Tier;
import net.minecraft.item.Tiers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static mcd_java.mcdw.Mcdw.CONFIG;

public enum AxesID implements IMeleeWeaponID, IInnateEnchantment {
    AXE_ANCHOR(true, Tiers.IRON, 8, -3.4f, "minecraft:iron_ingot"),
    AXE_AXE(true, Tiers.IRON, 6, -3.1f, "minecraft:iron_ingot"),
    AXE_ENCRUSTED_ANCHOR(true, Tiers.DIAMOND, 8, -3.4f, "minecraft:diamond"),
    AXE_FIREBRAND(true, Tiers.DIAMOND, 4, -2.9f, "minecraft:diamond"),
    AXE_HIGHLAND(true, Tiers.IRON, 4, -2.9f, "minecraft:iron_ingot");

    private final boolean isEnabled;
    private final Tier material;
    private final int damage;
    private final float attackSpeed;
    private final String[] repairIngredient;

    AxesID(boolean isEnabled, Tier material, int damage, float attackSpeed, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<AxesID, McdwAxe> getItemsEnum() {
        return ItemsRegistry.AXE_ITEMS;
    }

    public static HashMap<AxesID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.AXE_SPAWN_RATES;
    }

    public static HashMap<AxesID, MeleeStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.axeStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.axeStats.get(this).isEnabled;
    }

    @Override
    public McdwAxe getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<AxesID, MeleeStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.axeStats;
    }

    @Override
    public MeleeStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public MeleeStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.axeStats.get(this);
    }

    @Override
    public Tier getMaterial(){
        return material;
    }

    @Override
    public int getDamage(){
        return damage;
    }

    @Override
    public float getAttackSpeed(){
        return attackSpeed;
    }

    @Override
    public String[] getRepairIngredient() {
        return repairIngredient;
    }

    @Override
    public MeleeStats getMeleeStats() {
        return new IMeleeWeaponID.MeleeStats().meleeStats(isEnabled, CleanlinessHelper.materialToString(material), damage, attackSpeed, repairIngredient);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        return switch (this) {
            case AXE_ANCHOR -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GRAVITY);
            case AXE_AXE -> Map.of();
            case AXE_ENCRUSTED_ANCHOR -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GRAVITY, EnchantmentsID.JUNGLE_POISON);
            case AXE_FIREBRAND -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.FIRE_ASPECT);
            case AXE_HIGHLAND -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.STUNNING);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @Override
    public McdwAxe makeWeapon() {
        McdwAxe mcdwAxe = new McdwAxe(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().damage, this.getWeaponItemStats().attackSpeed, this.getWeaponItemStats().repairIngredient);

        getItemsEnum().put(this, mcdwAxe);
        return mcdwAxe;
    }
}
