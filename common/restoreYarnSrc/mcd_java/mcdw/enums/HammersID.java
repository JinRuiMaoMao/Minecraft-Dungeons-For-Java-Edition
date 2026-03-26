/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwHammer;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
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

public enum HammersID implements IMeleeWeaponID, IInnateEnchantment {
    HAMMER_BONECLUB(true, Tiers.IRON,7, -3.2f, "minecraft:bone_block"),
    HAMMER_BONE_CUDGEL(true, Tiers.NETHERITE,7, -3.2f, "minecraft:netherite_scrap"),
    HAMMER_FLAIL(true, Tiers.IRON,5, -2.8f, "minecraft:iron_ingot"),
    HAMMER_GRAVITY(true, Tiers.DIAMOND,6, -3.2f, "minecraft:diamond"),
    HAMMER_GREAT_HAMMER(true, Tiers.IRON,6, -3.2f, "minecraft:iron_ingot"),
    HAMMER_MACE(true, Tiers.IRON,5, -2.8f, "minecraft:iron_ingot"),
    HAMMER_STORMLANDER(true, Tiers.DIAMOND,7, -3.2f, "minecraft:diamond"),
    HAMMER_SUNS_GRACE(true, Tiers.DIAMOND,4, -2.8f, "minecraft:diamond");

    private final boolean isEnabled;
    private final Tier material;
    private final int damage;
    private final float attackSpeed;
    private final String[] repairIngredient;

    HammersID(boolean isEnabled, Tier material, int damage, float attackSpeed, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<HammersID, McdwHammer> getItemsEnum() {
        return ItemsRegistry.HAMMER_ITEMS;
    }

    public static HashMap<HammersID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.HAMMER_SPAWN_RATES;
    }

    public static HashMap<HammersID, MeleeStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.hammerStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.hammerStats.get(this).isEnabled;
    }

    @Override
    public McdwHammer getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<HammersID, MeleeStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.hammerStats;
    }

    @Override
    public MeleeStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public MeleeStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.hammerStats.get(this);
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
            case HAMMER_BONECLUB -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.KNOCKBACK);
            case HAMMER_GREAT_HAMMER, HAMMER_MACE -> Map.of();
            case HAMMER_FLAIL -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.CHAINS);
            case HAMMER_BONE_CUDGEL -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, Enchantments.KNOCKBACK, EnchantmentsID.ILLAGERS_BANE);
            case HAMMER_GRAVITY -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.GRAVITY);
            case HAMMER_STORMLANDER -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.THUNDERING);
            case HAMMER_SUNS_GRACE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.RADIANCE);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @Override
    public McdwHammer makeWeapon() {
        McdwHammer mcdwHammer = new McdwHammer(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().damage, this.getWeaponItemStats().attackSpeed, this.getWeaponItemStats().repairIngredient);

        getItemsEnum().put(this, mcdwHammer);
        return mcdwHammer;
    }
}
