package mcd_java.mcda.entities;

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

    public SummonedBeeEntity(EntityType<? extends SummonedBeeEntity> entityType, World world) {
        super(EntityType.BEE, world);
    }

    public static DefaultAttributeContainer getAttributeContainer(){
        return MobEntity
                .createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10.0D)
                .add(EntityAttributes.FLYING_SPEED, 2.5D)
                .add(EntityAttributes.MOVEMENT_SPEED, 2.5D)
                .add(EntityAttributes.ATTACK_DAMAGE, 5.0D)
                .add(EntityAttributes.FOLLOW_RANGE, 48.0D)
                .build();
    }

    public void setSummoner(Entity user){
        summoner = user;
    }

    protected void customServerAiStep(){
        if (summoner instanceof PlayerEntity){
            if (((PlayerEntity)summoner).getLastHurtByMob() != null)
                this.setBeeAttacker(((PlayerEntity)summoner).getLastHurtByMob());
            if (((PlayerEntity)summoner).getLastHurtMob() != null)
                this.setBeeAttacker(((PlayerEntity)summoner).getLastHurtMob());
        }
        super.customServerAiStep();
    }

    private void setBeeAttacker(LivingEntity attacker){
        if (attacker.equals(summoner))
            return;
        setLastHurtByMob(attacker);
    }

    public boolean doHurtTarget(Entity target) {
        if (target.equals(summoner) || this.hasStung())
            return false;
        return super.doHurtTarget(target);
    }
}
