package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.SummoningArtifactID;
import jinrui.mcdar.registries.SummonedEntityRegistry;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;

public class WonderfulWheatItem extends ArtifactSummoningItem{
    public WonderfulWheatItem() {
        super(
                SummoningArtifactID.WONDERFUL_WHEAT,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS
                        .get(SummoningArtifactID.WONDERFUL_WHEAT).mcdar$getDurability()
        );
    }

    public ActionResult useOn (ItemUsageContext itemUsageContext){
        return CleanlinessHelper.mcdar$cleanUseSummon(
                itemUsageContext,
                this,
                SummonedEntityRegistry.WONDERFUL_WHEAT_LLAMA_ENTITY
        );
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
