package jinrui.mcdar.artifacts;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.api.AOEHelper;
import jinrui.mcdar.api.AbilityHelper;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LoveMedallionItem extends ArtifactStatusInflictingItem {
    public LoveMedallionItem() {
        super(
                StatusInflictingArtifactID.LOVE_MEDALLION,
                Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS
                        .get(StatusInflictingArtifactID.LOVE_MEDALLION).mcdar$getDurability()
        );
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand){
        ItemStack itemStack = user.getItemInHand(hand);

        int i = 0;

        for (LivingEntity entitiesByPredicate : AOEHelper.getEntitiesByPredicate(Mob.class, user, 6,
                (nearbyEntity) -> AbilityHelper.isAoeTarget(nearbyEntity, user, user))) {
            sendIntoWildRage(entitiesByPredicate);
            i++;
            if (i >= 3)
                break;
        }

        if (!user.isCreative())
            itemStack.hurtAndBreak(1, user, (entity) -> entity.broadcastBreakEvent(hand));

        McdarEnchantmentHelper.mcdar$cooldownHelper(
                user,
                this
        );

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    public static void sendIntoWildRage(LivingEntity mobEntity) {
        boolean bl = false;
        try {
            mobEntity.getAttributeValue(Attributes.ATTACK_DAMAGE);
        } catch (IllegalArgumentException e) {
            bl = true;
        }
        if (!(bl || mobEntity instanceof WitherBoss || mobEntity instanceof EnderDragon || mobEntity instanceof AmbientCreature))
            mobEntity.addEffect(new MobEffectInstance(StatusEffectInit.CHARMED, 200, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag tooltipContext){
        CleanlinessHelper.createLoreTTips(stack, tooltip);
    }

}

/*
3 nearby mobs give off heart particles, become allies for 10 seconds, then die

// Abridged
3 mobs within 6 radius are afflicted with wild rage for 10 seconds as is in mcdw
 */