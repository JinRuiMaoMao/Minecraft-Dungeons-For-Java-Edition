package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.effects.ArtifactEffects;
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


public class BlastFungusItem extends ArtifactDamagingItem{
    public BlastFungusItem() {
            super(
                    DamagingArtifactID.BLAST_FUNGUS,
                    Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS
                            .get(DamagingArtifactID.BLAST_FUNGUS).mcdar$getDurability()
            );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
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

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
