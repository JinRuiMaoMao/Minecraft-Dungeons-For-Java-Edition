/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.api.interfaces.IInnateEnchantment;
import mcd_java.mcdw.api.util.CleanlinessHelper;
import mcd_java.mcdw.api.util.RarityHelper;
import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.enums.GlaivesID;
import mcd_java.mcdw.registries.EntityAttributesRegistry;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.Attribute;
import net.minecraft.entity.attribute.AttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.Tier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;

public class McdwGlaive extends SwordItem implements IInnateEnchantment {
    String[] repairIngredient;
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    private final Tier material;
    private final float attackDamage;
    GlaivesID glaivesEnum;

    public McdwGlaive(GlaivesID glaivesEnum, Tier material, int attackDamage, float attackSpeed, String[] repairIngredient) {
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        this.glaivesEnum = glaivesEnum;
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.material = material;
        this.attackDamage = attackDamage + material.getAttackDamageBonus();
        this.repairIngredient = repairIngredient;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Tool modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        if (FabricLoader.getInstance().isModLoaded("reach-entity-attributes") && CompatibilityFlags.isReachExtensionEnabled) {
            builder.put(ReachEntityEntityAttributes.REACH, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfGlaives,
                    AttributeModifier.Operation.ADDITION));
            builder.put(ReachEntityEntityAttributes.ATTACK_RANGE, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfGlaives,
                    AttributeModifier.Operation.ADDITION));
        } else if (CompatibilityFlags.isReachExtensionEnabled) {
            builder.put(EntityAttributesRegistry.ATTACK_RANGE, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfGlaives,
                    AttributeModifier.Operation.ADDITION));
        }
        this.attributeModifiers = builder.build();
    }

    @Override
    public Tier getTier() {
        return this.material;
    }

    @Override
    public int getEnchantmentValue(){
        return this.material.getEnchantmentValue();
    }

    @Override
    public float getDamage(){
        return this.attackDamage;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot){
        return equipmentSlot == EquipmentSlot.MAINHAND ? attributeModifiers :
                super.getDefaultAttributeModifiers(equipmentSlot);
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
        if (this.glaivesEnum.getIsEnabled())
            return this.glaivesEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        super.appendTooltip(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 17);
    }
}