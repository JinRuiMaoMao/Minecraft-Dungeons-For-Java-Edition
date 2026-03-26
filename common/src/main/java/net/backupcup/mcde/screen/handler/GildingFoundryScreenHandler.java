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
import net.minecraft.client.PlayerEntity.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AbstractContainerMenu;
import net.minecraft.screen.ContainerData;
import net.minecraft.screen.ContainerLevelAccess;
import net.minecraft.screen.ContainerListener;
import net.minecraft.screen.SimpleContainerData;
import net.minecraft.screen.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;

public class GildingFoundryScreenHandler extends AbstractContainerMenu implements ContainerListener {
    public static final Identifier GILDING_PACKET = Identifier.tryBuild(MCDEnchantments.MOD_ID, "gilding");
    private final Container inventory;
    private final PlayerEntity playerEntity;
    private final ContainerLevelAccess context;
    private final ContainerData propertyDelegate;
    private Optional<Identifier> generatedEnchantment = Optional.empty();

    public Optional<Identifier> getGeneratedEnchantment() {
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
        Optional<Identifier> generatedEnchantment
    ) {
        super(ModScreenHandlers.GILDING_FOUNDRY_SCREEN_HANDLER, syncId);
        this.context = context;
        this.playerEntity = playerInventory.PlayerEntity;
        checkContainerSize(inventory, 2);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.PlayerEntity);
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

        addSlotListener(EnchantmentUtils.generatorListener(context, playerInventory.PlayerEntity));
        addSlotListener(this);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(delegate);
    }

    @Override
    public boolean clickMenuButton(PlayerEntity player, int id) {
        if (generatedEnchantment.isEmpty()) {
            return false;
        }
        var weaponStack = PlayerEntity.isCreative() ? inventory.getItem(0) : inventory.getItem(0).copy();
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
        setNewEnchantment(PlayerEntity, weaponStack);

        if (!PlayerEntity.isCreative()) {
            context.execute((world, pos) -> {
                ((GildingFoundryBlockEntity)world.getBlockEntity(pos))
                    .setGenerated(enchantmentId);
            });
            startProgress();
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int invSlot) {
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
    public boolean stillValid(PlayerEntity player) {
        return stillValid(context, PlayerEntity, ModBlocks.GILDING_FOUNDRY);
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

    public static List<Identifier> getCandidatesForGidling(ItemStack itemStack) {
        return EnchantmentUtils.getEnchantmentsForItem(itemStack).collect(Collectors.toList());
    }

    public List<Identifier> getCandidatesForGidling() {
        return getCandidatesForGidling(inventory.getItem(0));
    }

    public void setNewEnchantment(PlayerEntity player, ItemStack weaponStack) {
        context.execute((world, pos) -> {
            ServerPlayerEntity serverPlayer = world.getServer().getPlayerList().getPlayer(PlayerEntity.getUUID());
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
        var PlayerEntity = client.PlayerEntity;
        if (PlayerEntity == null) {
            return;
        }

        var screenHandler = PlayerEntity.containerMenu;
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
