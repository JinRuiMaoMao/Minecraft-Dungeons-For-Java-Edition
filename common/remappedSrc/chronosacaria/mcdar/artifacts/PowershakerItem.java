package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PowershakerItem extends ArtifactDamagingItem{
    public PowershakerItem() {
        super(
                DamagingArtifactID.POWERSHAKER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                        .get(DamagingArtifactID.POWERSHAKER).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
        return CleanlinessHelper.mcdar$cleanUseWithOptionalStatus(
                user,
                hand,
                this,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
