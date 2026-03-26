package jinrui.mcdar.goals;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class TrackSummonerAttackerGoal<T extends PathfinderMob & OwnableEntity> extends TargetGoal {
    private final T summonedEntity;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public TrackSummonerAttackerGoal(T summonedEntity) {
        super(summonedEntity, false);
        this.summonedEntity = summonedEntity;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @SuppressWarnings("SimplifiableConditionalExpression") // Humans need to read this, IntelliJ...
    public boolean canUse() {
        if (summonedEntity != null && summonedEntity.getOwner() instanceof Player summoner) {
            if (summonedEntity instanceof TamableAnimal tameableSummonedEntity && tameableSummonedEntity.isOrderedToSit())
                return false;
            this.attacker = summoner.getLastHurtByMob();
            int i = summoner.getLastHurtByMobTimestamp();
            return i != this.lastAttackedTime
                    && this.canAttack(this.attacker, TargetingConditions.DEFAULT)
                    && (summonedEntity instanceof TamableAnimal tameableSummonedEntity)
                        ? tameableSummonedEntity.wantsToAttack(this.attacker, summoner)
                        : true;
        } else {
            return false;
        }
    }

    public void start() {
        if (summonedEntity != null && summonedEntity.getOwner() instanceof Player summoner) {
            this.mob.setTarget(this.attacker);
            this.lastAttackedTime = summoner.getLastHurtByMobTimestamp();
            super.start();
        }
    }
}