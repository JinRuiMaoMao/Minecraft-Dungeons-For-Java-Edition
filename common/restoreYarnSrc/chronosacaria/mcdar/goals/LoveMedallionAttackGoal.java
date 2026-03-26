package jinrui.mcdar.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class LoveMedallionAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {

    public LoveMedallionAttackGoal(Mob mob) {
        super(mob, LivingEntity.class, 0, true, true, LoveMedallionAttackGoal::isNonBossMobNotPlayer);
    }

    private static boolean isNonBossMobNotPlayer(LivingEntity livingEntity) {
        return !(livingEntity instanceof Player);
    }

    @Override
    public void start() {
        super.start();
        this.mob.setNoActionTime(0);
    }

}
