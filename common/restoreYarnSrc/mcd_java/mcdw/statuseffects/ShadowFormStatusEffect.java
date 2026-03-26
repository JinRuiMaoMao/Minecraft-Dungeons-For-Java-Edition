/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.statuseffects;

import mcd_java.mcdw.Mcdw;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class ShadowFormStatusEffect extends MobEffect {
    public ShadowFormStatusEffect(MobEffectCategory statusEffectCategory, int color, String id) {
        super(statusEffectCategory, color);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Mcdw.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier){
        super.addAttributeModifiers(entity, attributes, amplifier);
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, amplifier,false,false,false));
    }
}
