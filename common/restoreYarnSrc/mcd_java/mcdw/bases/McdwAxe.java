/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.AxesID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;

public class McdwAxe extends AxeItem implements IInnateEnchantment {
    String[] repairIngredient;
    AxesID axeEnum;
    public McdwAxe(AxesID axeEnum, Tier material, float attackDamage, float attackSpeed, String[] repairIngredient){
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        this.axeEnum = axeEnum;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return getInnateEnchantedStack(this);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        if(this.axeEnum.getIsEnabled())
            return this.axeEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        super.appendHoverText(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 14);
    }
}