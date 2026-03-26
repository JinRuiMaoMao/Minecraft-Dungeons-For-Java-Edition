/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwGlaive;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Tier;
import net.minecraft.item.Tiers;
import net.minecraft.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static mcd_java.mcdw.Mcdw.CONFIG;

public enum GlaivesID implements IMeleeWeaponID, IInnateEnchantment {
    GLAIVE_CACKLING_BROOM(true, Tiers.IRON,5, -3f, "minecraft:iron_ingot"),
    GLAIVE_GLAIVE(true, Tiers.IRON,5, -3f, "minecraft:iron_ingot"),
    GLAIVE_GRAVE_BANE(true, Tiers.IRON,6, -3f, "minecraft:iron_ingot"),
    GLAIVE_VENOM_GLAIVE(true, Tiers.IRON,6, -3f, "minecraft:iron_ingot");

    private final boolean isEnabled;
    private final Tier material;
    private final int damage;
    private final float attackSpeed;
    private final String[] repairIngredient;

    GlaivesID(boolean isEnabled, Tier material, int damage, float attackSpeed, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<GlaivesID, McdwGlaive> getItemsEnum() {
        return ItemsRegistry.GLAIVE_ITEMS;
    }

    public static HashMap<GlaivesID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.GLAIVE_SPAWN_RATES;
    }

    public static HashMap<GlaivesID, MeleeStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.glaiveStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.glaiveStats.get(this).isEnabled;
    }

    @Override
    public McdwGlaive getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<GlaivesID, MeleeStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.glaiveStats;
    }

    @Override
    public MeleeStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public MeleeStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.glaiveStats.get(this);
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
            case GLAIVE_CACKLING_BROOM, GLAIVE_GRAVE_BANE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.SMITING);
            case GLAIVE_GLAIVE -> Map.of();
            case GLAIVE_VENOM_GLAIVE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.POISON_CLOUD);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @Override
    public McdwGlaive makeWeapon() {
        McdwGlaive mcdwGlaive = new McdwGlaive(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().damage, this.getWeaponItemStats().attackSpeed, this.getWeaponItemStats().repairIngredient);

        getItemsEnum().put(this, mcdwGlaive);
        return mcdwGlaive;
    }
}