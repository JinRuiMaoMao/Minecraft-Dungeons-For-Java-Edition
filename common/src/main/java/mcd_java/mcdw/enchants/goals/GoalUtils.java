/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.goals;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.AbstractHorse;

public class GoalUtils {

    @Nullable
    public static LivingEntity getOwner(AbstractHorse horseBaseEntity){
        try{
            UUID ownerUniqueId = horseBaseEntity.getOwnerUUID();
            return ownerUniqueId == null ? null : horseBaseEntity.getWorld().getPlayerByUUID(ownerUniqueId);
        }catch (IllegalArgumentException var2) {
            return null;
        }
    }
}