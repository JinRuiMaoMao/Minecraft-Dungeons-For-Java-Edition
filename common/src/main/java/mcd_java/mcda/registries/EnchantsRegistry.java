package mcd_java.mcda.registries;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.enchants.ArmorEnchantment;
import mcd_java.mcda.enchants.EnchantID;
import mcd_java.mcda.enchants.enchantments.*;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import java.util.EnumMap;
import java.util.Locale;

public class EnchantsRegistry {
    public static final EnumMap<EnchantID, Enchantment> enchants = new EnumMap<>(EnchantID.class);

    public static void register() {
        for (EnchantID enchantID : EnchantID.values()) {

            if (!Mcda.CONFIG.mcdaEnableEnchantAndEffectConfig.enableEnchantment.get(enchantID))
                continue;

            Enchantment enchantment = switch (enchantID) {
                case BAG_OF_SOULS -> new BagOfSoulsEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case BURNING -> new BurningEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case CHILLING -> new ChillingEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case COWARDICE -> new CowardiceEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case DEATH_BARTER -> new DeathBarterEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case DEFLECT -> new DeflectEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case FIRE_FOCUS -> new FireFocusEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case FIRE_TRAIL -> new FireTrailEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET,
                        EquipmentSlot.FEET);
                case RECKLESS -> new RecklessEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case FOOD_RESERVES -> new FoodReservesEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case FRENZIED -> new FrenziedEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case HEAL_ALLIES -> new HealAlliesEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case LUCKY_EXPLORER -> new LuckyExplorerEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET,
                        EquipmentSlot.FEET);
                case POISON_FOCUS -> new PoisonFocusEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case POTION_BARRIER -> new PotionBarrierEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case RECYCLER -> new RecyclerEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case SNOWBALL -> new SnowballEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case SURPRISE_GIFT -> new SurpriseGiftEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR,
                        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                case SWIFTFOOTED -> new SwiftFootedEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR_FEET,
                        EquipmentSlot.FEET);
                //noinspection UnnecessaryDefault
                default -> new ArmorEnchantment(enchantID);
            };

            enchants.put(enchantID, enchantment);
            registerEnchant(enchantID.toString().toLowerCase(Locale.ROOT), enchantment);
        }
    }

    protected static void registerEnchant(String id, Enchantment enchant) {
        Registry.register(Registries.ENCHANTMENT, Mcda.ID(id), enchant);
    }
}