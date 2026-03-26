
/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.ShortbowsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.Text;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Tier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;

public class McdwShortbow extends BowItem implements IInnateEnchantment {

    public final Tier material;
    public final float drawSpeed;
    public float maxBowRange;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ParticleEffect type;
    String[] repairIngredient;
    ShortbowsID shortbowsEnum;

    public McdwShortbow(ShortbowsID shortbowsEnum, Tier material, float drawSpeed, float maxBowRangePar, String[] repairIngredient) {
        super(new Item.Properties().stacksTo(1).durability(material.getUses())
                .rarity(RarityHelper.fromToolMaterial(material))
        );
        this.shortbowsEnum = shortbowsEnum;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.RANGED).register(entries -> entries.accept(this.getDefaultInstance()));
        this.material = material;
        this.drawSpeed = drawSpeed;
        this.repairIngredient = repairIngredient;
        this.maxBowRange = maxBowRangePar;
        type = null;
    }

    public float getDrawSpeed() {
        return Math.max(0, drawSpeed);
    }

    @Override
    public int getDefaultProjectileRange() {
        return (int) maxBowRange;
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
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
        if (this.shortbowsEnum.getIsEnabled())
            return this.shortbowsEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 14);
    }
}