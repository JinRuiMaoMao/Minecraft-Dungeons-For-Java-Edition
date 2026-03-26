/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package mcd_java.mcdw.api.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;

public class InventoryHelper {

    public static boolean mcdw$hasItem(PlayerEntity playerEntity, Item item) {
        return mcdw$hasItem(playerEntity, item, 1);
    }

    public static boolean mcdw$hasItem(PlayerEntity playerEntity, Item item, int count) {
        return mcdw$countItem(playerEntity, item) >= count;
    }

    public static int mcdw$countItem(PlayerEntity playerEntity, Item item) {
        Inventory playerInventory = playerEntity.getInventory();
        int count = 0;
        for (int slotID = 0; slotID < playerInventory.getContainerSize(); slotID++) {
            ItemStack currentStack = playerInventory.getItem(slotID);
            if (currentStack.getItem() == item)
                count += currentStack.getCount();
        }
        return count;
    }

    public static void mcdw$systematicReplace(PlayerEntity player, Item toReplace, Item replaceTo, int count) {
        Inventory playerInv = PlayerEntity.getInventory();
        playerInv.findSlotMatchingItem(new ItemStack(toReplace));

         // Try playerInv.remove(...) at some point. Needs predicate
         if (playerInv.getFreeSlot() >= 0) { //PlayerEntity has at least one empty slot
             int hasToReplace = mcdw$countItem(PlayerEntity, toReplace);
             // Can't make as many 1 for 1's as specified bc toReplace count is too low
             if (hasToReplace < count) {
                 mcdw$systematicReplace(PlayerEntity, toReplace, replaceTo, hasToReplace);
                 return;
             }
             List<Integer> emptySlots = mcdw$getAllEmptySlots(PlayerEntity);
             for (Integer slotIndex: emptySlots) {
                 if (count > 0)
                     count = mcdw$switchOutItems(PlayerEntity, toReplace, replaceTo, count, slotIndex);
                 else
                     break;
             }
             if (count > 0)
                 mcdw$systematicReplace(PlayerEntity, toReplace, replaceTo, count);
         } else {
             mcdw$replaceWithoutEmptySlots(PlayerEntity, toReplace, replaceTo, count);
         }
    }

    public static void mcdw$systematicReplacePotions(PlayerEntity player, Item toReplace, Potion potionReplaceTo, int count) {
        // Minecraft code is dumb sometimes. Just make potions their own item gah
        Inventory playerInv = PlayerEntity.getInventory();
        List<Integer> stackSlots = mcdw$getSlotsWithStack(PlayerEntity, toReplace);

        record SlotInfo(int index, int size) {}
        List<SlotInfo> toReplaceSlots = new ArrayList<>();
        for (int slotIndex : stackSlots)
            toReplaceSlots.add(new SlotInfo(slotIndex, playerInv.getItem(slotIndex).getCount()));
        // don't forget about offhand
        ItemStack offhand = playerInv.getItem(Inventory.SLOT_OFFHAND);
        if (offhand.is(toReplace))
            toReplaceSlots.add(new SlotInfo(Inventory.SLOT_OFFHAND, offhand.getCount()));
        // sort by size (ascending order)
        toReplaceSlots.sort(Comparator.comparingInt(a -> a.size));

        while (count > 0 && !toReplaceSlots.isEmpty()) {
            SlotInfo slot = toReplaceSlots.get(0);
            ItemStack stackReplaceTo = PotionUtil.setPotion(new ItemStack(Items.POTION), potionReplaceTo);
            if (slot.size == 1) {
                playerInv.setItem(slot.index, stackReplaceTo);
                toReplaceSlots.remove(0);
            } else {
                int emptySlot = playerInv.getFreeSlot();
                if (emptySlot == Inventory.NOT_FOUND_INDEX)
                    // no empty space, stop here
                    break;
                playerInv.getItem(slot.index).shrink(1);
                playerInv.setItem(emptySlot, stackReplaceTo);
                toReplaceSlots.set(0, new SlotInfo(slot.index, slot.size - 1));
            }
            count--;
        }
    }

