package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DefensiveArtifactID;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TotemOfRegenerationItem extends ArtifactDefensiveItem{
    public TotemOfRegenerationItem() {
        super(
                DefensiveArtifactID.TOTEM_OF_REGENERATION,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS
                        .get(DefensiveArtifactID.TOTEM_OF_REGENERATION).mcdar$getDurability()
        );
    }

    public InteractionResult useOn (UseOnContext itemUsageContext) {
        if (itemUsageContext.getLevel() instanceof ServerLevel serverWorld) {
            Player itemUsageContextPlayer = itemUsageContext.getPlayer();
            BlockPos itemUseContextBlockPos = itemUsageContext.getClickedPos();

            BlockPos blockPos;
            if (serverWorld.getBlockState(itemUseContextBlockPos).getCollisionShape(serverWorld, itemUseContextBlockPos).isEmpty()){
                blockPos = itemUseContextBlockPos;
            } else {
                blockPos = itemUseContextBlockPos.relative(itemUsageContext.getHorizontalDirection());
            }
            if (itemUsageContextPlayer != null){

                AOECloudHelper.spawnStatusEffectCloud(itemUsageContextPlayer, blockPos, 100,
                        new MobEffectInstance(MobEffects.REGENERATION, 100, 100));

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
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
