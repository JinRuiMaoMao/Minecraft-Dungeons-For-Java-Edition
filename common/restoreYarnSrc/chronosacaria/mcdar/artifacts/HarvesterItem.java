package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class HarvesterItem extends ArtifactDamagingItem{
    public HarvesterItem() {
        super(
                DamagingArtifactID.HARVESTER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                        .get(DamagingArtifactID.HARVESTER).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        if (user.totalExperience >= 40 || user.isCreative()) {
            CleanlinessHelper.playCenteredSound(user, SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
            AOECloudHelper.spawnExplosionCloud(user, user, 3.0f);
            AOEHelper.causeExplosion(user, user, 15, 3.0F);

            if (!user.isCreative()) {
                user.giveExperiencePoints(-40);
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
