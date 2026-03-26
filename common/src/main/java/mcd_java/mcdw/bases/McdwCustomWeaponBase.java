/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.bases;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.Attribute;
import net.minecraft.entity.attribute.AttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Tier;
import net.minecraft.item.TieredItem;
import net.minecraft.item.Vanishable;

public class McdwCustomWeaponBase extends TieredItem implements Vanishable {
    private final float attackDamage;
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;

    public McdwCustomWeaponBase(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, settings);
        this.attackDamage = attackDamage + material.getAttackDamageBonus();
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    
}