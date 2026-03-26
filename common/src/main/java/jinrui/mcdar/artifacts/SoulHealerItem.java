package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DefensiveArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

public class SoulHealerItem extends ArtifactDefensiveItem{
    public SoulHealerItem() {
        super(
                DefensiveArtifactID.SOUL_HEALER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                        .get(DefensiveArtifactID.SOUL_HEALER).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.totalExperience >= 20 || user.isCreative()){

            boolean bl = user.getHealth() < user.getMaxHealth();
            float healedAmount = bl ?
                    healAlly(user) : healMostInjuredAlly(user, 12);
            if (!user.isCreative()){
                if (healedAmount > 0) {
                    user.giveExperiencePoints((int) (-healedAmount));
                    itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));
                    McdarEnchantmentHelper.mcdar$cooldownHelper(
                            user,
                            this,
                            bl ? 20
                                    : Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                                        .get(DefensiveArtifactID.SOUL_HEALER)
                                        .mcdar$getMaxCooldownEnchantmentTime()
                    );
                }
            }
        }

        return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }

    public static float healMostInjuredAlly(LivingEntity healer, float distance) {
        List<LivingEntity> nearbyEntities = AOEHelper.getEntitiesByPredicate(healer, distance,
                (nearbyEntity) -> AbilityHelper.canHealEntity(healer, nearbyEntity));
        if (!nearbyEntities.isEmpty()) {
            nearbyEntities.sort((o1, o2) -> {
                float o1LostHealth = o1.getMaxHealth() - o1.getHealth();
                float o2LostHealth = o2.getMaxHealth() - o2.getHealth();
                return Float.compare(o1LostHealth, o2LostHealth);
            });
            LivingEntity mostInjuredAlly = nearbyEntities.get(nearbyEntities.size() - 1);
            return healAlly(mostInjuredAlly);
        } else
            return 0;
    }
    public static float healAlly(LivingEntity allyToBeHealed) {
        float maxHealth = allyToBeHealed.getMaxHealth();
        float lostHealth = maxHealth - allyToBeHealed.getHealth();
        float healedAmount = Math.min(lostHealth, 0.2F * maxHealth);
        allyToBeHealed.heal(healedAmount);
        //addParticles((ServerWorld) world, mostInjuredAlly, ParticleTypes.HEART);
        return healedAmount;
    }
}
