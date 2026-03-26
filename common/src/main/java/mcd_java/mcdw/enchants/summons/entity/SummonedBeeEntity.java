/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.summons.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class SummonedBeeEntity extends BeeEntity {

    private Entity summoner;

    public SummonedBeeEntity(EntityType<? extends SummonedBeeEntity> type, World world){
        super(EntityType.BEE, world);
    }

    public static DefaultAttributeContainer.Builder createSummonedBeeEntityAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0)
                .add(EntityAttributes.FLYING_SPEED, 0.6f)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3f)
                .add(EntityAttributes.ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    public void setSummoner(Entity user){
        summoner = user;
    }

    protected void customServerAiStep(){
        if(summoner instanceof PlayerEntity summoningPlayer){
            if(summoningPlayer.getLastHurtByMob() != null){
                this.setBeeAttacker(summoningPlayer.getLastHurtByMob());
            }

            if (summoningPlayer.getLastHurtMob() != null){
                this.setBeeAttacker(summoningPlayer.getLastHurtMob());
            }
        }
        super.customServerAiStep();
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean setBeeAttacker(LivingEntity attacker){
        if(attacker.equals(summoner)){
            return false;
        }
        setLastHurtByMob(attacker);
        return true;
    }
    public boolean doHurtTarget(Entity target) {
        return !target.equals(summoner) && !this.hasStung() && super.doHurtTarget(target);
    }

}
