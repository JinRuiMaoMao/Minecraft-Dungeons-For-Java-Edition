/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.mixin.mcdw;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void mcdw$getPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        var currentEntries = cir.getReturnValue();

        // 1. REMOVING ENCHANT ENTRIES ADDED INCORRECTLY

        var toRemove = new ArrayList<EnchantmentInstance>();
        for (var entry: currentEntries) {
            if (!entry.enchantment.canEnchant(stack)) {
                toRemove.add(entry);
            }
        }
        currentEntries.removeAll(toRemove);

        // 2. ADDING ENCHANT ENTRIES LEFT OUT INITIALLY

        // This logic is mostly copied from EnchantmentHelper.getPossibleEntries
        boolean isBook = stack.is(Items.BOOK);
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            // Don't check entries already added
            boolean alreadyAdded = currentEntries.stream().anyMatch(entry -> entry.enchantment.equals(enchantment));
            if (alreadyAdded) { continue; }

            if (enchantment.isTreasureOnly()
                    && !treasureAllowed
                    || !enchantment.isDiscoverable()
                    || !enchantment.canEnchant(stack) // Custom logic, replacing `!enchantment.type.isAcceptableItem(item)`
                    && !isBook) continue;
            for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                if (power < enchantment.getMinCost(i) || power > enchantment.getMaxCost(i)) continue;
                currentEntries.add(new EnchantmentInstance(enchantment, i));
            }
        }
    }
}
