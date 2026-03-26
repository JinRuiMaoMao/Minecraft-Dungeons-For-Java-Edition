package jinrui.mcdar.goals;

import jinrui.mcdar.api.SummoningHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class FollowSummonerGoal<T extends PathfinderMob & OwnableEntity> extends Goal {
    private final T summonedEntity;
    private final double speed;
    private final PathNavigation navigation;
    private int countdownTicks;
    private final float maxDistance;
    private final float minDistance;

    public FollowSummonerGoal(T summonedEntity, double speed,
                                       PathNavigation navigation, float maxDistance, float minDistance) {
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
                && !(this.summonedEntity.distanceToSqr(livingEntity) < (double) (this.minDistance * this.minDistance));
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else {
            return this.summonedEntity.distanceToSqr(this.summonedEntity.getOwner()) > (double) (this.maxDistance * this.maxDistance);
        }
    }

    public void tick() {
        this.summonedEntity.getLookControl().setLookAt(this.summonedEntity.getOwner(), 10.0F,
                (float) this.summonedEntity.getMaxHeadXRot());
        if (--this.countdownTicks <= 0) {
            this.countdownTicks = 10;
            if (!this.summonedEntity.isPassenger()) {
                if (this.summonedEntity.distanceToSqr(this.summonedEntity.getOwner()) >= 144.0D) {
                    SummoningHelper.mcdar$tryTeleport(this.summonedEntity, this.summonedEntity.getOwner());
                } else {
                    this.navigation.moveTo(this.summonedEntity.getOwner(), this.speed);
                }
            }
        }
    }

}
