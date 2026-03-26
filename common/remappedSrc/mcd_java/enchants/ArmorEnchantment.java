package mcd_java.enchants;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ArmorEnchantment  extends Enchantment {
    public final EnchantID id;

    public ArmorEnchantment(EnchantID id) {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
        this.id = id;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
