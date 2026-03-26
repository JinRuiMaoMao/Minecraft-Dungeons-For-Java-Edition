package jinrui.mcdar.goals;

import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;

public class FollowSummonerGoal<T extends PathAwareEntity & OwnableSummon> extends Goal {
    private final T summonedEntity;
    private final double speed;
    private final EntityNavigation navigation;
    private int countdownTicks;
    private final float maxDistance;
    private final float minDistance;

    public FollowSummonerGoal(T summonedEntity, double speed,
                                       EntityNavigation navigation, float maxDistance, float minDistance) {
        this.summonedEntity = summonedEntity;
        this.speed = speed;
        this.navigation = navigation;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity = this.summonedEntity.getOwner();

        return livingEntity != null
                && !livingEntity.isSpectator()
                && !(this.summonedEntity.squaredDistanceTo(livingEntity) < (double) (this.minDistance * this.minDistance));
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return this.summonedEntity.squaredDistanceTo(this.summonedEntity.getOwner()) > (double) (this.maxDistance * this.maxDistance);
        }
    }

    public void tick() {
        this.summonedEntity.getLookControl().setLookAt(this.summonedEntity.getOwner(), 10.0F,
                (float) this.summonedEntity.getMaxHeadRotation());
        if (--this.countdownTicks <= 0) {
            this.countdownTicks = 10;
            if (!this.summonedEntity.isPassenger()) {
                if (this.summonedEntity.squaredDistanceTo(this.summonedEntity.getOwner()) >= 144.0D) {
                    SummoningHelper.mcdar$tryTeleport(this.summonedEntity, this.summonedEntity.getOwner());
                } else {
                    this.navigation.startMovingTo(this.summonedEntity.getOwner(), this.speed);
                }
            }
        }
    }

}
