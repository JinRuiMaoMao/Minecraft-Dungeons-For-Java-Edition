package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.StatusInflictingArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

public class CorruptedSeedsItem extends ArtifactStatusInflictingItem{
    public CorruptedSeedsItem() {
        super(
                StatusInflictingArtifactID.CORRUPTED_SEEDS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS
                        .get(StatusInflictingArtifactID.CORRUPTED_SEEDS).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        AOEHelper.afflictNearbyEntities(user, new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 4),
                new StatusEffectInstance(StatusEffects.POISON, 140, 1));

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
