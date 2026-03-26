/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.enchantments;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.bases.McdwCustomWeaponBase;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.enums.SettingsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MobType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnchantmentLevelEntry;

public class IllagersBaneEnchantment extends Enchantment {
    public IllagersBaneEnchantment(Rarity rarity, EnchantmentTarget enchantmentTarget, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentTarget, equipmentSlots);
        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getIsEnabled()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ENCHANTMENTS).register(entries -> {
                // For loop creates first 3 levels of enchanted books
                for (int i = 1; i <= getMaxLevel(); i++)
                    entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentLevelEntry(this, i)));
            });
        }
    }

    @Override
    public int getMaxLevel(){
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getMaxLevel();
    }

    @Override
    public float getDamageBonus(int level, MobType group){
        if (group == MobType.ILLAGER){
            return 1 + (0.2f * level);
        } else {
            return 0f;
        }
    }

    @Override
    protected boolean checkCompatibility (Enchantment other){
        return Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.ENABLE_OP_ENCHANTMENT_MIXING)
                || !(other instanceof DamageEnchantment);
    }

    @Override
    public boolean isDiscoverable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getIsAvailableForRandomSelection();
    }

    @Override
    public boolean isTradeable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ILLAGERS_BANE).mcdw$getIsAvailableForEnchantedBookOffer();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof McdwCustomWeaponBase;
    } //TODO Explore Bows and Crossbows

}
