package jinrui.mcdar.enchants;

import jinrui.mcdar.Mcdar;
import jinrui.mcdar.registries.ItemGroupRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class BeastBossEnchantment extends Enchantment {

    public BeastBossEnchantment(Rarity rarity, EnchantmentCategory enchantmentTarget, EquipmentSlot[] equipmentSlots) {
        super(rarity, enchantmentTarget, equipmentSlots);
        if (Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsEnabled()) {
            Registry.register(BuiltInRegistries.ENCHANTMENT, Mcdar.ID("beast_boss"), this);
            ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ARTIFACTS).register(entries -> {
                // For loop creates first 3 levels of enchanted books
                for (int i = 1; i <= getMaxLevel(); i++)
                    entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(this, i)));
            });
        }
    }

    @Override
    public int getMaxLevel(){
        return Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getMaxLevel();
    }

    @Override
    public boolean isDiscoverable() {
        return Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsEnabled()
                && Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsAvailableForRandomSelection();
    }

    @Override
    public boolean isTradeable() {
        return Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsEnabled()
                && Mcdar.CONFIG.mcdarEnchantmentsConfig.ENCHANTMENT_CONFIG.get(EnchantmentsID.BEAST_BOSS).mcdar$getIsAvailableForEnchantedBookOffer();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

}
