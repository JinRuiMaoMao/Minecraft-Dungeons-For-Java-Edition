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
import mcd_java.mcdw.enums.StavesID;
import mcd_java.mcdw.registries.EntityAttributesRegistry;
import mcd_java.mcdw.registries.ItemGroupRegistry;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.item.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import java.util.Map;

public class McdwStaff extends AxeItem implements IInnateEnchantment {

    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    private final Tier material;
    private final float attackDamage;
    String[] repairIngredient;
    StavesID stavesEnum;

    public McdwStaff(StavesID stavesEnum, Tier material, int attackDamage, float attackSpeed, String[] repairIngredient) {
        super(material, attackDamage, attackSpeed,
                new Item.Properties().rarity(RarityHelper.fromToolMaterial(material)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.MELEE).register(entries -> entries.accept(this.getDefaultInstance()));
        this.stavesEnum = stavesEnum;
        this.material = material;
        this.attackDamage = attackDamage + material.getAttackDamageBonus();
        this.repairIngredient = repairIngredient;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Tool modifier", this.attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Tool modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        if (FabricLoader.getInstance().isModLoaded("reach-entity-attributes") && CompatibilityFlags.isReachExtensionEnabled) {
            builder.put(ReachEntityAttributes.REACH, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfStaves,
                    AttributeModifier.Operation.ADDITION));
            builder.put(ReachEntityAttributes.ATTACK_RANGE, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfStaves,
                    AttributeModifier.Operation.ADDITION));
        } else if (CompatibilityFlags.isReachExtensionEnabled) {
            builder.put(EntityAttributesRegistry.ATTACK_RANGE, new AttributeModifier("Attack range",
                    Mcdw.CONFIG.mcdwNewStatsConfig.extraAttackReachOfStaves,
                    AttributeModifier.Operation.ADDITION));
        }
        this.attributeModifiers = builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
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
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return CleanlinessHelper.canRepairCheck(repairIngredient, ingredient);
    }

    @Override
    public float getAttackDamage(){
        return this.attackDamage;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner){
        return !miner.isCreative();
    }

    // Damage to tool upon usage
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }
    // Double Damage to tool upon improper usage
    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner){
        if (state.getDestroySpeed(world, pos) != 0.0F){
            stack.hurtAndBreak(2, miner, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot){
        return equipmentSlot == EquipmentSlot.MAINHAND ? attributeModifiers :
                super.getDefaultAttributeModifiers(equipmentSlot);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return getInnateEnchantedStack(this);
    }

    @Override
    public Map<Enchantment, Integer> getInnateEnchantments() {
        if (this.stavesEnum.getIsEnabled())
            return this.stavesEnum.getInnateEnchantments();
        return Map.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        super.appendHoverText(stack, world, tooltip, tooltipContext);
        CleanlinessHelper.mcdw$tooltipHelper(stack, tooltip, 16);
    }
}