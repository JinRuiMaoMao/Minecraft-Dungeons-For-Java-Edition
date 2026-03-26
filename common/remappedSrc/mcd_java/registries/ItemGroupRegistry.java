package mcd_java.registries;

import mcd_java.Mcda;
import mcd_java.items.ArmorSets;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroupRegistry {
    public static final ResourceKey<CreativeModeTab> ARMOR = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcda.ID("armor"));

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ARMOR, FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.mcda.armor"))
                .icon(() -> {
                    if (Mcda.CONFIG.mcdaEnableArmorsConfig.ARMORS_SETS_ENABLED.get(ArmorSets.SPLENDID)) {
                        return new ItemStack(ArmorsRegistry.armorItems.get(ArmorSets.SPLENDID).get(ArmorItem.Type.CHESTPLATE));
                    }
                    return new ItemStack(ItemsRegistry.UPGRADE_CORE_ARCHER);
                })
                .build());
    }
}
