/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.enchantments;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.bases.McdwCustomWeaponBase;
import mcd_java.mcdw.enchants.types.DamageBoostEnchantment;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.enums.SettingsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnchantmentLevelEntry;

public class DynamoEnchantment extends DamageBoostEnchantment {
    public DynamoEnchantment(Rarity rarity, EnchantmentTarget enchantmentTarget, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentTarget, equipmentSlots);
        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsEnabled()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ENCHANTMENTS).register(entries -> {
                // For loop creates first 3 levels of enchanted books
                for (int i = 1; i <= getMaxLevel(); i++)
                    entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentLevelEntry(this, i)));
            });
        }
    }

    @Override
    public int getMaxLevel(){
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getMaxLevel();
    }

    @Override
    protected boolean checkCompatibility (Enchantment other) {
        return Mcdw.CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.ENABLE_OP_ENCHANTMENT_MIXING)
                || !(other instanceof DamageBoostEnchantment);
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsAvailableForRandomSelection();
    }

    @Override
    public boolean isTradeable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.DYNAMO).mcdw$getIsAvailableForEnchantedBookOffer();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof McdwCustomWeaponBase
                || stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

}
