/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enchants.summons.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SummonedBeeEntity extends Bee {

    private Entity summoner;

    public SummonedBeeEntity(EntityType<? extends SummonedBeeEntity> type, Level world){
        super(EntityType.BEE, world);
    }

    public static AttributeSupplier.Builder createSummonedBeeEntityAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.FLYING_SPEED, 0.6f)
                .add(Attributes.MOVEMENT_SPEED, 0.3f)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    public void setSummoner(Entity user){
        summoner = user;
    }

    protected void customServerAiStep(){
        if(summoner instanceof Player summoningPlayer){
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
