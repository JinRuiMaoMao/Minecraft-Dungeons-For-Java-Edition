package mcd_java.api;

import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class AbilityHelper {

    public static List<LivingEntity> getPotentialPounceTargets(LivingEntity pouncer, float distance){
        return pouncer.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                new AABB(pouncer.blockPosition()).inflate(distance),
                (nearbyEntity) -> AbilityHelper.isPounceTarget(nearbyEntity, pouncer, pouncer));
    }

    public static boolean isPetOf(LivingEntity self, LivingEntity owner) {
        if (self instanceof TamableAnimal pet)
            return pet.getOwner() == owner;
        return false;
    }

    private static boolean isAVillagerOrIronGolem(LivingEntity entity) {
        return (entity instanceof Villager) || (entity instanceof IronGolem);
    }

    private static boolean isNotAPlayer(LivingEntity entity) {
        return !(entity instanceof Player);
    }

    private static boolean isAllyOf(LivingEntity self, LivingEntity other) {
        return isPetOf(other, self)
                || isAVillagerOrIronGolem(other)
                || self.isAlliedTo(other);
    }

    public static boolean isAoeTarget(LivingEntity self, LivingEntity attacker, LivingEntity center) {
        return self != attacker
                && self.isAlive()
                && !isAllyOf(attacker, self)
                && !isUnaffectedByAoe(self)
                && center.hasLineOfSight(self);
    }

    public static boolean isPounceTarget(LivingEntity self, LivingEntity attacker, LivingEntity center){
        return self != attacker
                && self.isAlive()
                && !isAllyOf(attacker, self)
                && center.hasLineOfSight(self);
    }

    private static boolean isUnaffectedByAoe(LivingEntity entity) {
        if (entity instanceof Player)
            return ((Player) entity).isCreative();
        return false;
    }

    public static boolean canHealEntity(LivingEntity self, LivingEntity other) {
        if (!self.isAlive() || !other.isAlive())
            return false;
        return isAllyOf(self, other)
                && self.hasLineOfSight(other);
    }

    public static boolean canFireAtEnemy(LivingEntity self, LivingEntity enemy) {
        if (!self.isAlive() || !enemy.isAlive())
            return false;
        return self.hasLineOfSight(enemy)
                && !isAllyOf(self, enemy)
                && isNotAPlayer(enemy);
    }
}
