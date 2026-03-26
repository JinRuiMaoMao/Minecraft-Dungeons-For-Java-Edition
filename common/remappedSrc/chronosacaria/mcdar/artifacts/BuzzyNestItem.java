package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.SummoningArtifactID;
import jinrui.mcdar.registries.SummonedEntityRegistry;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BuzzyNestItem extends ArtifactSummoningItem{
    public BuzzyNestItem() {
        super(
                SummoningArtifactID.BUZZY_NEST,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS
                        .get(SummoningArtifactID.BUZZY_NEST).mcdar$getDurability()
        );
    }

    public InteractionResult useOn(UseOnContext itemUsageContext) {
        return CleanlinessHelper.mcdar$cleanUseSummon(
                itemUsageContext,
                this,
                SummonedEntityRegistry.BUZZY_NEST_BEE_ENTITY
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
