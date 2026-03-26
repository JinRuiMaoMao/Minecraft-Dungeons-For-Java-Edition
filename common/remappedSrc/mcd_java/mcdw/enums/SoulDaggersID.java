/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.bases.McdwSoulDagger;
import mcd_java.mcdw.configs.McdwNewStatsConfig;
import mcd_java.mcdw.registries.ItemsRegistry;
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

public enum SoulDaggersID implements IMeleeWeaponID, IInnateEnchantment {
    SOUL_DAGGER_ETERNAL_KNIFE(true, Tiers.NETHERITE,4, -2.8f, "minecraft:netherite_scrap"),
    SOUL_DAGGER_SOUL_KNIFE(true, Tiers.IRON,4, -2.8f, "minecraft:iron_ingot"),
    SOUL_DAGGER_TRUTHSEEKER(true, Tiers.NETHERITE,3, -2.8f, "minecraft:netherite_scrap");

    private final boolean isEnabled;
    private final Tier material;
    private final int damage;
    private final float attackSpeed;
    private final String[] repairIngredient;


    @SuppressWarnings("SameParameterValue")
    SoulDaggersID(boolean isEnabled, Tier material, int damage, float attackSpeed, String... repairIngredient) {
        this.isEnabled = isEnabled;
        this.material = material;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.repairIngredient = repairIngredient;
    }

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<SoulDaggersID, McdwSoulDagger> getItemsEnum() {
        return ItemsRegistry.SOUL_DAGGER_ITEMS;
    }

    public static HashMap<SoulDaggersID, Integer> getSpawnRates() {
        return Mcdw.CONFIG.mcdwNewlootConfig.SOUL_DAGGER_SPAWN_RATES;
    }

    public static HashMap<SoulDaggersID, MeleeStats> getWeaponStats() {
        return CONFIG.mcdwNewStatsConfig.soulDaggerStats;
    }

    @Override
    public boolean getIsEnabled(){
        return CONFIG.mcdwNewStatsConfig.soulDaggerStats.get(this).isEnabled;
    }

    @Override
    public McdwSoulDagger getItem() {
        return getItemsEnum().get(this);
    }

    @Override
    public Integer getItemSpawnRate() {
        return getSpawnRates().get(this);
    }

    @Override
    public HashMap<SoulDaggersID, MeleeStats> getWeaponStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.soulDaggerStats;
    }

    @Override
    public MeleeStats getWeaponItemStats() {
        return getWeaponStats().get(this);
    }

    @Override
    public MeleeStats getWeaponItemStats(McdwNewStatsConfig mcdwNewStatsConfig) {
        return mcdwNewStatsConfig.soulDaggerStats.get(this);
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
            case SOUL_DAGGER_ETERNAL_KNIFE -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.SOUL_SIPHON);
            case SOUL_DAGGER_SOUL_KNIFE -> Map.of();
            case SOUL_DAGGER_TRUTHSEEKER -> CleanlinessHelper.mcdw$checkInnateEnchantmentEnabled(1, EnchantmentsID.COMMITTED);
        };
    }

    @Override
    public @NotNull ItemStack getInnateEnchantedStack(Item item) {
        return item.getDefaultInstance();
    }

    @Override
    public McdwSoulDagger makeWeapon() {
        McdwSoulDagger mcdwSoulDagger = new McdwSoulDagger(this, CleanlinessHelper.stringToMaterial(this.getWeaponItemStats().material),
                this.getWeaponItemStats().damage, this.getWeaponItemStats().attackSpeed, this.getWeaponItemStats().repairIngredient);

        getItemsEnum().put(this, mcdwSoulDagger);
        return mcdwSoulDagger;
    }
}