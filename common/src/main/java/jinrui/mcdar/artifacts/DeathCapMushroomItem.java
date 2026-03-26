package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.CleanlinessHelper;
import jinrui.mcdar.enums.AgilityArtifactID;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;

public class DeathCapMushroomItem extends ArtifactAgilityItem{
    public DeathCapMushroomItem() {
        super(
                AgilityArtifactID.DEATH_CAP_MUSHROOM,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS
                        .get(AgilityArtifactID.DEATH_CAP_MUSHROOM).mcdar$getDurability()
        );
    }

    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        return CleanlinessHelper.mcdar$cleanUseWithOptionalStatus(
                user,
                hand,
                this,
                StatusEffects.DIG_SPEED,
                180,
                3,
                StatusEffects.MOVEMENT_SPEED,
                180,
                1,
                null,
                null,
                null);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }
}
