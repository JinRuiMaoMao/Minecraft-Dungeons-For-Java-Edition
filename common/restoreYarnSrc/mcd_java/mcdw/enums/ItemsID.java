/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.enums;

import mcd_java.mcdw.registries.ItemsRegistry;
import net.minecraft.world.item.Item;
import java.util.EnumMap;

public enum ItemsID {
    ITEM_BEE_STINGER;

    @SuppressWarnings("SameReturnValue")
    public static EnumMap<ItemsID, Item> getItemsEnum() {
        return ItemsRegistry.MCDW_ITEMS;
    }

    public Item makeItem(Item.Properties settings) {
        Item item = new Item(settings);
        getItemsEnum().put(this, item);
        return item;
    }

    public Item getItem() {
        return getItemsEnum().get(this);
    }
}
