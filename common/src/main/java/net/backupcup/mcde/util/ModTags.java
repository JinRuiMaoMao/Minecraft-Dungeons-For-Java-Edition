package net.backupcup.mcde.util;

import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.enchantment.Enchantment;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> WEAPONS = TagKey.create(Registries.ITEM, new Identifier("c", "weapon_enchantments_allowed"));
    }

    public static class Enchantments {
        public static final TagKey<Enchantment> POWERFUL = TagKey.create(Registries.ENCHANTMENT, new Identifier("c", "powerful"));
    }

    public static boolean isIn(Enchantment enchantment, TagKey<Enchantment> tag) {
        return isIn(enchantment, tag, Registries.ENCHANTMENT);
    }

    public static boolean isIn(Identifier enchantmentId, TagKey<Enchantment> tag) {
        return isIn(enchantmentId, tag, Registries.ENCHANTMENT);
    }

    public static <T> boolean isIn(T obj, TagKey<T> tag, Registry<T> registry) {
        var key = registry.getResourceKey(obj);
        if (key.isEmpty()) {
            return false;
        }
        var entry = registry.getHolder(key.get());
        if (entry.isEmpty()) {
            return false;
        }
        return entry.get().is(tag);
    }

    public static <T> boolean isIn(Identifier id, TagKey<T> tag, Registry<T> registry) {
        return isIn(registry.get(id), tag, registry);
    }
}
