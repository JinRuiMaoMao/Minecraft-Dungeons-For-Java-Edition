package jinrui.mcdar.effects;

import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.interfaces.OwnableSummon;
import jinrui.mcdar.registries.EnchantsRegistry;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.enchantment.EnchantmentHelper;

public class EnchantmentEffects {

    public static float beastBossDamage(OwnableSummon summonedEntity, ServerWorld serverWorld) {
        if (summonedEntity.getOwner() != null) {
            UUID summonerUUID = summonedEntity.getOwnerUUID();
            if (summonerUUID != null) {
                Entity beastOwner = serverWorld.getEntity(summonerUUID);
                if (beastOwner instanceof LivingEntity beastOwnerAsLiving) {
                    int beastBossLevel =
                            EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.BEAST_BOSS,
                                    beastOwnerAsLiving);
                    if (beastBossLevel > 0) {
                        return 1.1F + (0.1F * beastBossLevel);
                    }
                }
            }
        }
        return 1f;
    }

    public static void activateBeastBurst(PlayerEntity player) {
        float explosionRadius = 3.0f;
        List<StatusEffectInstance> potionEffects = PotionUtil.getMobEffects(PlayerEntity.getUseItem());
        if (potionEffects.isEmpty()) return;
        if (potionEffects.get(0).getEffect() == StatusEffects.HEAL){
            int beastBurstLevel =
                    EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.BEAST_BURST,
                            PlayerEntity);
            if (beastBurstLevel > 0){
                for (LivingEntity summonedMob : AOEHelper.getEntitiesByPredicate(PlayerEntity, 10,
                        (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, PlayerEntity))) {
                    if (summonedMob == null) continue;
                    CleanlinessHelper.playCenteredSound(summonedMob, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);
                    AOECloudHelper.spawnExplosionCloud(summonedMob, summonedMob, explosionRadius);
                    AOEHelper.causeExplosion(PlayerEntity, summonedMob, 3 * beastBurstLevel, explosionRadius);
                }
            }
        }
    }

    public static void activateBeastSurge(PlayerEntity player) {
        List<StatusEffectInstance> potionEffects = PotionUtil.getMobEffects(PlayerEntity.getUseItem());
        if (potionEffects.isEmpty()) return;
        if (potionEffects.get(0).getEffect().equals(StatusEffects.HEAL)) {
            int beastSurgeLevel =
                    EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.BEAST_SURGE,
                            PlayerEntity);
            if (beastSurgeLevel > 0) {
                AOEHelper.afflictNearbyEntities(LivingEntity.class, PlayerEntity, 10,
                        (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, PlayerEntity),
                        new StatusEffectInstance(StatusEffects.MOVEMENT_SPEED, 10 * 20, (beastSurgeLevel * 3) - 1),
                        new StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 10 * 20, (beastSurgeLevel * 3) - 1));
            }
        }
    }
}
