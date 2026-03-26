/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.CrossbowsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Tier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;

public class McdwCrossbow extends CrossbowItem implements IInnateEnchantment {

    public final Tier material;
    public final int drawSpeed;
    public final float range;
    String[] repairIngredient;
    CrossbowsID crossbowsEnum;

    public McdwCrossbow(CrossbowsID crossbowsEnum, Tier material, int drawSpeed, float range, String[] repairIngredient) {
        super(new Item.Properties().stacksTo(1).durability(100 + material.getUses())
                .rarity(RarityHelper.fromToolMaterial(material))
        );
        this.crossbowsEnum = crossbowsEnum;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.RANGED).register(entries -> entries.accept(this.getDefaultInstance()));
        this.material = material;
        this.drawSpeed = drawSpeed;
        this.range = range;
        this.repairIngredient = repairIngredient;
    }

    public float getProjectileVelocity(ItemStack stack) {
        return containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.2F;
    }

    @Override
    public int getDefaultProjectileRange() {
        return (int) range;
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }

    public int getDrawSpeed() {
        return this.drawSpeed;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return getInnateEnchantedStack(this);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        if (this.crossbowsEnum.getIsEnabled())
            return this.crossbowsEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 19);
    }
}