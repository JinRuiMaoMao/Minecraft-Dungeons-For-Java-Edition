package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.AgilityArtifactID;
import jinrui.mcdar.registries.StatusEffectInit;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

public class LightFeatherItem extends ArtifactAgilityItem{
    public LightFeatherItem() {
        super(
                AgilityArtifactID.LIGHT_FEATHER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS
                        .get(AgilityArtifactID.LIGHT_FEATHER).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        user.jumpFromGround();

        for (LivingEntity nearbyEntity : AOEHelper.getEntitiesByPredicate(user, 5,
                (nearbyEntity) -> nearbyEntity != user && !AbilityHelper.isPetOf(nearbyEntity, user) && nearbyEntity.isAlive())) {
            AOEHelper.knockbackNearbyEnemies(user, nearbyEntity, 5.0F);

            nearbyEntity.addStatusEffect(new StatusEffectInstance(StatusEffectInit.STUNNED, 60));
            nearbyEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 60));
            nearbyEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 4));
        }

        if (!user.isCreative())
            itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));

        McdarEnchantmentHelper.mcdar$cooldownHelper(
                user,
                this
        );

        return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
