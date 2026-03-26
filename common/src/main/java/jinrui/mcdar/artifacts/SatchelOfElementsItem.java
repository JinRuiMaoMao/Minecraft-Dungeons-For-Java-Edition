package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.StatusInflictingArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;

public class SatchelOfElementsItem extends ArtifactStatusInflictingItem{
    public SatchelOfElementsItem() {
        super(
                StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS
                        .get(StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS).mcdar$getDurability()
        );
    }

    @Override
    public ActionResult useOn (ItemUsageContext context) {
        if (context.getPlayer() != null && context.getPlayer().getWorld().getServer() != null) {
            ServerPlayerEntity user = context.getPlayer().getWorld().getServer().getPlayerList().getPlayer(context.getPlayer().getUUID());
            if (user != null) {
                Hand hand = user.getUsedItemHand();
                ItemStack itemStack = user.getItemInHand(hand);

                if (user.totalExperience >= 15 || user.isCreative()) {
                    AOEHelper.satchelOfElementsEffects(user);

                    if (!user.isCreative()) {
                        user.giveExperiencePoints(-15);
                        itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));
                    }
                    McdarEnchantmentHelper.mcdar$cooldownHelper(
                            user,
                            this
                    );
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
