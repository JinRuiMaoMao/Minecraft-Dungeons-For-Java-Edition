package mcd_java.mcda.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

public class McdaEnchantmentHelper {
    public static int getBagOfSoulsLevel(Enchantment enchantment, PlayerEntity playerEntity){
        int totalLevel = 0;
        for (ItemStack itemStack : enchantment.getSlotItems(playerEntity).values())
            totalLevel += EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);

        return totalLevel;
    }
}
