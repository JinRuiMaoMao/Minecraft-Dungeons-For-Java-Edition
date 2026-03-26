package jinrui.mcdar.api;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.enums.*;
import jinrui.mcdar.registries.EnchantsRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class CleanlinessHelper {

    @SuppressWarnings("deprecation")
    public static final net.minecraft.util.RandomSource RANDOM = RandomSource.createThreadSafe();

    public static void playCenteredSound(LivingEntity center, SoundEvent soundEvent, float volume, float pitch) {
        playCenteredSound(center, soundEvent, SoundSource.PLAYERS, volume, pitch);
    }

    public static void playCenteredSound(LivingEntity center, SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch) {
        center.level().playSound(null,
                center.getX(), center.getY(), center.getZ(),
                soundEvent, soundCategory,
                volume, pitch);
    }

    public static boolean percentToOccur (int chance) {
        return RANDOM.nextInt(100) + 1 <= chance;
    }

    public static void createLoreTTips(ItemStack stack, List<Component> tooltip) {
        String str = stack.getItem().getDescriptionId().toLowerCase(Locale.ROOT).substring(11);
        String translationKey = String.format("tooltip_info_item.mcdar.%s_", str);
        int i = 1;
        while (I18n.exists(translationKey + i)) {
            tooltip.add(Component.translatable(translationKey + i).withStyle(ChatFormatting.ITALIC));
            i++;
        }
    }

    public static void mcdar$dropItem(LivingEntity le, Item item) {
        mcdar$dropItem(le, item, 1);
    }

    public static void mcdar$dropItem(LivingEntity le, ItemStack itemStack) {
        ItemEntity it = new ItemEntity(
                le.level(), le.getX(), le.getY(), le.getZ(),
                itemStack);
        le.level().addFreshEntity(it);
    }

    public static void mcdar$dropItem(LivingEntity le, Item item, int amount) {
        mcdar$dropItem(le, new ItemStack(item, amount));
    }

    public static boolean isCoolingDown(Player player, Item item) {
        return player.getCooldowns().isOnCooldown(item);
    }

    public static InteractionResultHolder<ItemStack> mcdar$cleanUseWithOptionalStatus(
            Player player,
            InteractionHand hand,
            Item artifact,
            @Nullable MobEffect statusEffect,
            @Nullable Integer statusEffectDuration,
            @Nullable Integer statusEffectAmplifier,
            @Nullable MobEffect statusEffect1,
            @Nullable Integer statusEffectDuration1,
            @Nullable Integer statusEffectAmplifier1,
            @Nullable MobEffect statusEffect2,
            @Nullable Integer statusEffectDuration2,
            @Nullable Integer statusEffectAmplifier2
    ) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (statusEffect != null && statusEffectDuration != null && statusEffectAmplifier != null) {
            MobEffectInstance statusEffectInstance = new MobEffectInstance(statusEffect, statusEffectDuration, statusEffectAmplifier);
            player.addEffect(statusEffectInstance);
        }
        if (statusEffect1 != null && statusEffectDuration1 != null && statusEffectAmplifier1 != null) {
            MobEffectInstance statusEffectInstance1 = new MobEffectInstance(statusEffect1, statusEffectDuration1, statusEffectAmplifier1);
            player.addEffect(statusEffectInstance1);
        }
        if (statusEffect2 != null && statusEffectDuration2 != null && statusEffectAmplifier2 != null) {
            MobEffectInstance statusEffectInstance2 = new MobEffectInstance(statusEffect2, statusEffectDuration2, statusEffectAmplifier2);
            player.addEffect(statusEffectInstance2);
        }
        if (!player.isCreative())
            itemStack.hurtAndBreak(1, player, (entity) -> entity.broadcastBreakEvent(hand));
        McdarEnchantmentHelper.mcdar$cooldownHelper(player, artifact);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
    }

    public static InteractionResult mcdar$cleanUseSummon(
            UseOnContext itemUsageContext,
            Item artifact,
            EntityType<?> summon
    ) {
        if (itemUsageContext.getLevel() instanceof ServerLevel serverWorld) {
            Player itemUsageContextPlayer = itemUsageContext.getPlayer();
            if (itemUsageContextPlayer != null) {
                if (SummoningHelper.mcdar$summonSummonableEntity(
                        (LivingEntity) summon.create(serverWorld),
                        itemUsageContextPlayer,
                        itemUsageContext.getClickedPos())) {

                    if (!itemUsageContextPlayer.isCreative())
                        itemUsageContext.getItemInHand().hurtAndBreak(1, itemUsageContextPlayer,
                                (entity) -> entity.broadcastBreakEvent(itemUsageContext.getHand()));

                    McdarEnchantmentHelper.mcdar$cooldownHelper(
                            itemUsageContextPlayer,
                            artifact);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static int mcdar$artifactIDToItemCooldownTime(Item artifactItem) {
        int cooldownLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.COOLDOWN, artifactItem.getDefaultInstance());
        //if (artifactItem instanceof IArtifactItem) {
            for (AgilityArtifactID agilityArtifactID : AgilityArtifactID.values())
                if (artifactItem.asItem() == agilityArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS.get(agilityArtifactID)
                            .mcdar$getIsEnabled())
                    return Mcdar.CONFIG.mcdarArtifactsStatsConfig.AGILITY_ARTIFACT_STATS.get(agilityArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
            for (DamagingArtifactID damagingArtifactID : DamagingArtifactID.values())
                if (artifactItem.asItem() == damagingArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS.get(damagingArtifactID)
                            .mcdar$getIsEnabled())
                    return Mcdar.CONFIG.mcdarArtifactsStatsConfig.DAMAGING_ARTIFACT_STATS.get(damagingArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
            for (DefensiveArtifactID defensiveArtifactID : DefensiveArtifactID.values())
                if (artifactItem.asItem() == defensiveArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS.get(defensiveArtifactID)
                            .mcdar$getIsEnabled() && artifactItem.asItem() != DefensiveArtifactID.SOUL_HEALER.mcdar$getItem())
                    return Mcdar.CONFIG.mcdarArtifactsStatsConfig.DEFENSIVE_ARTIFACT_STATS.get(defensiveArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
            for (QuiverArtifactID quiverArtifactID : QuiverArtifactID.values())
                if (artifactItem.asItem() == quiverArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(quiverArtifactID)
                            .mcdar$getIsEnabled())
                    return (cooldownLevel + 1) * Mcdar.CONFIG.mcdarArtifactsStatsConfig.QUIVER_ARTIFACT_STATS.get(quiverArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
            for (StatusInflictingArtifactID statusInflictingArtifactID : StatusInflictingArtifactID.values())
                if (artifactItem.asItem() == statusInflictingArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS.get(statusInflictingArtifactID)
                            .mcdar$getIsEnabled())
                    return Mcdar.CONFIG.mcdarArtifactsStatsConfig.STATUS_INFLICTING_ARTIFACT_STATS.get(statusInflictingArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
            for (SummoningArtifactID summoningArtifactID : SummoningArtifactID.values())
                if (artifactItem.asItem() == summoningArtifactID.mcdar$getItem()
                        && Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS.get(summoningArtifactID)
                            .mcdar$getIsEnabled())
                    return Mcdar.CONFIG.mcdarArtifactsStatsConfig.SUMMONING_ARTIFACT_STATS.get(summoningArtifactID)
                            .mcdar$getMaxCooldownEnchantmentTime();
        //}
        return 0;
    }
}
