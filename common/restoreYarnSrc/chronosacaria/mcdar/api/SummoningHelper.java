package jinrui.mcdar.api;

import jinrui.mcdar.api.interfaces.Summonable;
import jinrui.mcdar.registries.SummonedEntityRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class SummoningHelper {

    public static final List<? extends EntityType<? extends Sheep>> SHEEP = Arrays.asList(
            SummonedEntityRegistry.ENCHANTED_GRASS_GREEN_SHEEP_ENTITY,
            SummonedEntityRegistry.ENCHANTED_GRASS_BLUE_SHEEP_ENTITY,
            SummonedEntityRegistry.ENCHANTED_GRASS_RED_SHEEP_ENTITY);

    public static void mcdar$summonedSheepEffect(LivingEntity sheep, int effectInt) {
        switch (effectInt) {
            case 0 -> AOEHelper.afflictNearbyEntities(Mob.class, sheep, 5,
                    (nearbyEntity) -> nearbyEntity != sheep && nearbyEntity.isAlive(), new MobEffectInstance(MobEffects.POISON, 100, 4));
            case 1 -> AOEHelper.afflictNearbyEntities(Player.class, sheep, 10,
                    LivingEntity::isAlive, new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 2));
            case 2 -> {
                for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(Mob.class, sheep, 5,
                        (nearbyEntity) -> nearbyEntity != sheep && nearbyEntity.isAlive())) {
                    nearbyEntity.setSecondsOnFire(5);
                }
            }
            default -> {}
        }
    }

    public static boolean mcdar$summonSummonableEntity(LivingEntity entityToSpawn, LivingEntity summoner, BlockPos blockPos) {
        Level world = summoner.level();

        if (entityToSpawn instanceof Summonable summonableEntity) {
            try {
                summonableEntity.setSummoner(summoner);
                entityToSpawn.moveTo(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), 0, 0);
                world.addFreshEntity(entityToSpawn);
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        }
        return false;
    }

    public static void mcdar$trackAndProtectSummoner(Mob summonedEntity) {
        if (summonedEntity instanceof OwnableEntity summonable && summonedEntity.isAlive()) {
            if (summonable.getOwner() instanceof Player summoner) {
                if (summoner.getLastHurtByMob() != null)
                    summonedEntity.setTarget(summoner.getLastHurtByMob());
                else if (summoner.getLastHurtMob() != null && summoner.getLastHurtMob() != summonedEntity)
                    summonedEntity.setTarget(summoner.getLastHurtMob());
            }
        }
    }

    public static boolean mcdar$attackTarget(Mob summonedMob, Entity target, SoundEvent soundEvent, float damageAmount) {
        boolean bl = target.hurt(target.level().damageSources().mobAttack(summonedMob), damageAmount);
        if (bl) {
            summonedMob.doHurtTarget(target);
            summonedMob.playSound(soundEvent, 1f,(summonedMob.getRandom().nextFloat() - summonedMob.getRandom().nextFloat()) * 0.2F + 1.0F);
        }
        return bl;
    }

    public static void mcdar$tryTeleport(Mob summonedEntity, @Nullable LivingEntity summoner) {
        if (summoner == null)
            return;

        BlockPos blockPos = new BlockPos(summoner.blockPosition());

        for (int i = 0; i < 10; ++i) {
            int j = mcdar$getRandomInt(summonedEntity, -3, 3);
            int k = mcdar$getRandomInt(summonedEntity, -1, 1);
            int l = mcdar$getRandomInt(summonedEntity, -3, 3);
            boolean bl = mcdar$tryTeleportTo(summonedEntity, summoner, blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l); //23343
            if (bl) {
                return;
            }
        }
    }

    private static int mcdar$getRandomInt(Mob summonedEntity, int i, int j) {
        return summonedEntity.getRandom().nextInt(j - i + 1) + i;
    }

    private static boolean mcdar$tryTeleportTo(Mob summonedEntity, LivingEntity summoner, int i, int j, int k){
        if (Math.abs((double) i - summoner.getX()) < 2.0D && Math.abs((double) k - summoner.getZ()) < 2.0D) {
            return false;
        } else if (!mcdar$canTeleportTo(summonedEntity, new BlockPos(i, j, k))){
            return false;
        } else {
            summonedEntity.getNavigation().stop();
            summonedEntity.moveTo((double)i + 0.5, j, (double)k + 0.5, summonedEntity.getYRot(), summonedEntity.getXRot());
            return true;
        }
    }

    private static boolean mcdar$canTeleportTo(Mob summonedEntity, BlockPos blockPos) {
        if (WalkNodeEvaluator.getBlockPathTypeStatic(summonedEntity.getCommandSenderWorld(), new BlockPos.MutableBlockPos()) != BlockPathTypes.WALKABLE)
            return false;
        return summonedEntity.level().noCollision(summonedEntity, summonedEntity.getBoundingBox().move(blockPos.subtract(new BlockPos(summonedEntity.blockPosition()))));
    }
}
