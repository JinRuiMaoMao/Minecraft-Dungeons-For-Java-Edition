package jinrui.mcdar.effects;

import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import jinrui.mcdar.registries.ArtifactsRegistry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ArtifactEffects {
    private static final float EXPLOSION_RADIUS = 3.0F;

    public static void activatePowerShaker(PlayerEntity player, LivingEntity target) {
        // Temporary way to stop crash with Industrial Revolution Slaughter Block
        if (PlayerEntity.getScoreboardName().equals("slaughter")) {
            return;
        }

        ItemStack offhand = PlayerEntity.getOffhandItem();
        if (target != null && offhand.getItem() == ArtifactsRegistry.DAMAGING_ARTIFACT.get(DamagingArtifactID.POWERSHAKER).asItem()) {
            if (CleanlinessHelper.isCoolingDown(PlayerEntity, offhand.getItem()) && CleanlinessHelper.percentToOccur(20)) {
                CleanlinessHelper.playCenteredSound(target, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);
                AOECloudHelper.spawnExplosionCloud(PlayerEntity, target, EXPLOSION_RADIUS);
                AOEHelper.affectNearbyEntities(PlayerEntity, 3.0f,
                        (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, PlayerEntity, target),
                        livingEntity -> AOEHelper.causeExplosion(PlayerEntity, target, target.getMaxHealth() * 0.2F, EXPLOSION_RADIUS)
                );
            }
        }
    }

    public static void causeBlastFungusExplosions(LivingEntity user, float distance, float damageAmount) {
        AOEHelper.affectNearbyEntities(user, distance,
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, user, nearbyEntity),
                livingEntity -> {
                    if (!(livingEntity instanceof PlayerEntity playerEntity && playerEntity.getAbilities().instabuild)) {
                        AOECloudHelper.spawnExplosionCloud(user, livingEntity, EXPLOSION_RADIUS);
                        AOEHelper.causeExplosion(user, livingEntity, damageAmount, distance);
                    }
                }
        );
    }

    public static void enchantersTomeEffects(PlayerEntity user) {
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, user))){
            StatusEffect[] statuses = {StatusEffects.DIG_SPEED, StatusEffects.DAMAGE_BOOST, StatusEffects.MOVEMENT_SPEED};
            StatusEffectInstance statusEffectInstance =
                    new StatusEffectInstance(statuses[CleanlinessHelper.RANDOM.nextInt(statuses.length)], 100, 2);
            nearbyEntity.addStatusEffect(statusEffectInstance);
        }
    }

    public static void updraftNearbyEnemies(PlayerEntity user) {
        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())){
            nearbyEntity.setDeltaMovement(0.0D, 1.25D, 0.0D);
        }
    }
}
