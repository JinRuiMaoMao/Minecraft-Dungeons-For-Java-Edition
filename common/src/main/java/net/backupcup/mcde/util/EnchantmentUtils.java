package net.backupcup.mcde.util;

import static net.minecraft.registry.Registries.ENCHANTMENT;

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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AbstractContainerMenu;
import net.minecraft.screen.ContainerLevelAccess;
import net.minecraft.screen.ContainerListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.gen.SingleThreadedRandomSource;

public class EnchantmentUtils {
    public static Stream<Identifier> getEnchantmentStream() {
        return ENCHANTMENT.keySet().stream();
    }

    public static Stream<Identifier> getEnchantmentsForItem(ItemStack itemStack) {
        var existing = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
            .map(e -> ENCHANTMENT.getKey(e))
            .collect(Collectors.toSet());
        return getAllEnchantmentsForItem(itemStack)
            .filter(id -> !existing.contains(id));
    }

    public static Stream<Identifier> getAllEnchantmentsForItem(ItemStack itemStack) {
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

    public static Enchantment getEnchantment(Identifier enchantmentId) {
        return ENCHANTMENT.get(enchantmentId);
    }

    public static Identifier getEnchantmentId(Enchantment enchantment) {
        return ENCHANTMENT.getKey(enchantment);
    }

    public static Formatting formatEnchantment(Identifier id) {
        return MCDEnchantments.getConfig().isEnchantmentPowerful(id) ? Formatting.RED : Formatting.LIGHT_PURPLE;
    }

    public static boolean isCompatible(Collection<Identifier> present, Identifier enchantment) {
        return present.stream().allMatch(id -> ENCHANTMENT.get(enchantment).isCompatibleWith(ENCHANTMENT.get(id)));
    }

    public static boolean isCompatible(Identifier present, Identifier enchantment) {
        return ENCHANTMENT.get(enchantment).isCompatibleWith(ENCHANTMENT.get(present));
    }


    public static ContainerListener generatorListener(ContainerLevelAccess context, PlayerEntity player) {
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
                    var serverPlayerEntity = Optional.ofNullable(server.getPlayerList().getPlayer(PlayerEntity.getUUID()));
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

    public static Optional<Identifier> generateEnchantment(ItemStack itemStack, Optional<ServerPlayerEntity> optionalOwner) {
        return generateEnchantment(itemStack, optionalOwner, new SingleThreadedRandomSource(System.nanoTime()), getPossibleCandidates(itemStack));
    }

    public static Optional<Identifier> generateEnchantment(ItemStack itemStack, Optional<ServerPlayerEntity> optionalOwner, List<Identifier> candidates) {
        return generateEnchantment(itemStack, optionalOwner, new SingleThreadedRandomSource(System.nanoTime()), candidates);
    }

    public static Set<Identifier> getLockedEnchantments(ServerPlayerEntity player) {
        if (PlayerEntity.isCreative()) {
            return Set.of();
        }
        var advancements = PlayerEntity.server.getAdvancements().getAllAdvancements();
        var tracker = PlayerEntity.getAdvancements();
        var unlocks = MCDEnchantments.getConfig().getUnlocks().stream()
            .collect(Collectors.partitioningBy(u -> advancements.stream()
                .filter(u.getAdvancements()::contains)
                .allMatch(a -> tracker.getOrStartProgress(a).isDone()),
                Collectors.flatMapping(u -> getEnchantmentStream().filter(u.getEnchantments()::contains),
                    Collectors.toSet())));
        unlocks.get(false).removeIf(unlocks.get(true)::contains);
        return unlocks.get(false);
    }

    public static Optional<Identifier> generateEnchantment(ItemStack itemStack, Optional<ServerPlayerEntity> optionalOwner, Random random, List<Identifier> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        optionalOwner.ifPresent(PlayerEntity -> candidates.removeIf(getLockedEnchantments(PlayerEntity)::contains));
        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    public static Set<Identifier> getAllEnchantmentsInItem(ItemStack itemStack) {
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

    public static Stream<Identifier> getEnchantmentsNotInItem(ItemStack itemStack) {
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

    private static List<Identifier> getPossibleCandidates(ItemStack itemStack) {
        var present = getAllEnchantmentsInItem(itemStack);
        var candidates = getAllEnchantmentsForItem(itemStack)
            .filter(id -> !present.contains(id));
        if (MCDEnchantments.getConfig().isCompatibilityRequired()) {
            candidates = candidates.filter(id -> isCompatible(present, id));
        }
         return candidates.toList();
    }


}