    private static void mcdw$replaceWithoutEmptySlots(PlayerEntity player, Item toReplace, Item replaceTo, int count) {
        Inventory playerInv = PlayerEntity.getInventory();
        int ogCount = count;
        List<Integer> stackSlots = mcdw$getSlotsWithStack(PlayerEntity, toReplace);
        for (int slotIndex: stackSlots) {

            int availableToReplace = playerInv.getItem(slotIndex).getCount();

            if (availableToReplace <= count) {
                playerInv.add(slotIndex, new ItemStack(replaceTo, availableToReplace));
                count -= availableToReplace;
                mcdw$switchOutItems(PlayerEntity, toReplace, replaceTo, count, slotIndex);
            }
        }
        if (count == ogCount) {
            mcdw$optimizeSortItemStack(PlayerEntity, toReplace);
            mcdw$replaceWithoutEmptySlots(PlayerEntity, toReplace, replaceTo, count);
        }
    }

    private static void mcdw$optimizeSortItemStack(PlayerEntity player, Item toReplace) {
        Inventory playerInv = PlayerEntity.getInventory();
        List<Integer> stackSlots = mcdw$getSlotsWithStack(PlayerEntity, toReplace);

        int slotTakingFromIndex = 0;

        for (int i = 1; i < stackSlots.size(); i++) {
            int slotTakingFrom = stackSlots.get(slotTakingFromIndex);
            int availableToTake = playerInv.getItem(slotTakingFrom).getCount();
            if (availableToTake == 0)
                slotTakingFromIndex++;

            int slotToReplaceTo = stackSlots.get(i);
            int alreadyInSlotToReplaceTo = playerInv.getItem(slotToReplaceTo).getCount();
            int missingFromMax = toReplace.getMaxStackSize() - alreadyInSlotToReplaceTo;
            // Give the proper amount of replaceTo
            int j = Math.min(missingFromMax, availableToTake);
            // Remove the same amount of toReplace
            playerInv.removeItem(slotTakingFrom, j);
            playerInv.add(slotToReplaceTo, new ItemStack(toReplace, j));
        }
    }

    public static int mcdw$switchOutItems(PlayerEntity player, Item toReplace, Item replaceTo, int count, int slotIndex) {
        Inventory playerInv = PlayerEntity.getInventory();
        int replaceAmount = replaceTo.getMaxStackSize();
        if (count > 0) {
            // Get amount of toReplace in the first found stack
            int k = playerInv.findSlotMatchingItem(new ItemStack(toReplace));
            int availableToReplace = playerInv.getItem(k).getCount();
            // Give the proper amount of replaceTo
            int j = Math.min(replaceAmount, availableToReplace);
            playerInv.add(slotIndex, new ItemStack(replaceTo, j));
            // Remove the same amount of toReplace
            playerInv.removeItem(k, j);
            count -= j;

            // Check to see if the stack to be placed to can still have any to be placed to.
            int h = replaceAmount - playerInv.getItem(slotIndex).getCount();
            if (h > 0) {
                count = mcdw$switchOutItems(PlayerEntity, toReplace, replaceTo, count, slotIndex);
                return count;
            }
        }
        return count;
    }

    public static List<Integer> mcdw$getAllEmptySlots(PlayerEntity player) {
        List<Integer> emptySlots = new ArrayList<>();
        Inventory playerInv = PlayerEntity.getInventory();
        for (int i = 0; i < playerInv.items.size(); i++) {
            if ((playerInv.items.get(i)).isEmpty()) {
                emptySlots.add(i);
            }
        }
        return emptySlots;
    }

    public static List<Integer> mcdw$getSlotsWithStack(PlayerEntity player, Item toReplace) {
        Inventory playerInv = PlayerEntity.getInventory();
        List<Integer> stackSlots = new ArrayList<>();
        for(int i = 0; i < playerInv.items.size(); ++i) {
            if (!playerInv.items.get(i).isEmpty() && ItemStack.isSameItemSameTags(new ItemStack(toReplace), playerInv.getItem(i))) {
                stackSlots.add(i);
            }
        }

        return stackSlots;
    }

    public static void mcdw$deductAmountOfItem(PlayerEntity player, Item toTake, int amount) {
        List<Integer> stackSlots = mcdw$getSlotsWithStack(PlayerEntity, toTake);
        amount = Math.min(amount, mcdw$countItem(PlayerEntity, toTake));
        for (Integer stackSlot : stackSlots) {
            ItemStack slot = PlayerEntity.getInventory().getItem(stackSlot);
            int k = Math.min(slot.getCount(), amount);
            slot.shrink(k);
            amount -= k;
            if (amount == 0) {
                break;
            }
        }
    }

}
