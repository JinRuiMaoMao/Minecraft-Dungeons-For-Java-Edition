package net.backupcup.mcde.block.entity;

import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.screen.handler.GildingFoundryScreenHandler;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.ContainerHelper;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AbstractContainerMenu;
import net.minecraft.screen.ContainerData;
import net.minecraft.screen.ContainerLevelAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;

public class GildingFoundryBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private int gilding_progress;
    private Optional<Identifier> generated = Optional.empty();

    public void setGenerated(Identifier id) {
        generated = Optional.of(id);
    }

    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    private final ContainerData propertyDelegate = new ContainerData() {

        @Override
        public int get(int index) {
            return gilding_progress;
        }

        @Override
        public void set(int index, int value) {
            gilding_progress = value;
        }

        @Override
        public int getCount() {
            return 1;
        }

    };

    public GildingFoundryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GILDING_FOUNDRY, pos, state);
    }

    public boolean hasProgress() {
        return gilding_progress != 0;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.mcde.gilding_foundry");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, PlayerEntity player) {
        var weaponStack  = inventory.get(0);
        if (!weaponStack.isEmpty()) {
            generated = EnchantmentUtils.generateEnchantment(
                weaponStack,
                Optional.of(level.getServer().getPlayerList().getPlayer(inv.PlayerEntity.getUUID())),
                GildingFoundryScreenHandler.getCandidatesForGidling(weaponStack)
            );
        }
        return new GildingFoundryScreenHandler(
            syncId,
            inv,
            this,
            this.propertyDelegate,
            ContainerLevelAccess.create(level, worldPosition),
            generated
        );
    }

    @Override
    protected void saveAdditional(NbtCompound nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, inventory);
    }

    @Override
    public void load(NbtCompound nbt) {
        ContainerHelper.loadAllItems(nbt, inventory);
        super.load(nbt);
    }

    public static void tick(World world, BlockPos blockPos, BlockState state, GildingFoundryBlockEntity entity) {
        if (world.isClientSide()) {
            return;
        }

        if (entity.gilding_progress == 0) {
            return;
        }
        if (entity.inventory.get(0).isEmpty() || entity.inventory.get(1).getCount() < MCDEnchantments.getConfig().getGildingCost()) {
            entity.resetProgress();
            setChanged(world, blockPos, state);
            return;
        }
        entity.gilding_progress++;
        if (entity.gilding_progress > MCDEnchantments.getConfig().getGildingDuration()) {
            entity.finishGilding();
        }

        setChanged(world, blockPos, state);
    }

    private void finishGilding() {
        gilding_progress = 0;
        var weaponStack = inventory.get(0);
        
        if (generated.isEmpty()) {
            return;
        }
        var ingridient = inventory.get(1);
        var item = ingridient.getItem();
        ingridient.shrink(MCDEnchantments.getConfig().getGildingCost());
        var id = generated.get();
        EnchantmentSlots.fromItemStack(weaponStack).ifPresent(slots -> {
            if (item.equals(Items.EMERALD)) {
                var map = EnchantmentHelper.getEnchantments(weaponStack);
                map.keySet().removeAll(slots.getGildingEnchantments());
                EnchantmentHelper.setEnchantments(map, weaponStack);
                slots.removeAllGildings();
            }
            slots.addGilding(id);
            slots.updateItemStack(weaponStack);
        });
    }

    private void resetProgress() {
        gilding_progress = 0;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, FriendlyByteBuf buf) {
        buf.writeOptional(generated, (w, id) -> w.writeResourceLocation(id));
    }
}
