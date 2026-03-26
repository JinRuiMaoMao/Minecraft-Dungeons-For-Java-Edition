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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;

public class McdwPick extends PickaxeItem {
    String[] repairIngredient;
    public McdwPick(Tier material, int attackDamage, float attackSpeed, String[] repairIngredient) {
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        super.appendHoverText(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 15);
    }
}