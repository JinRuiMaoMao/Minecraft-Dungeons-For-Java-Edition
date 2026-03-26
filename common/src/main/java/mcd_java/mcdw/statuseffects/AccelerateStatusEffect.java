/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.statuseffects;

import mcd_java.mcdw.Mcdw;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class AccelerateStatusEffect extends StatusEffect {
    public AccelerateStatusEffect(StatusEffectCategory statusEffectCategory, int color, String id) {
        super(statusEffectCategory, color);
        Registry.register(Registries.MOB_EFFECT, new Identifier(Mcdw.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }
}
