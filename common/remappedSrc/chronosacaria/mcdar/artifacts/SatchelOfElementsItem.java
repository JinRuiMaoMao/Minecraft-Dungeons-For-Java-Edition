package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.StatusInflictingArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SatchelOfElementsItem extends ArtifactStatusInflictingItem{
    public SatchelOfElementsItem() {
        super(
                StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS
                        .get(StatusInflictingArtifactID.SATCHEL_OF_ELEMENTS).mcdar$getDurability()
        );
    }

    @Override
    public InteractionResult useOn (UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().level().getServer() != null) {
            ServerPlayer user = context.getPlayer().level().getServer().getPlayerList().getPlayer(context.getPlayer().getUUID());
            if (user != null) {
                InteractionHand hand = user.getUsedItemHand();
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
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
