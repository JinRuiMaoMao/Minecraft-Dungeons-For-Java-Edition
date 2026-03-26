/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class WildRageAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public WildRageAttackGoal(Mob mob) {
        super(mob, LivingEntity.class, 0, true, true, LivingEntity::attackable);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setNoActionTime(0);
    }
}
