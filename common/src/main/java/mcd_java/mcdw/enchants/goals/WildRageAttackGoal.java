/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;

public class WildRageAttackGoal extends ActiveTargetGoal<LivingEntity> {
    public WildRageAttackGoal(MobEntity MobEntity) {
        super(MobEntity, LivingEntity.class, 0, true, true, LivingEntity::attackable);
    }
}
