package mcd_java.entities;

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

    public SummonedBeeEntity(EntityType<? extends SummonedBeeEntity> entityType, Level world) {
        super(EntityType.BEE, world);
    }

    public static AttributeSupplier getAttributeContainer(){
        return Mob
                .createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 2.5D)
                .add(Attributes.MOVEMENT_SPEED, 2.5D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .build();
    }

    public void setSummoner(Entity user){
        summoner = user;
    }

    protected void customServerAiStep(){
        if (summoner instanceof Player){
            if (((Player)summoner).getLastHurtByMob() != null)
                this.setBeeAttacker(((Player)summoner).getLastHurtByMob());
            if (((Player)summoner).getLastHurtMob() != null)
                this.setBeeAttacker(((Player)summoner).getLastHurtMob());
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
