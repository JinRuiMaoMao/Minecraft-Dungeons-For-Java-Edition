package jinrui.mcdar.effects;

import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.registries.EnchantsRegistry;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentEffects {

    public static float beastBossDamage(OwnableEntity summonedEntity, ServerLevel serverWorld) {
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

    public static void activateBeastBurst(Player player) {
        float explosionRadius = 3.0f;
        List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(player.getUseItem());
        if (potionEffects.isEmpty()) return;
        if (potionEffects.get(0).getEffect() == MobEffects.HEAL){
            int beastBurstLevel =
                    EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.BEAST_BURST,
                            player);
            if (beastBurstLevel > 0){
                for (LivingEntity summonedMob : AOEHelper.getEntitiesByPredicate(player, 10,
                        (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, player))) {
                    if (summonedMob == null) continue;
                    CleanlinessHelper.playCenteredSound(summonedMob, SoundEvents.GENERIC_EXPLODE, 0.5F, 1.0F);
                    AOECloudHelper.spawnExplosionCloud(summonedMob, summonedMob, explosionRadius);
                    AOEHelper.causeExplosion(player, summonedMob, 3 * beastBurstLevel, explosionRadius);
                }
            }
        }
    }

    public static void activateBeastSurge(Player player) {
        List<MobEffectInstance> potionEffects = PotionUtils.getMobEffects(player.getUseItem());
        if (potionEffects.isEmpty()) return;
        if (potionEffects.get(0).getEffect().equals(MobEffects.HEAL)) {
            int beastSurgeLevel =
                    EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.BEAST_SURGE,
                            player);
            if (beastSurgeLevel > 0) {
                AOEHelper.afflictNearbyEntities(LivingEntity.class, player, 10,
                        (nearbyEntity) -> AbilityHelper.isPetOf(nearbyEntity, player),
                        new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10 * 20, (beastSurgeLevel * 3) - 1),
                        new MobEffectInstance(MobEffects.DAMAGE_BOOST, 10 * 20, (beastSurgeLevel * 3) - 1));
            }
        }
    }
}
