package net.backupcup.mcde.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantmentSlot {
    private SlotPosition slot;
    private Map<SlotPosition, ResourceLocation> enchantments;
    private int level = 0;

    private Optional<SlotPosition> chosen = Optional.empty();

    public EnchantmentSlot(SlotPosition slot, Map<SlotPosition, ResourceLocation> enchantments) {
        this.slot = slot;
        this.enchantments = enchantments;
    }

    public Optional<SlotPosition> getChosenPosition() {
        return chosen;
    }

    public Optional<Choice> getChosen() {
        return chosen.map(pos -> new Choice(this, pos));
    }

    public boolean setChosen(SlotPosition pos, int level) {
        if (enchantments.containsKey(pos)) {
            this.chosen = Optional.of(pos);
            this.level = level;
            return true;
        }
        return false;
    }

    public void clearChoice() {
        chosen = Optional.empty();
        level = 0;
    }

    public SlotPosition getSlotPosition() {
        return slot;
    }

    public int ordinal() {
        return slot.ordinal();
    }

    public Optional<ResourceLocation> getChoice(SlotPosition pos) {
        return Optional.ofNullable(enchantments.get(pos));
    }

    public List<Choice> choices() {
        return enchantments.keySet().stream().map(pos -> new Choice(this, pos)).toList();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void upgrade() {
        if (!isMaxedOut()) {
            level++;
        }
    }

    public boolean isMaxedOut() {
        return chosen.map(pos -> level >= BuiltInRegistries.ENCHANTMENT.get(enchantments.get(pos)).getMaxLevel()).orElse(false);
    }

    public void removeChosenEnchantment(ItemStack itemStack) {
        getChosen().ifPresent(c -> {
            var enchantments = EnchantmentHelper.getEnchantments(itemStack);
            enchantments.remove(c.getEnchantment());
            EnchantmentHelper.setEnchantments(enchantments, itemStack);
        });
    }

    public static EnchantmentSlot of(SlotPosition slot, ResourceLocation first) {
        return new EnchantmentSlot(slot, Map.of(SlotPosition.FIRST, first));
    }

    public static EnchantmentSlot of(SlotPosition slot, ResourceLocation first, ResourceLocation second) {
        return new EnchantmentSlot(slot, Map.of(SlotPosition.FIRST, first, SlotPosition.SECOND, second));
    }

    public static EnchantmentSlot of(SlotPosition slot, ResourceLocation first, ResourceLocation second, ResourceLocation third) {
        return new EnchantmentSlot(slot, Map.of(SlotPosition.FIRST, first, SlotPosition.SECOND, second, SlotPosition.THIRD, third));
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append('[');
        builder.append(Arrays.stream(SlotPosition.values()).flatMap(pos -> {
            if (!enchantments.containsKey(pos)) {
                return Stream.empty();
            }

            if (chosen.isPresent() && pos == chosen.get()) {
                return Stream.of(String.format("(%s {%d})", enchantments.get(pos), level));
            }

            return Stream.of(enchantments.get(pos).toString());
        }).collect(Collectors.joining(", ")));
        builder.append(']');
        return builder.toString();
    }

    public CompoundTag toNbt() {
        CompoundTag root = new CompoundTag();
        CompoundTag choices = new CompoundTag();
        enchantments.entrySet().stream()
            .forEach(kvp -> choices.putString(kvp.getKey().name(), kvp.getValue().toString()));
        root.put("Choices", choices);
        if (chosen.isPresent()) {
            root.putString("Chosen", chosen.get().name());
        }
        return root;
    }

    public static EnchantmentSlot fromNbt(CompoundTag nbt, SlotPosition slot, Map<Enchantment, Integer> enchantments) {
        var choices = nbt.getCompound("Choices");
        var choiceMap = choices.getAllKeys().stream()
                .collect(Collectors.toMap(
                    key -> SlotPosition.valueOf(key),
                    key -> ResourceLocation.tryParse(choices.getString(key))
                ));
        choiceMap.entrySet().removeIf(entry -> EnchantmentUtils.getEnchantment(entry.getValue()) == null);
        var newSlot = new EnchantmentSlot(slot, choiceMap);
        if (nbt.contains("Chosen")) {
            var chosenSlot = SlotPosition.valueOf(nbt.getString("Chosen"));
            if (choiceMap.containsKey(chosenSlot)) {
                newSlot.setChosen(chosenSlot, enchantments.getOrDefault(EnchantmentUtils.getEnchantment(choiceMap.get(chosenSlot)), 0));
            }
        }
        return newSlot;
    }

    public void changeEnchantment(SlotPosition pos, ResourceLocation enchantment) {
        enchantments.put(pos, enchantment);
    }
}
