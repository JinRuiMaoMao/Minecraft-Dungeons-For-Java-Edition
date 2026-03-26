package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.AgilityArtifactID;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BootsOfSwiftnessItem extends ArtifactAgilityItem{
    public BootsOfSwiftnessItem() {
        super(
                AgilityArtifactID.BOOTS_OF_SWIFTNESS,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS
                        .get(AgilityArtifactID.BOOTS_OF_SWIFTNESS).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand){
        return CleanlinessHelper.mcdar$cleanUseWithOptionalStatus(
                user,
                hand,
                this,
                MobEffects.MOVEMENT_SPEED,
                40,
                2,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
