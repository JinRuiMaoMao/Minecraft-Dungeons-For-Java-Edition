package jinrui.mcdar.registries;

import jinrui.mcdar.enchants.BeastBossEnchantment;
import jinrui.mcdar.enchants.BeastBurstEnchantment;
import jinrui.mcdar.enchants.BeastSurgeEnchantment;
import jinrui.mcdar.enchants.CooldownEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.enchantment.Enchantment;

public class EnchantsRegistry {
    public static Enchantment COOLDOWN;
    public static Enchantment BEAST_BOSS;
    public static Enchantment BEAST_BURST;
    public static Enchantment BEAST_SURGE;
    public static void register(){
        COOLDOWN = new CooldownEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
        BEAST_BOSS = new BeastBossEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
        BEAST_BURST = new BeastBurstEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
        BEAST_SURGE = new BeastSurgeEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.WEAPON,
                new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }
}
