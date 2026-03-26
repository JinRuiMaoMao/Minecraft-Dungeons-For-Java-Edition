package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LightningRodItem extends ArtifactDamagingItem{
    public LightningRodItem() {
        super(
                DamagingArtifactID.LIGHTNING_ROD,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                        .get(DamagingArtifactID.LIGHTNING_ROD).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.totalExperience >= 15 || user.isCreative()) {
            AOEHelper.electrocuteNearbyEnemies(user, 5, 5, Integer.MAX_VALUE);

            if (!user.isCreative()){
                user.giveExperiencePoints(-15);
                itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));
            }
            McdarEnchantmentHelper.mcdar$cooldownHelper(
                    user,
                    this
            );
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
