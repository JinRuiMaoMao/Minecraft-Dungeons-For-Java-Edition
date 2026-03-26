package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.QuiverArtifactID;
import jinrui.mcdar.registries.EnchantsRegistry;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class TormentQuiverItem extends ArtifactQuiverItem{
    public TormentQuiverItem() {
        super(
                QuiverArtifactID.TORMENT_QUIVER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS
                        .get(QuiverArtifactID.TORMENT_QUIVER).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use (Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);
        if (user.totalExperience >= 20 || user.isCreative()){

            int cooldownLevel = EnchantmentHelper.getEnchantmentLevel(EnchantsRegistry.COOLDOWN, user);
            user.getCooldowns().addCooldown(
                    this,
                    (cooldownLevel + 1) * Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS
                            .get(QuiverArtifactID.TORMENT_QUIVER)
                            .mcdar$getMaxCooldownEnchantmentTime()
            );

            if (!user.isCreative()) {
                user.giveExperiencePoints(-20);
                itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}