package jinrui.mcdar.goals;

import jinrui.mcdar.api.interfaces.OwnableSummon;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TrackSummonerAttackerGoal<T extends PathAwareEntity & OwnableSummon> extends TrackTargetGoal {
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
        if (summonedEntity != null && summonedEntity.getOwner() instanceof PlayerEntity summoner) {
            if (summonedEntity instanceof TameableEntity tameableSummonedEntity && tameableSummonedEntity.isSitting())
                return false;
            this.attacker = summoner.getLastHurtByMob();
            int i = summoner.getLastHurtByMobTimestamp();
            return i != this.lastAttackedTime
                    && this.canAttack(this.attacker, TargetPredicate.DEFAULT)
                    && (summonedEntity instanceof TameableEntity tameableSummonedEntity)
                        ? tameableSummonedEntity.canAttackWithOwner(this.attacker, summoner)
                        : true;
        } else {
            return false;
        }
    }

    public void start() {
        if (summonedEntity != null && summonedEntity.getOwner() instanceof PlayerEntity summoner) {
            this.summonedEntity.setTarget(this.attacker);
            this.lastAttackedTime = summoner.getLastHurtByMobTimestamp();
            super.start();
        }
    }
}