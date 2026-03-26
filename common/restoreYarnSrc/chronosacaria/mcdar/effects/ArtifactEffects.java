package jinrui.mcdar.effects;

import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import jinrui.mcdar.registries.ArtifactsRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArtifactEffects {
    private static final float EXPLOSION_RADIUS = 3.0F;

    public static void activatePowerShaker(Player player, LivingEntity target) {
        // Temporary way to stop crash with Industrial Revolution Slaughter Block
        if (player.getScoreboardName().equals("slaughter")) {
            return;
        }

        ItemStack offhand = player.getOffhandItem();
        if (target != null && offhand.getItem() == ArtifactsRegistry.DAMAGING_ARTIFACT.get(DamagingArtifactID.POWERSHAKER).asItem()) {
            if (CleanlinessHelper.isCoolingDown(player, offhand.getItem()) && CleanlinessHelper.percentToOccur(20)) {
                CleanlinessHelper.playCenteredSound(target, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);
                AOECloudHelper.spawnExplosionCloud(player, target, EXPLOSION_RADIUS);
                AOEHelper.affectNearbyEntities(player, 3.0f,
                        (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, player, target),
                        livingEntity -> AOEHelper.causeExplosion(player, target, target.getMaxHealth() * 0.2F, EXPLOSION_RADIUS)
                );
            }
        }
    }

    public static void causeBlastFungusExplosions(LivingEntity user, float distance, float damageAmount) {
        AOEHelper.affectNearbyEntities(user, distance,
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, user, nearbyEntity),
                livingEntity -> {
                    if (!(livingEntity instanceof Player playerEntity && playerEntity.getAbilities().instabuild)) {
                        AOECloudHelper.spawnExplosionCloud(user, livingEntity, EXPLOSION_RADIUS);
                        AOEHelper.causeExplosion(user, livingEntity, damageAmount, distance);
                    }
                }
        );
    }

    public static void enchantersTomeEffects(Player user) {
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, user))){
            MobEffect[] statuses = {MobEffects.DIG_SPEED, MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED};
            MobEffectInstance statusEffectInstance =
                    new MobEffectInstance(statuses[CleanlinessHelper.RANDOM.nextInt(statuses.length)], 100, 2);
            nearbyEntity.addEffect(statusEffectInstance);
        }
    }

    public static void updraftNearbyEnemies(Player user) {
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
            nearbyEntity.setDeltaMovement(0.0D, 1.25D, 0.0D);
        }
    }
}
