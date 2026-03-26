package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DefensiveArtifactID;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;

public class TotemOfRegenerationItem extends ArtifactDefensiveItem{
    public TotemOfRegenerationItem() {
        super(
                DefensiveArtifactID.TOTEM_OF_REGENERATION,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                        .get(DefensiveArtifactID.TOTEM_OF_REGENERATION).mcdar$getDurability()
        );
    }

    public ActionResult useOn (ItemUsageContext itemUsageContext) {
        if (itemUsageContext.getLevel() instanceof ServerWorld serverWorld) {
            PlayerEntity itemUsageContextPlayer = itemUsageContext.getPlayer();
            BlockPos itemUseContextBlockPos = itemUsageContext.getClickedPos();

            BlockPos blockPos;
            if (serverWorld.getBlockState(itemUseContextBlockPos).getCollisionShape(serverWorld, itemUseContextBlockPos).isEmpty()){
                blockPos = itemUseContextBlockPos;
            } else {
                blockPos = itemUseContextBlockPos.relative(itemUsageContext.getHorizontalDirection());
            }
            if (itemUsageContextPlayer != null){

                AOECloudHelper.spawnStatusEffectCloud(itemUsageContextPlayer, blockPos, 100,
                        new StatusEffectInstance(StatusEffects.REGENERATION, 100, 100));

                if (!itemUsageContextPlayer.isCreative())
                    itemUsageContext.getItemInHand().hurtAndBreak(1, itemUsageContextPlayer,
                            (entity) -> entity.broadcastBreakEvent(itemUsageContext.getHand()));

                McdarEnchantmentHelper.mcdar$cooldownHelper(
                        itemUsageContextPlayer,
                        this
                );
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
