package net.backupcup.mcde.util;

import static net.minecraft.core.registries.BuiltInRegistries.ENCHANTMENT;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.backupcup.mcde.MCDEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;

public class EnchantmentUtils {
    public static Stream<ResourceLocation> getEnchantmentStream() {
        return ENCHANTMENT.keySet().stream();
    }

    public static Stream<ResourceLocation> getEnchantmentsForItem(ItemStack itemStack) {
        var existing = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
            .map(e -> ENCHANTMENT.getKey(e))
            .collect(Collectors.toSet());
        return getAllEnchantmentsForItem(itemStack)
            .filter(id -> !existing.contains(id));
    }

    public static Stream<ResourceLocation> getAllEnchantmentsForItem(ItemStack itemStack) {
        Predicate<Enchantment> target = itemStack.is(ModTags.Items.WEAPONS) ?
            e -> e.category.equals(EnchantmentCategory.WEAPON) || e.canEnchant(itemStack) :
            e -> e.canEnchant(itemStack);

        return ENCHANTMENT.stream()
            .filter(MCDEnchantments.getConfig()::isEnchantmentAllowed)
            .filter(e -> e.isDiscoverable() || !MCDEnchantments.getConfig().isAvailabilityForRandomSelectionRespected())
            .filter(e -> !e.isTreasureOnly() || MCDEnchantments.getConfig().isTreasureAllowed())
            .filter(e -> !e.isCurse() || MCDEnchantments.getConfig().areCursedAllowed())
            .filter(target)
            .map(ENCHANTMENT::getKey);
    }

    public static List<EnchantmentCategory> getEnchantmentTargets(Item item) {
        return Arrays.stream(EnchantmentCategory.values())
            .filter(target -> target.canEnchant(item)).toList();
    }

    public static Enchantment getEnchantment(ResourceLocation enchantmentId) {
        return ENCHANTMENT.get(enchantmentId);
    }

    public static ResourceLocation getEnchantmentId(Enchantment enchantment) {
        return ENCHANTMENT.getKey(enchantment);
    }

    public static ChatFormatting formatEnchantment(ResourceLocation id) {
        return MCDEnchantments.getConfig().isEnchantmentPowerful(id) ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE;
    }

    public static boolean isCompatible(Collection<ResourceLocation> present, ResourceLocation enchantment) {
        return present.stream().allMatch(id -> ENCHANTMENT.get(enchantment).isCompatibleWith(ENCHANTMENT.get(id)));
    }

    public static boolean isCompatible(ResourceLocation present, ResourceLocation enchantment) {
        return ENCHANTMENT.get(enchantment).isCompatibleWith(ENCHANTMENT.get(present));
    }


    public static ContainerListener generatorListener(ContainerLevelAccess context, Player player) {
        return new ContainerListener() {
            @Override
            public void dataChanged(AbstractContainerMenu handler, int property, int value) {
            }

            @Override
            public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stack) {
                if (slotId != 0 || stack.isEmpty() || EnchantmentSlots.fromItemStack(stack).isPresent()) {
                    return;
                }
                context.execute((world, pos) -> {
                    var server = world.getServer();
                    var serverPlayerEntity = Optional.ofNullable(server.getPlayerList().getPlayer(player.getUUID()));
                    SlotsGenerator.forItemStack(stack)
                        .withOptionalOwner(serverPlayerEntity)
                        .build()
                        .generateEnchantments()
                        .updateItemStack(stack);
                    handler.setItem(0, 0, stack);
                });
            }
        };
    }

    public static Optional<ResourceLocation> generateEnchantment(ItemStack itemStack, Optional<ServerPlayer> optionalOwner) {
        return generateEnchantment(itemStack, optionalOwner, new SingleThreadedRandomSource(System.nanoTime()), getPossibleCandidates(itemStack));
    }

    public static Optional<ResourceLocation> generateEnchantment(ItemStack itemStack, Optional<ServerPlayer> optionalOwner, List<ResourceLocation> candidates) {
        return generateEnchantment(itemStack, optionalOwner, new SingleThreadedRandomSource(System.nanoTime()), candidates);
    }

    public static Set<ResourceLocation> getLockedEnchantments(ServerPlayer player) {
        if (player.isCreative()) {
            return Set.of();
        }
        var advancements = player.server.getAdvancements().getAllAdvancements();
        var tracker = player.getAdvancements();
        var unlocks = MCDEnchantments.getConfig().getUnlocks().stream()
            .collect(Collectors.partitioningBy(u -> advancements.stream()
                .filter(u.getAdvancements()::contains)
                .allMatch(a -> tracker.getOrStartProgress(a).isDone()),
                Collectors.flatMapping(u -> getEnchantmentStream().filter(u.getEnchantments()::contains),
                    Collectors.toSet())));
        unlocks.get(false).removeIf(unlocks.get(true)::contains);
        return unlocks.get(false);
    }

    public static Optional<ResourceLocation> generateEnchantment(ItemStack itemStack, Optional<ServerPlayer> optionalOwner, RandomSource random, List<ResourceLocation> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        optionalOwner.ifPresent(player -> candidates.removeIf(getLockedEnchantments(player)::contains));
        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    public static Set<ResourceLocation> getAllEnchantmentsInItem(ItemStack itemStack) {
        var present = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
            .map(key -> ENCHANTMENT.getKey(key))
            .collect(Collectors.toSet());
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (slotsOptional.isEmpty()) {
            return present;
        }
        var slots = slotsOptional.get();
        slots.stream()
            .flatMap(s -> s.choices().stream())
            .map(c -> c.getEnchantmentId()).forEach(present::add);
        return present;
    }

    public static Stream<ResourceLocation> getEnchantmentsNotInItem(ItemStack itemStack) {
        var present = getAllEnchantmentsInItem(itemStack);
        var candidates = getAllEnchantmentsForItem(itemStack)
            .filter(id -> !present.contains(id));
        return candidates;
    }

    public static boolean isGilding(Enchantment enchantment, ItemStack itemStack) {
        return EnchantmentSlots.fromItemStack(itemStack)
                .map(slots -> slots.getGildingIds().contains(EnchantmentUtils.getEnchantmentId(enchantment)))
                .orElse(false);
    }

    private static List<ResourceLocation> getPossibleCandidates(ItemStack itemStack) {
        var present = getAllEnchantmentsInItem(itemStack);
        var candidates = getAllEnchantmentsForItem(itemStack)
            .filter(id -> !present.contains(id));
        if (MCDEnchantments.getConfig().isCompatibilityRequired()) {
            candidates = candidates.filter(id -> isCompatible(present, id));
        }
         return candidates.toList();
    }


}
