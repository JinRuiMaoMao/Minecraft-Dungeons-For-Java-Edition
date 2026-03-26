package net.backupcup.mcde.screen.handler;

import I;
import java.util.Optional;
import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.block.ModBlocks;
import net.backupcup.mcde.util.EnchantmentSlot;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.backupcup.mcde.util.SlotPosition;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RunicTableScreenHandler extends AbstractContainerMenu {
    private final Container inventory = new SimpleContainer(1);
    private final ContainerLevelAccess context;
    public Container getInventory() {
        return inventory;
    }

    public RunicTableScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL);
    }

    public RunicTableScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModScreenHandlers.RUNIC_TABLE_SCREEN_HANDLER, syncId);

        checkContainerSize(inventory, 1);
        this.context = context;
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 132, 43) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem().isEnchantable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addSlotListener(EnchantmentUtils.generatorListener(context, playerInventory.player));
        inventory.setChanged();

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack itemStack = inventory.getItem(0);
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (slotsOptional.isEmpty()) {
            return clickMenuButton(player, id);
        }
        var slots = slotsOptional.get();
        var posAmount = SlotPosition.values().length;
        var clickedSlot = slots.getEnchantmentSlot(SlotPosition.values()[id / posAmount]).get();
        var chosen = clickedSlot.getChosen();
        int level = 1;
        ResourceLocation enchantmentId;
        if (chosen.isPresent()) {
            if (chosen.get().isMaxedOut()) {
                return super.clickMenuButton(player, id);
            }
            clickedSlot.upgrade();
            level = chosen.get().getLevel();
            enchantmentId = chosen.get().getEnchantmentId();
            if (!canEnchant(player, enchantmentId, level)) {
                return super.clickMenuButton(player, id);
            }
        } else {
            int choicePos = id % posAmount;
            enchantmentId = clickedSlot.getChoice(SlotPosition.values()[choicePos]).get();
            if (!canEnchant(player, enchantmentId, level)) {
                return super.clickMenuButton(player, id);
            }
            clickedSlot.setChosen(SlotPosition.values()[choicePos], level);
        }
        slots.updateItemStack(itemStack);
        if (clickedSlot.isMaxedOut()) {
            player.awardStat(Stats.ENCHANT_ITEM);
        }
        player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5f, 1f);
        if (!player.isCreative()) {
            player.giveExperienceLevels(-MCDEnchantments.getConfig().getEnchantCost(enchantmentId, level));
        }
        inventory.setChanged();
        context.execute((world, pos) -> {
            var server = world.getServer();
            var tracker = server.getPlayerList().getPlayer(player.getUUID()).getAdvancements();
            var advancement = server.getAdvancements().getAdvancement(ResourceLocation.tryBuild("minecraft", "story/enchant_item"));
            tracker.award(advancement, "enchanted_item");
        });
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(context, player, ModBlocks.RUNIC_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        context.execute((world, pos) -> {
            clearContainer(player, inventory);
        });
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 3 + l * 18, 84 + i * 19));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 3 + i * 18, 148));
        }
    }

    public static boolean canEnchant(Player player, ResourceLocation enchantmentId, int level) {
        if (player.isCreative()) {
            return true;
        }
        return player.experienceLevel >= MCDEnchantments.getConfig().getEnchantCost(enchantmentId, level);
    }
}
