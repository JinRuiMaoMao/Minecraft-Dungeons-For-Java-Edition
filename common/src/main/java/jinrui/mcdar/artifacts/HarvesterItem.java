package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOECloudHelper;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.DamagingArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

public class HarvesterItem extends ArtifactDamagingItem{
    public HarvesterItem() {
        super(
                DamagingArtifactID.HARVESTER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                        .get(DamagingArtifactID.HARVESTER).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
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
        return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
