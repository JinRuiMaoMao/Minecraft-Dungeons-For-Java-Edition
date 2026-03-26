/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.client;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.bases.*;
import mcd_java.mcdw.enchants.summons.render.SummonedBeeRenderer;
import mcd_java.mcdw.enums.*;
import mcd_java.mcdw.registries.EnchantsRegistry;
import mcd_java.mcdw.registries.ParticlesRegistry;
import mcd_java.mcdw.registries.StatusEffectsRegistry;
import mcd_java.mcdw.registries.SummonedEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class McdwClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(SummonedEntityRegistry.SUMMONED_BEE_ENTITY, SummonedBeeRenderer::new);
        ParticlesRegistry.registerOnClient();

        Arrays.stream(BowsID.values()).forEach(bowsID -> registerRangedWeaponPredicates(bowsID.getItem()));
        Arrays.stream(ShortbowsID.values()).forEach(shortBowsID -> registerRangedWeaponPredicates(shortBowsID.getItem()));
        Arrays.stream(LongbowsID.values()).forEach(longBowsID -> registerRangedWeaponPredicates(longBowsID.getItem()));
        Arrays.stream(CrossbowsID.values()).forEach(crossbowsID -> registerRangedWeaponPredicates(crossbowsID.getItem()));
        Arrays.stream(ShieldsID.values()).forEach(shieldsID -> registerShieldPredicates(shieldsID.getItem()));
    }

    public static void registerRangedWeaponPredicates(Item item) {
        ItemProperties.register(item, new ResourceLocation("pull"), (itemStack, clientWorld, livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            } else if (item instanceof McdwBow bow) {
                return calculateDrawSpeed(itemStack, livingEntity, bow.getDrawSpeed());
            } else if (item instanceof McdwShortbow shortbow) {
                return calculateDrawSpeed(itemStack, livingEntity, shortbow.getDrawSpeed());
            } else if (item instanceof McdwLongbow longbow) {
                return calculateDrawSpeed(itemStack, livingEntity, longbow.getDrawSpeed());
            } else if (item instanceof McdwCrossbow crossbow) {
                return calculateDrawSpeed(itemStack, livingEntity, crossbow.getDrawSpeed());
            }
            return 0.0F;
        });

        if (item instanceof BowItem) {
            ItemProperties.register(item, new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, seed) ->
                    livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
        }
        if (item instanceof CrossbowItem) {
            ItemProperties.register(item, new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, seed) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && !McdwCrossbow.isCharged(itemStack) ? 1.0F : 0.0F;
                }
            });

            ItemProperties.register(item, new ResourceLocation("charged"), (itemStack, clientWorld, livingEntity, seed) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return McdwCrossbow.isCharged(itemStack) ? 1.0F : 0.0F;
                }
            });

            ItemProperties.register(item, new ResourceLocation("firework"), (itemStack, clientWorld, livingEntity, seed) -> {
                if (livingEntity == null) {
                    return 0.0F;
                } else {
                    return McdwCrossbow.isCharged(itemStack) && McdwCrossbow.containsChargedProjectile(itemStack,
                            Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
                }
            });
        }
    }
    public static void registerShieldPredicates(McdwShield shield) {
        ItemProperties.register(shield, new ResourceLocation("blocking"),
                (itemStack, clientWorld, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() &&
                        livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F
        );
    }
    private static float calculateDrawSpeed(ItemStack itemStack, LivingEntity livingEntity, float drawSpeed) {
        int useTicks = itemStack.getUseDuration() - livingEntity.getUseItemRemainingTicks();
        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.ACCELERATE).mcdw$getIsEnabled()) {
            int accelerateLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ACCELERATE), itemStack);
            if (accelerateLevel > 0) {
                MobEffectInstance accelerateInstance = livingEntity.getEffect(StatusEffectsRegistry.ACCELERATE);
                int consecutiveShots = accelerateInstance != null ? accelerateInstance.getAmplifier() + 1 : 0;

                useTicks = (int) (useTicks * (1f + (Mth.clamp(consecutiveShots * (6.0f + 2.0f * accelerateLevel), 0f, 100f) / 100f)));
            }
        }
        if (Mcdw.CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.OVERCHARGE).mcdw$getIsEnabled()) {
            int overchargeLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.OVERCHARGE), itemStack);
            if (overchargeLevel > 0) {
                int overcharge = (int) Math.min((useTicks / drawSpeed) - 1, overchargeLevel);
                useTicks = overcharge == overchargeLevel ? useTicks : (int) (useTicks % drawSpeed);
            }
        }
        if (itemStack.getItem() instanceof BowItem)
            return livingEntity.getUseItem() != itemStack ? 0.0F : (float) useTicks / drawSpeed;
        if (itemStack.getItem() instanceof McdwCrossbow)
            return McdwCrossbow.isCharged(itemStack) ? 0.0F : (float) useTicks / (float) McdwCrossbow.getChargeDuration(itemStack);
        return drawSpeed;
    }
}
