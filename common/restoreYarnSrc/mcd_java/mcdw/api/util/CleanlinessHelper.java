/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import mcd_java.mcdw.api.interfaces.IMcdwEnchantedArrow;
import mcd_java.mcdw.api.interfaces.IOffhandAttack;
import mcd_java.mcdw.bases.McdwLongbow;
import mcd_java.mcdw.bases.McdwShortbow;
import mcd_java.mcdw.configs.CompatibilityFlags;
import mcd_java.mcdw.enums.EnchantmentsID;
import mcd_java.mcdw.enums.SettingsID;
import mcd_java.mcdw.registries.EnchantsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static mcd_java.mcdw.Mcdw.CONFIG;

public class CleanlinessHelper {
    @SuppressWarnings("deprecation")
    public static final RandomSource random = RandomSource.createThreadSafe();

    public static boolean percentToOccur (int chance) {
        return random.nextInt(100) < chance;
    }

    public static boolean canRepairCheck(String[] repairIngredient, ItemStack ingredient) {
        List<Item> potentialIngredients = new ArrayList<>(List.of());
        AtomicBoolean isWood = new AtomicBoolean(false);
        AtomicBoolean isStone = new AtomicBoolean(false);
        if (repairIngredient != null && repairIngredient.length > 0) {
            Arrays.stream(repairIngredient).toList().forEach(repIngredient -> {
                if (repIngredient != null) {
                    if (repIngredient.contentEquals("minecraft:planks"))
                        isWood.set(true);
                    else if (repIngredient.contentEquals("minecraft:stone_crafting_materials"))
                        isStone.set(true);
                    potentialIngredients.add(
                            BuiltInRegistries.ITEM.get(new ResourceLocation(repIngredient)));
                }
            });
        }

        return potentialIngredients.contains(ingredient.getItem())
                || (isWood.get() && ingredient.is(ItemTags.PLANKS)
                || (isStone.get() && ingredient.is(ItemTags.STONE_CRAFTING_MATERIALS)));
    }

    public static void playCenteredSound (LivingEntity center, SoundEvent soundEvent, float volume, float pitch) {
        playCenteredSound(center, soundEvent, SoundSource.PLAYERS, volume, pitch);
    }

    public static void playCenteredSound (LivingEntity center, SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch) {
        center.level().playSound(null,
                center.getX(), center.getY(), center.getZ(),
                soundEvent, soundCategory,
                volume, pitch);
    }

    public static void mcdw$dropItem(LivingEntity le, Item item) {
        mcdw$dropItem(le, item, 1);
    }

    public static void mcdw$dropItem(LivingEntity le, ItemStack itemStack) {
        ItemEntity it = new ItemEntity(
                le.level(), le.getX(), le.getY(), le.getZ(),
                itemStack);
        le.level().addFreshEntity(it);
    }

    public static void mcdw$dropItem(LivingEntity le, Item item, int amount) {
        mcdw$dropItem(le, new ItemStack(item, amount));
    }

