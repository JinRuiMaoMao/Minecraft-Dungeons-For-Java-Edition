/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.Tier;
import net.minecraft.block.DispenserBlock;

public class McdwShield extends ShieldItem {

    public final Tier material;
    String[] repairIngredient;

    public McdwShield(Tier material, String[] repairIngredient) {
        super(new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)).stacksTo(1)
                .durability(250 + material.getUses())
        );
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.SHIELDS).register(entries -> entries.accept(this.getDefaultInstance()));
        this.material = material;
        this.repairIngredient = repairIngredient;

        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Override
    public String getDescriptionId (ItemStack itemStack){
        return BlockItem.getBlockEntityData(itemStack) != null ?
                this.getDescriptionId() + '.' + getColor(itemStack).getName() : super.getDescriptionId(itemStack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }
}