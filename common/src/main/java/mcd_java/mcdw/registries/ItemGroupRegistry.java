/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.registries;

import mcd_java.mcdw.Mcdw;
import mcd_java.mcdw.enums.LongbowsID;
import mcd_java.mcdw.enums.ShieldsID;
import mcd_java.mcdw.enums.SwordsID;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemGroupRegistry {
    public static final RegistryKey<ItemGroup> MELEE = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdw.ID("weapons/melee"));
    public static final RegistryKey<ItemGroup> RANGED = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdw.ID("weapons/ranged"));
    public static final RegistryKey<ItemGroup> SHIELDS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdw.ID("weapons/shields"));
    public static final RegistryKey<ItemGroup> ENCHANTMENTS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Mcdw.ID("enchantments"));

    public static void register() {
        Registry.register(Registries.CREATIVE_MODE_TAB, MELEE, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcdw.weapons/melee"))
                .icon(() -> {
                    if(SwordsID.SWORD_HEARTSTEALER.getItem() != null) {
                        return new ItemStack(SwordsID.SWORD_HEARTSTEALER.getItem());
                    }
                    return new ItemStack(Items.IRON_SWORD);
                })
                .build());
        Registry.register(Registries.CREATIVE_MODE_TAB, RANGED, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcdw.weapons/ranged"))
                .icon(() -> {
                    if(LongbowsID.BOW_LONGBOW.getItem() != null) {
                        return new ItemStack(LongbowsID.BOW_LONGBOW.getItem());
                    }
                    return new ItemStack(Items.BOW);
                })
                .build());
        Registry.register(Registries.CREATIVE_MODE_TAB, SHIELDS, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcdw.weapons/shields"))
                .icon(() -> {
                    if (ShieldsID.SHIELD_ROYAL_GUARD.getItem() != null) {
                        return new ItemStack(ShieldsID.SHIELD_ROYAL_GUARD.getItem());
                    }
                    return new ItemStack(Items.SHIELD);
                })
                .build());
        Registry.register(Registries.CREATIVE_MODE_TAB, ENCHANTMENTS, FabricItemGroup.builder()
                .title(Text.translatable("itemGroup.mcdw.enchantments"))
                .icon(() -> new ItemStack(Items.ENCHANTED_BOOK))
                .build());
    }
}
