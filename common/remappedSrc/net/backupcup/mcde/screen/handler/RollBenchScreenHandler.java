package net.backupcup.mcde.screen.handler;

import I;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.block.ModBlocks;
import net.backupcup.mcde.util.Choice;
import net.backupcup.mcde.util.EnchantmentSlot;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.backupcup.mcde.util.SlotPosition;
import net.backupcup.mcde.util.SlotsGenerator;
import net.backupcup.mcde.util.SlotsGenerator.Builder;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RollBenchScreenHandler extends AbstractContainerMenu implements ContainerListener {
    private final Container inventory = new SimpleContainer(2);
    private final ContainerLevelAccess context;
    private final Player playerEntity;
    private Map<SlotPosition, Boolean> locked = new EnumMap<>(Map.of(SlotPosition.FIRST, false, SlotPosition.SECOND, false, SlotPosition.THIRD, false));
    public static final ResourceLocation LOCKED_SLOTS_PACKET = ResourceLocation.tryBuild(MCDEnchantments.MOD_ID, "locked_slots");
    public static final int REROLL_BUTTON_ID = -1;

    public Container getInventory() {
        return inventory;
    }

    public Optional<Boolean> isSlotLocked(SlotPosition slot) {
        return Optional.ofNullable(locked.get(slot));
    }

    public RollBenchScreenHandler(int syncId, Inventory inventory) {
        this(syncId, inventory, ContainerLevelAccess.NULL);
    }

    public RollBenchScreenHandler(int syncId, Inventory playerInventory, ContainerLevelAccess context) {
        super(ModScreenHandlers.ROLL_BENCH_SCREEN_HANDLER, syncId);
        this.playerEntity = playerInventory.player;
        this.context = context;
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 131, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem().isEnchantable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(inventory, 1, 131, 52) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI) || stack.is(Items.ECHO_SHARD);
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
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        ItemStack itemStack = inventory.getItem(0);
        ItemStack rerollMaterialStack = inventory.getItem(1);
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (slotsOptional.isEmpty()) {
            return clickMenuButton(player, id);
        }
        var slots = slotsOptional.get();
        if (id == REROLL_BUTTON_ID) {
            var serverPlayerEntity = context.evaluate((world, pos) -> world.getServer().getPlayerList().getPlayer(player.getUUID()));
            var gilding = slots.getGildingIds();
            EnchantmentSlots newSlots;
            if (MCDEnchantments.getConfig().canFullRerollRemoveSlots()) {
                newSlots = SlotsGenerator.forItemStack(itemStack)
                    .withOptionalOwner(serverPlayerEntity)
                    .build()
                    .generateEnchantments();
            } else {
                var generatorBuilder = SlotsGenerator.forItemStack(itemStack)
                    .withOptionalOwner(serverPlayerEntity);
                if (slots.getEnchantmentSlot(SlotPosition.SECOND).isPresent()) {
                    generatorBuilder.withSecondSlotAbsoluteChance(1f);
                }
                if (slots.getEnchantmentSlot(SlotPosition.THIRD).isPresent()) {
                    generatorBuilder.withThirdSlotAbsoluteChance(1f);
                } 
                newSlots = generatorBuilder.build().generateEnchantments();
            }
            newSlots.addAllGilding(gilding);
            slots.removeChosenEnchantments(itemStack);
            newSlots.updateItemStack(itemStack);
            if (!player.isCreative()) {
                rerollMaterialStack.shrink(1);
            }
            player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5f, 1f);
            inventory.setChanged();
            return false;
        }
        var slotsSize = SlotPosition.values().length;
        var clickedSlot = slots.getEnchantmentSlot(SlotPosition.values()[id / slotsSize]).get();
        SlotPosition toChange;
        ResourceLocation enchantmentId;
        var newEnchantment = generateEnchantment(player, clickedSlot.getSlotPosition());
        if (newEnchantment.isEmpty()) {
            return super.clickMenuButton(player, id);
        }

        if (clickedSlot.getChosen().isPresent()) {
            var chosen = clickedSlot.getChosen().get();
            enchantmentId = chosen.getEnchantmentId();

            if (!canReroll(player, enchantmentId, slots)) {
                return super.clickMenuButton(player, id);
            }
            clickedSlot.removeChosenEnchantment(itemStack);
            clickedSlot.clearChoice();
            toChange = chosen.getChoicePosition();
        } else {
            toChange = SlotPosition.values()[id % slotsSize];
            enchantmentId = clickedSlot.getChoice(toChange).get();

            if (!canReroll(player, enchantmentId, slots)) {
                return super.clickMenuButton(player, id);
            }
        }

        clickedSlot.changeEnchantment(toChange, newEnchantment.get());
        if (!player.isCreative()) {
            rerollMaterialStack.shrink(slots.getNextRerollCost(enchantmentId));
        }
        MCDEnchantments.getConfig().getRerollCostParameters().updateCost(slots);
        slots.updateItemStack(itemStack);
        player.playNotifySound(SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5f, 1f);
        inventory.setChanged();
        return super.clickMenuButton(player, id);
    }

    public boolean canReroll(Player player, ResourceLocation enchantmentId, EnchantmentSlots slots) {
        if (player.isCreative()) {
            return true;
        }
        ItemStack lapisLazuliStack = inventory.getItem(1);
        return lapisLazuliStack.is(Items.LAPIS_LAZULI) && lapisLazuliStack.getCount() >= slots.getNextRerollCost(enchantmentId);
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
        return stillValid(context, player, ModBlocks.ROLL_BENCH);
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
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 2 + l * 18, 84 + i * 19));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 2 + i * 18, 148));
        }
    }

    public List<ResourceLocation> getCandidatesForReroll(SlotPosition clickedSlot) {
        var itemStack = inventory.getItem(0);
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (slotsOptional.isEmpty()) {
            return List.of();
        }
        var slots = slotsOptional.get();
        var candidates = EnchantmentUtils.getEnchantmentsNotInItem(itemStack);
        if (!MCDEnchantments.getConfig().isCompatibilityRequired()) {
            return candidates.collect(Collectors.toList());
        }
        var enchantmentsNotInClickedSlot =
            slots.stream().filter(s -> !s.getSlotPosition().equals(clickedSlot))
            .flatMap(s -> s.choices().stream())
            .map(c -> c.getEnchantmentId())
            .toList();
        candidates = candidates.filter(id -> EnchantmentUtils.isCompatible(enchantmentsNotInClickedSlot, id))
            .filter(id -> EnchantmentUtils.isCompatible(EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
                        .map(EnchantmentUtils::getEnchantmentId)
                        .filter(enchantmentId -> slots.getEnchantmentSlot(clickedSlot)
                            .flatMap(slot -> slot.getChosen())
                            .map(c -> !c.getEnchantmentId().equals(enchantmentId))
                            .orElse(true))
                        .toList(), id));
        return candidates.collect(Collectors.toList());
    }

    public Optional<ResourceLocation> generateEnchantment(Player player, SlotPosition clickedSlot) {
        return EnchantmentUtils.generateEnchantment(
            inventory.getItem(0),
            context.evaluate((world, pos) -> world.getServer().getPlayerList().getPlayer(player.getUUID())),
            getCandidatesForReroll(clickedSlot)
        );
    }

    private void sendLockedSlots(EnchantmentSlots slots, Player player) {
        var buffer = PacketByteBufs.create();
        var locked = slots.stream().collect(Collectors.toMap(
            s -> s.getSlotPosition(),
            s -> generateEnchantment(player, s.getSlotPosition()).isEmpty(),
            (lhs, rhs) -> lhs,
            () -> new EnumMap<>(SlotPosition.class)
        ));
        buffer.writeInt(containerId);
        buffer.writeMap(locked, (buf, s) -> buf.writeEnum(s), (buf, b) -> buf.writeBoolean(b));
        ServerPlayNetworking.send((ServerPlayer)player, LOCKED_SLOTS_PACKET, buffer);
    }


    public static void receiveNewLocks(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf,
            PacketSender responseSender) {
        var player = client.player;
        if (player == null) {
            return;
        }
        var screenHandler = player.containerMenu;
        if (screenHandler == null) {
            return;
        }

        if (screenHandler.containerId != buf.readInt()) {
            return;
        }

        var map = buf.readMap(b -> b.readEnum(SlotPosition.class), b -> b.readBoolean());

        if (screenHandler instanceof RollBenchScreenHandler rbScreenHandler) {
            client.execute(() -> {
                rbScreenHandler.locked = map;
            });
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu handler, int property, int value) {
    }

    @Override
    public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
        if (slotId != 0) {
            return;
        }
        EnchantmentSlots.fromItemStack(stack).ifPresent(
            slots -> sendLockedSlots(slots, playerEntity)
        );
    }
}
