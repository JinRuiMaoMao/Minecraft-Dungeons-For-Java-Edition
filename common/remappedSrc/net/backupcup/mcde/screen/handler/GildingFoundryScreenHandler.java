package net.backupcup.mcde.screen.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.block.ModBlocks;
import net.backupcup.mcde.block.entity.GildingFoundryBlockEntity;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class GildingFoundryScreenHandler extends AbstractContainerMenu implements ContainerListener {
    public static final ResourceLocation GILDING_PACKET = ResourceLocation.tryBuild(MCDEnchantments.MOD_ID, "gilding");
    private final Container inventory;
    private final Player playerEntity;
    private final ContainerLevelAccess context;
    private final ContainerData propertyDelegate;
    private Optional<ResourceLocation> generatedEnchantment = Optional.empty();

    public Optional<ResourceLocation> getGeneratedEnchantment() {
        return generatedEnchantment;
    }

    public Container getInventory() {
        return inventory;
    }

    public boolean hasEnchantmentForGilding() {
        return generatedEnchantment.isPresent();
    }

    public GildingFoundryScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf buf) {
        this(
            syncId,
            inventory,
            new SimpleContainer(2),
            new SimpleContainerData(1),
            ContainerLevelAccess.NULL,
            buf.readOptional(r -> r.readResourceLocation())
        );
    }

    public GildingFoundryScreenHandler(
        int syncId,
        Inventory playerInventory,
        Container inventory,
        ContainerData delegate,
        ContainerLevelAccess context,
        Optional<ResourceLocation> generatedEnchantment
    ) {
        super(ModScreenHandlers.GILDING_FOUNDRY_SCREEN_HANDLER, syncId);
        this.context = context;
        this.playerEntity = playerInventory.player;
        checkContainerSize(inventory, 2);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.player);
        this.propertyDelegate = delegate;
        this.generatedEnchantment = generatedEnchantment;

        this.addSlot(new Slot(inventory, 0, 74, 9) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem().isEnchantable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(inventory, 1, 74, 37) {

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GOLD_INGOT) || stack .is(Items.EMERALD);
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        addSlotListener(EnchantmentUtils.generatorListener(context, playerInventory.player));
        addSlotListener(this);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(delegate);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (generatedEnchantment.isEmpty()) {
            return false;
        }
        var weaponStack = player.isCreative() ? inventory.getItem(0) : inventory.getItem(0).copy();
        var enchantmentId = generatedEnchantment.get();
        EnchantmentSlots.fromItemStack(weaponStack).ifPresent(slots -> {
            if (slots.hasGilding()) {
                var map = EnchantmentHelper.getEnchantments(weaponStack);
                map.keySet().removeAll(slots.getGildingEnchantments());
                EnchantmentHelper.setEnchantments(map, weaponStack);
                slots.removeAllGildings();
            }
            slots.addGilding(enchantmentId);
            slots.updateItemStack(weaponStack);
        });
        setNewEnchantment(player, weaponStack);

        if (!player.isCreative()) {
            context.execute((world, pos) -> {
                ((GildingFoundryBlockEntity)world.getBlockEntity(pos))
                    .setGenerated(enchantmentId);
            });
            startProgress();
        }
        return false;
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

    public int getProgress() {
        return propertyDelegate.get(0);
    }

    public void startProgress() {
        propertyDelegate.set(0, 1);
    }

    public boolean hasProgress() {
        return getProgress() != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(context, player, ModBlocks.GILDING_FOUNDRY);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 2 + l * 18, 80 + i * 19));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 2 + i * 18, 144));
        }
    }

    public static List<ResourceLocation> getCandidatesForGidling(ItemStack itemStack) {
        return EnchantmentUtils.getEnchantmentsForItem(itemStack).collect(Collectors.toList());
    }

    public List<ResourceLocation> getCandidatesForGidling() {
        return getCandidatesForGidling(inventory.getItem(0));
    }

    public void setNewEnchantment(Player player, ItemStack weaponStack) {
        context.execute((world, pos) -> {
            ServerPlayer serverPlayer = world.getServer().getPlayerList().getPlayer(player.getUUID());
            generatedEnchantment = EnchantmentUtils.generateEnchantment(
                weaponStack,
                Optional.of(serverPlayer),
                getCandidatesForGidling()
            );
            var buffer = PacketByteBufs.create();
            buffer.writeInt(containerId);
            buffer.writeOptional(generatedEnchantment, (buf, e) -> buf.writeResourceLocation(e));
            ServerPlayNetworking.send(serverPlayer, GILDING_PACKET, buffer);
        });
    }

    public static void receiveNewEnchantment(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf,
            PacketSender responseSender) {
        var player = client.player;
        if (player == null) {
            return;
        }

        var screenHandler = player.containerMenu;
        if (screenHandler == null) {
            return;
        }
        int syncId = buf.readInt();

        if (screenHandler.containerId != syncId) {
            return;
        }

        if (screenHandler instanceof GildingFoundryScreenHandler gfScreenHandler) {
            gfScreenHandler.generatedEnchantment = buf.readOptional(b -> b.readResourceLocation());
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu handler, int property, int value) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
        if (slotId != 0 || EnchantmentSlots.fromItemStack(stack).isEmpty()) {
            return;
        }

        setNewEnchantment(playerEntity, stack);
    }

}
