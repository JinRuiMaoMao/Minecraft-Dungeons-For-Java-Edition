package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.QuiverArtifactID;
import jinrui.mcdar.registries.EnchantsRegistry;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;

public class TormentQuiverItem extends ArtifactQuiverItem{
    public TormentQuiverItem() {
        super(
                QuiverArtifactID.TORMENT_QUIVER,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS
                        .get(QuiverArtifactID.TORMENT_QUIVER).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand){
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
        return new TypedActionResult<>(InteractionResult.SUCCESS, itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}