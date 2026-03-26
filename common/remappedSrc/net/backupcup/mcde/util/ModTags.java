package net.backupcup.mcde.util;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> WEAPONS = TagKey.create(Registries.ITEM, new ResourceLocation("c", "weapon_enchantments_allowed"));
    }

    public static class Enchantments {
        public static final TagKey<Enchantment> POWERFUL = TagKey.create(Registries.ENCHANTMENT, new ResourceLocation("c", "powerful"));
    }

    public static boolean isIn(Enchantment enchantment, TagKey<Enchantment> tag) {
        return isIn(enchantment, tag, BuiltInRegistries.ENCHANTMENT);
    }

    public static boolean isIn(ResourceLocation enchantmentId, TagKey<Enchantment> tag) {
        return isIn(enchantmentId, tag, BuiltInRegistries.ENCHANTMENT);
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

    public static <T> boolean isIn(ResourceLocation id, TagKey<T> tag, Registry<T> registry) {
        return isIn(registry.get(id), tag, registry);
    }
}