    public static void mcdw$tooltipHelper(ItemStack stack, List<Component> tooltip, int subStringIndex) {
        int i = 1;
        String str = stack.getItem().getDescriptionId().toLowerCase(Locale.ROOT).substring(subStringIndex);
        String translationKey = String.format("tooltip_info_item.mcdw.%s_", str);
        while (I18n.exists(translationKey + i)) {
            tooltip.add(Component.translatable(translationKey + i).withStyle(ChatFormatting.ITALIC));
            i++;
        }
        if (stack.getItem() instanceof IOffhandAttack) {
            if (CompatibilityFlags.noOffhandConflicts) {
                tooltip.add(Component.translatable("tooltip_info_item.mcdw.gap").withStyle(ChatFormatting.ITALIC));
                tooltip.add(Component.translatable("tooltip_note_item.mcdw.dualwield").withStyle(ChatFormatting.GREEN));
            }
        }
        if (stack.getItem() instanceof McdwShortbow) {
            tooltip.add(Component.translatable("tooltip_info_item.mcdw.gap").withStyle(ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("tooltip_note_item.mcdw.shortbow").withStyle(ChatFormatting.GREEN));
        }
        if (stack.getItem() instanceof McdwLongbow) {
            tooltip.add(Component.translatable("tooltip_info_item.mcdw.gap").withStyle(ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("tooltip_note_item.mcdw.longbow").withStyle(ChatFormatting.GREEN));
        }

    }

    public static void addPPEEnchantments(ItemStack itemStack, IMcdwEnchantedArrow ppe) {
        int chainReactionLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.CHAIN_REACTION), itemStack);
        if (chainReactionLevel > 0) {
            ppe.mcdw$setChainReactionLevel(chainReactionLevel);
        }
        int chargeLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.CHARGE), itemStack);
        if (chargeLevel > 0) {
            ppe.mcdw$setChargeLevel(chargeLevel);
        }
        int cobwebShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.COBWEB_SHOT), itemStack);
        if (cobwebShotLevel > 0) {
            ppe.mcdw$setCobwebShotLevel(cobwebShotLevel);
        }
        int dynamoLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.DYNAMO), itemStack);
        if (dynamoLevel > 0) {
            ppe.mcdw$setDynamoLevel(dynamoLevel);
        }
        int enigmaResonatorLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.ENIGMA_RESONATOR), itemStack);
        if (enigmaResonatorLevel > 0) {
            ppe.mcdw$setEnigmaResonatorLevel(enigmaResonatorLevel);
        }
        int fuseShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.FUSE_SHOT), itemStack);
        if (fuseShotLevel > 0) {
            ppe.mcdw$setFuseShotLevel(fuseShotLevel);
        }
        int freezingLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.FREEZING), itemStack);
        if (freezingLevel > 0) {
            ppe.mcdw$setFreezingLevel(freezingLevel);
        }
        int gravityLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.GRAVITY), itemStack);
        if (gravityLevel > 0) {
            ppe.mcdw$setGravityLevel(gravityLevel);
        }
        int growingLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.GROWING), itemStack);
        if (growingLevel > 0) {
            ppe.mcdw$setGrowingLevel(growingLevel);
        }
        int levitationShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.LEVITATION_SHOT), itemStack);
        if (levitationShotLevel > 0) {
            ppe.mcdw$setLevitationShotLevel(levitationShotLevel);
        }
        int phantomsMarkLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.PHANTOMS_MARK), itemStack);
        if (phantomsMarkLevel > 0) {
            ppe.mcdw$setPhantomsMarkLevel(phantomsMarkLevel);
        }
        int poisonCloudLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.POISON_CLOUD), itemStack);
        if (poisonCloudLevel > 0) {
            ppe.mcdw$setPoisonCloudLevel(poisonCloudLevel);
        }
        int radianceLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.RADIANCE), itemStack);
        if (radianceLevel > 0) {
            ppe.mcdw$setRadianceLevel(radianceLevel);
        }
        int replenishLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.REPLENISH), itemStack);
        if (replenishLevel > 0) {
            ppe.mcdw$setReplenishLevel(replenishLevel);
        }
        int ricochetLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.RICOCHET), itemStack);
        if (ricochetLevel > 0) {
            ppe.mcdw$setRicochetLevel(ricochetLevel);
        }
        int shadowShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.SHADOW_SHOT), itemStack);
        if (shadowShotLevel > 0) {
            ppe.mcdw$setShadowShotLevel(shadowShotLevel);
        }
        int tempoTheftLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.TEMPO_THEFT), itemStack);
        if (tempoTheftLevel > 0) {
            ppe.mcdw$setTempoTheftLevel(tempoTheftLevel);
        }
        int thunderingLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.THUNDERING), itemStack);
        if (thunderingLevel > 0) {
            ppe.mcdw$setThunderingLevel(thunderingLevel);
        }
        int voidShotLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.VOID_SHOT), itemStack);
        if (voidShotLevel > 0) {
            ppe.mcdw$setVoidShotLevel(voidShotLevel);
        }
        int wildRageLevel = EnchantmentHelper.getItemEnchantmentLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.WILD_RAGE), itemStack);
        if (wildRageLevel > 0){
            ppe.mcdw$setWildRageLevel(wildRageLevel);
        }
    }

    public static Map<Enchantment, Integer> mcdw$checkInnateEnchantmentEnabled(int level, Object... enchantments) {
        LinkedHashMap<Enchantment, Integer> enchantmentIntegerLinkedHashMap = new LinkedHashMap<>();
        for (Object enchantment : enchantments) {
            if (enchantment instanceof EnchantmentsID id) {
                if (CONFIG.mcdwEnchantmentsConfig.ENCHANTMENT_CONFIG.get(id).mcdw$getIsEnabled()
                        && CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.ENABLE_INNATE_ENCHANTMENTS)) {
                    enchantmentIntegerLinkedHashMap.put(EnchantsRegistry.enchantments.get(id), level);
                }
            } else if (enchantment instanceof Enchantment vanillaEnchantment
                    && CONFIG.mcdwEnchantmentSettingsConfig.ENABLE_ENCHANTMENT_SETTINGS.get(SettingsID.ENABLE_INNATE_ENCHANTMENTS)) {
                enchantmentIntegerLinkedHashMap.put(vanillaEnchantment, level);
            }
        }
        return enchantmentIntegerLinkedHashMap;
    }

    public static String materialToString(Tier toolMaterial) {
        if (toolMaterial == Tiers.WOOD)
            return "wood";
        else if (toolMaterial == Tiers.STONE)
            return "stone";
        else if (toolMaterial == Tiers.GOLD)
            return "gold";
        else if (toolMaterial == Tiers.IRON)
            return "iron";
        else if (toolMaterial == Tiers.DIAMOND)
            return "diamond";
        else if (toolMaterial == Tiers.NETHERITE)
            return "netherite";
        else
            return "none";
    }

    public static Tier stringToMaterial(String material) {
        return switch (material) {
            case "wood" -> Tiers.WOOD;
            case "stone" -> Tiers.STONE;
            case "gold" -> Tiers.GOLD;
            case "diamond" -> Tiers.DIAMOND;
            case "netherite" -> Tiers.NETHERITE;
            default -> Tiers.IRON;
        };
    }
}
