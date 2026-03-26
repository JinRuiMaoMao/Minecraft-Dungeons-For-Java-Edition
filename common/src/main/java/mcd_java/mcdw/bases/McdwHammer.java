/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.HammersID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Tier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;

public class McdwHammer extends AxeItem implements IInnateEnchantment {
    String[] repairIngredient;
    HammersID hammersEnum;
    public McdwHammer(HammersID hammersEnum, Tier material, int attackDamage, float attackSpeed, String[] repairIngredient) {
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        this.hammersEnum = hammersEnum;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.repairIngredient = repairIngredient;
    }

    @Override
    public ActionResult useOn(ItemUsageContext context) {
        return InteractionResult.PASS;
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
        if (this.hammersEnum.getIsEnabled())
            return this.hammersEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 17);
    }
}