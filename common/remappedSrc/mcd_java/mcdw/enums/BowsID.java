/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwBow;
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

public enum BowsID implements IRangedWeaponID, IInnateEnchantment {
    BOW_ANCIENT_BOW(       true, Tiers.NETHERITE, 7, 14, 18f, "minecraft:netherite_scrap"),
    BOW_BONEBOW(           true, Tiers.STONE,     5, 16, 12f, "minecraft:bone"),
    BOW_BUBBLE_BOW(        true, Tiers.IRON,      5, 15, 12f, "minecraft:iron_ingot"),
    BOW_BUBBLE_BURSTER(    true, Tiers.DIAMOND,   5, 15, 13f, "minecraft:diamond"),
    BOW_BURST_GALE_BOW(    true, Tiers.DIAMOND,   6, 12, 16f, "minecraft:diamond"),
    BOW_CALL_OF_THE_VOID(  true, Tiers.NETHERITE, 6, 15, 16f, "minecraft:netherite_scrap"),
    BOW_ECHO_OF_THE_VALLEY(true, Tiers.DIAMOND,   6, 11, 16f, "minecraft:diamond"),
    BOW_ELITE_POWER_BOW(   true, Tiers.IRON,      6, 20, 15f, "minecraft:iron_ingot"),
    BOW_GREEN_MENACE(      true, Tiers.DIAMOND,   5, 17, 13f, "minecraft:diamond"),
    BOW_HAUNTED_BOW(       true, Tiers.NETHERITE, 6, 18, 16f, "minecraft:netherite_scrap"),
    BOW_HUNTERS_PROMISE(   true, Tiers.IRON,      6, 15, 16f, "minecraft:iron_ingot"),
    BOW_HUNTING_BOW(       true, Tiers.IRON,      6, 16, 15f, "minecraft:iron_ingot"),
    BOW_LOST_SOULS(        true, Tiers.NETHERITE, 6, 12, 17f, "minecraft:netherite_scrap"),
    BOW_MASTERS_BOW(       true, Tiers.IRON,      6, 17, 16f, "minecraft:iron_ingot"),
    BOW_NOCTURNAL_BOW(     true, Tiers.DIAMOND,   6, 17, 14f, "minecraft:diamond"),
    BOW_PHANTOM_BOW(       true, Tiers.DIAMOND,   6, 20, 14f, "minecraft:diamond"),
    BOW_PINK_SCOUNDREL(    true, Tiers.DIAMOND,   5, 17, 13f, "minecraft:diamond"),
    BOW_POWER_BOW(         true, Tiers.IRON,      6, 20, 14f, "minecraft:iron_ingot"),
    BOW_SABREWING(         true, Tiers.DIAMOND,   5, 10, 13f, "minecraft:diamond"),
    BOW_SHIVERING_BOW(     true, Tiers.DIAMOND,   6, 14, 15f, "minecraft:diamond"),
    BOW_SNOW_BOW(          true, Tiers.IRON,      5, 16, 13f, "minecraft:iron_ingot"),
    BOW_SOUL_BOW(          true, Tiers.IRON,      6, 14, 15f, "minecraft:iron_ingot"),
    BOW_TRICKBOW(          true, Tiers.DIAMOND,   5, 12, 12f, "minecraft:diamond"),
    BOW_TWIN_BOW(          true, Tiers.DIAMOND,   5, 12, 12f, "minecraft:diamond"),
    BOW_TWISTING_VINE_BOW( true, Tiers.IRON,      5, 15, 13f, "minecraft:iron_ingot"),
    BOW_VOID_BOW(          true, Tiers.DIAMOND,   6, 15, 16f, "minecraft:diamond"),
    BOW_WEB_BOW(           true, Tiers.DIAMOND,   5, 15, 12f, "minecraft:diamond"),
    BOW_WEEPING_VINE_BOW(  true, Tiers.IRON,      5, 15, 13f, "minecraft:iron_ingot"),
    BOW_WIND_BOW(          true, Tiers.DIAMOND,   6, 11, 15f, "minecraft:diamond"),
    BOW_WINTERS_TOUCH(     true, Tiers.DIAMOND,   6, 15, 14f, "minecraft:diamond");

    private final boolean isEnabled;
    private final Tier material;
    private final double projectileDamage;
    private final int drawSpeed;
    private final float range;
    private final String[] repairIngredient;

    BowsID(boolean isEnabled, Tier material, double projectileDamage, int drawSpeed, float range, String... repairIngredient) {
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
    public static EnumMap<BowsID, McdwBow> getItemsEnum() {
        return ItemsRegistry.BOW_ITEMS;
    }

    public static HashMap<BowsID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.BOW_SPAWN_RATES;
    }

    public static HashMap<BowsID, RangedStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.bowStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.bowStats.get(this).isEnabled;
    }

    @Override
    public McdwBow getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<BowsID, RangedStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.bowStats;
    }

    @Override
    public RangedStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public RangedStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.bowStats.get(this);
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


    public Map<Enchantment, Integer> getInnateEnchantments() {
        return switch (this) {
            case BOW_ANCIENT_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.DYNAMO);
            case BOW_BONEBOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GROWING);
            case BOW_BUBBLE_BOW, BOW_SOUL_BOW, BOW_TWISTING_VINE_BOW, BOW_WEEPING_VINE_BOW, BOW_HUNTING_BOW -> Map.of();
            case BOW_SNOW_BOW, BOW_WINTERS_TOUCH -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.FREEZING);
            case BOW_BUBBLE_BURSTER, BOW_TRICKBOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.RICOCHET);
            case BOW_ECHO_OF_THE_VALLEY -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GRAVITY, EnchantmentsID.RICOCHET);
            case BOW_BURST_GALE_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.CHARGE, EnchantmentsID.GRAVITY);
            case BOW_CALL_OF_THE_VOID -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.FUSE_SHOT, EnchantmentsID.VOID_SHOT);
            case BOW_ELITE_POWER_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(2, Enchantments.POWER_ARROWS);
            case BOW_GREEN_MENACE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.RICOCHET, EnchantmentsID.POISON_CLOUD);
            case BOW_HAUNTED_BOW, BOW_TWIN_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.BONUS_SHOT);
            case BOW_HUNTERS_PROMISE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.REPLENISH);
            case BOW_LOST_SOULS -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.MULTI_SHOT);
            case BOW_NOCTURNAL_BOW, BOW_SHIVERING_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.TEMPO_THEFT);
            case BOW_PHANTOM_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.MULTI_SHOT, Enchantments.POWER_ARROWS);
            case BOW_PINK_SCOUNDREL -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.RICOCHET, EnchantmentsID.WILD_RAGE);
            case BOW_POWER_BOW, BOW_MASTERS_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.POWER_ARROWS);
            case BOW_SABREWING -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.POWER_ARROWS, EnchantmentsID.RADIANCE);
            case BOW_VOID_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.VOID_SHOT);
            case BOW_WEB_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.COBWEB_SHOT);
            case BOW_WIND_BOW -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GRAVITY);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public McdwBow makeWeapon() {
        McdwBow mcdwBow = new McdwBow(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().drawSpeed, this.getWeaponItemStats().range, this.getWeaponItemStats().repairIngredient);
        getItemsEnum().put(this, mcdwBow);
        return mcdwBow;
    }
}