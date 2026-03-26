package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.api.McdarEnchantmentHelper;
import jinrui.mcdar.enums.StatusInflictingArtifactID;
import jinrui.mcdar.registries.StatusEffectInit;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;


public class ShockPowderItem extends ArtifactStatusInflictingItem{
    public ShockPowderItem() {
        super(
                StatusInflictingArtifactID.SHOCK_POWDER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS
                        .get(StatusInflictingArtifactID.SHOCK_POWDER).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        AOEHelper.afflictNearbyEntities(user, new MobEffectInstance(StatusEffectInit.STUNNED, 100),
                new MobEffectInstance(MobEffects.CONFUSION, 100),
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));

        if (!user.isCreative())
            itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));

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
