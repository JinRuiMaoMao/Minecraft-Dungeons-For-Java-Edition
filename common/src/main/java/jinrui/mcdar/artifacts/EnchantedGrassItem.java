package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.api.SummoningHelper;
import jinrui.mcdar.enums.SummoningArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;

public class EnchantedGrassItem extends ArtifactSummoningItem{
    public EnchantedGrassItem() {
        super(
                SummoningArtifactID.ENCHANTED_GRASS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS
                        .get(SummoningArtifactID.ENCHANTED_GRASS).mcdar$getDurability()
        );
    }

    public ActionResult useOn (ItemUsageContext itemUsageContext){
        if (itemUsageContext.getLevel() instanceof ServerWorld serverWorld) {
            PlayerEntity itemUsageContextPlayer = itemUsageContext.getPlayer();

            if (itemUsageContextPlayer != null) {

                int effectInt = CleanlinessHelper.RANDOM.nextInt(3);
                Sheep sheep = SummoningHelper.SHEEP.get(effectInt).create(serverWorld);

                if (SummoningHelper.mcdar$summonSummonableEntity(sheep, itemUsageContextPlayer, itemUsageContext.getClickedPos())) {
                    assert sheep != null;
                    if (CleanlinessHelper.percentToOccur(1))
                        sheep.setCustomName(Text.literal("Lilly"));
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
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
