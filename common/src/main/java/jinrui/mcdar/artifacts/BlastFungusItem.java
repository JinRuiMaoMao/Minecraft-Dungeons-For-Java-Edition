package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.effects.ArtifactEffects;
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


public class BlastFungusItem extends ArtifactDamagingItem{
    public BlastFungusItem() {
            super(
                    DamagingArtifactID.BLAST_FUNGUS,
                    Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                            .get(DamagingArtifactID.BLAST_FUNGUS).mcdar$getDurability()
            );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        CleanlinessHelper.playCenteredSound(user, SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
        ArtifactEffects.causeBlastFungusExplosions(user, 5, 4);
        if (!user.isCreative()){
            itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));
        }
        McdarEnchantmentHelper.mcdar$cooldownHelper(
                user,
                this
        );

        return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
