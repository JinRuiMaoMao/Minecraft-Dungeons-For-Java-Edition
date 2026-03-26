
/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.enums.BowsID;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import mcd_java.mcdw.registries.ItemsRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;

import static mcd_java.mcdw.api.util.RangedAttackHelper.getVanillaBowChargeTime;

@SuppressWarnings("UnusedAssignment")
public class McdwBow extends BowItem implements IInnateEnchantment {

    public final Tier material;
    public final float drawSpeed;
    public float maxBowRange;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final ParticleOptions type;
    String[] repairIngredient;
    BowsID bowsEnum;

    public McdwBow(BowsID bowsEnum, Tier material, float drawSpeed, float maxBowRangePar, String[] repairIngredient) {
        super(new Item.Properties().stacksTo(1).durability(100 + material.getUses())
                .rarity(RarityHelper.fromToolMaterial(material))
        );
        this.bowsEnum = bowsEnum;
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

    public static float getBowArrowVelocity(ItemStack stack, int charge) {
        float bowChargeTime = getVanillaBowChargeTime(stack);
        if (bowChargeTime <= 0){
            bowChargeTime = 1;
        }

        float arrowVelocity = (float) charge / 30;
        arrowVelocity = (arrowVelocity * arrowVelocity + arrowVelocity * 2.0F) / 3.0F;
        if (arrowVelocity > 1.0F) {
            arrowVelocity = 1.0F;
        }

        return arrowVelocity;
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
        if (this.bowsEnum.getIsEnabled())
            return this.bowsEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        super.appendHoverText(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 14);
        if (stack.getItem() == ItemsRegistry.BOW_ITEMS.get(BowsID.BOW_HUNTERS_PROMISE))
            tooltip.add(Component.translatable("tooltip_ench_item.mcdw.hunters_promise_1").withStyle(ChatFormatting.GRAY));
    }
}