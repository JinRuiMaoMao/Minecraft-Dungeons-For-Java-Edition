package mcd_java.mcda.registries;

import mcd_java.mcda.Mcda;
import mcd_java.mcda.items.ArmorSets;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupRegistry {
    public static final RegistryKey<ItemGroup> ARMOR = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcda.ID("armor"));

    public static void register() {
        Registry.register(Registries.CREATIVE_MODE_TAB, ARMOR, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcda.armor"))
                .icon(() -> {
                    if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(ArmorSets.SPLENDID)) {
                        return new ItemStack(ArmorsRegistry.armorItems.get(ArmorSets.SPLENDID).get(ArmorItem.Type.CHESTPLATE));
                    }
                    return new ItemStack(ItemsRegistry.UPGRADE_CORE_ARCHER);
                })
                .build());
    }
}
