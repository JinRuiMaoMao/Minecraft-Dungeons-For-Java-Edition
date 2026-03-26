/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.registries;

import mcd_java.mcdw.statuseffects.AccelerateStatusEffect;
import mcd_java.mcdw.statuseffects.DynamoStatusEffect;
import mcd_java.mcdw.statuseffects.PainCycleStatusEffect;
import mcd_java.mcdw.statuseffects.ShadowFormStatusEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class StatusEffectsRegistry {
    public static MobEffect ACCELERATE;
    public static MobEffect DYNAMO;
    public static MobEffect PAIN_CYCLE;
    public static MobEffect SHADOW_FORM;

    public static void register() {
        ACCELERATE = new AccelerateStatusEffect(MobEffectCategory.BENEFICIAL, 0x036edc, "accelerate");
        DYNAMO = new DynamoStatusEffect(MobEffectCategory.BENEFICIAL, 0xffbf00, "dynamo");
        PAIN_CYCLE = new PainCycleStatusEffect(MobEffectCategory.NEUTRAL, 0x640004, "pain_cycle");
        SHADOW_FORM = new ShadowFormStatusEffect(MobEffectCategory.BENEFICIAL, 0x40023e, "shadow_form");
    }
}
