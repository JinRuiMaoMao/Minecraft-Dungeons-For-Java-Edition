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

public class AccelerateStatusEffect extends MobEffect {
    public AccelerateStatusEffect(MobEffectCategory statusEffectCategory, int color, String id) {
        super(statusEffectCategory, color);
        Registry.register(BuiltInRegistries.MOB_EFFECT, new ResourceLocation(Mcdw.MOD_ID, id), this);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){
        return true;
    }
}
