/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.enchantments;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enchants.types.DamageBoostEnchantment;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.enums.SwordsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class SharedPainEnchantment extends Enchantment {
    public SharedPainEnchantment(Rarity rarity, EnchantmentCategory enchantmentTarget, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentTarget, equipmentSlots);
        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsEnabled()) {
            ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ENCHANTMENTS).register(entries -> {
                // For loop creates first 3 levels of enchanted books
                for (int i = 1; i <= getMaxLevel(); i++)
                    entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(this, i)));
            });
        }
    }

    @Override
    public int getMaxLevel(){
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getMaxLevel();
    }

    @Override
    public boolean isDiscoverable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsAvailableForRandomSelection();
    }

    @Override
    public boolean isTradeable() {
        return Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsEnabled()
                && Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.SHARED_PAIN).mcdw$getIsAvailableForEnchantedBookOffer();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(SwordsID.SWORD_THE_STARLESS_NIGHT.getItem());
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof DamageBoostEnchantment);
    }

    @Override
    public int getMinCost(int level) {
        return 0;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level);
    }

}