package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.enums.SummoningArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class EnchantedGrassItem extends ArtifactSummoningItem{
    public EnchantedGrassItem() {
        super(
                SummoningArtifactID.ENCHANTED_GRASS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS
                        .get(SummoningArtifactID.ENCHANTED_GRASS).mcdar$getDurability()
        );
    }

    public InteractionResult useOn (UseOnContext itemUsageContext){
        if (itemUsageContext.getLevel() instanceof ServerLevel serverWorld) {
            Player itemUsageContextPlayer = itemUsageContext.getPlayer();

            if (itemUsageContextPlayer != null) {

                int effectInt = CleanlinessHelper.RANDOM.nextInt(3);
                Sheep sheep = SummoningHelper.SHEEP.get(effectInt).create(serverWorld);

                if (SummoningHelper.mcdar$summonSummonableEntity(sheep, itemUsageContextPlayer, itemUsageContext.getClickedPos())) {
                    assert sheep != null;
                    if (CleanlinessHelper.percentToOccur(1))
                        sheep.setCustomName(Component.literal("Lilly"));
                    SummoningHelper.mcdar$summonedSheepEffect(sheep, effectInt);

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
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
