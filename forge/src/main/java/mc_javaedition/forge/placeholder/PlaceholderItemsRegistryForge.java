package mc_javaedition.forge.placeholder;

import mc_javaedition.combat.MeleeWeaponStatLookup;
import mc_javaedition.combat.RangedWeaponStatLookup;
import mc_javaedition.combat.ArmorStatLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.world.item.alchemy.Potion;

/**
 * Registers placeholder Items for all item model JSONs found in assets/<namespace>/models/item/.
 *
 * Goal: make /give <namespace>:<id> recognize items and show icons (depending on texture/model availability).
 *
 * This is a migration-time safety net while the full item registries are incomplete.
 */
public final class PlaceholderItemsRegistryForge {
    private static final String LIST_RESOURCE = "/placeholder_items.txt";
    private static final String TARGET_NAMESPACE = "mcdjava";

    private PlaceholderItemsRegistryForge() {}

    private static final float BASIC_FOOD_SATURATION = 0.3f;
    private static final int BASIC_FOOD_NUTRITION = 4;
    private static final Set<String> ARTIFACT_IDS = Set.of(
            "wonderful_wheat", "wind_horn", "updraft_tome", "totem_of_soul_protection", "totem_of_shielding",
            "totem_of_regeneration", "torment_quiver", "thundering_quiver", "tasty_bone", "soul_healer",
            "shock_powder", "satchel_of_elixirs", "satchel_of_elements", "powershaker", "love_medallion",
            "lightning_rod", "light_feather", "iron_hide_amulet", "harvester", "harpoon_quiver",
            "gong_of_weakening", "golem_kit", "ghost_cloak", "flaming_quiver", "enchanters_tome",
            "enchanted_grass", "death_cap_mushroom", "corrupted_seeds", "fishing_rod", "ice_wand",
            "buzzy_nest", "boots_of_swiftness", "blast_fungus",
            "eye_of_the_guardian", "corrupted_pumpkin"
    );
    private static final Set<String> FORCED_ARTIFACT_PLACEHOLDERS = Set.of(
            "corrupted_beacon",
            "updraft_tome",
            "harvester",
            "lightning_rod",
            "scatter_mines",
            "blast_fungus",
            "spinblade",
            "eye_of_the_guardian",
            "corrupted_pumpkin",
            "corrupted_seeds",
            "fishing_rod",
            "gong_of_weakening",
            "ice_wand",
            "love_medallion",
            "satchel_of_elements",
            "shock_powder"
    );

    public static void register(IEventBus bus) {
        MeleeWeaponStatLookup.ensureLoaded();
        RangedWeaponStatLookup.ensureLoaded();
        ArmorStatLookup.ensureLoaded();
        Map<String, DeferredRegister<Item>> byNamespace = new HashMap<>();
        Set<String> registeredPaths = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                PlaceholderItemsRegistryForge.class.getResourceAsStream(LIST_RESOURCE),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;

                String idPath = parts[1];

                // User request: all placeholder items must use mcdjava namespace.
                ResourceLocation fullId = new ResourceLocation(TARGET_NAMESPACE, idPath);

                // Avoid same path collisions when merging multiple source namespaces into one.
                if (!registeredPaths.add(idPath)) {
                    continue;
                }

                // Skip if something already exists (avoid "already registered/frozen" crashes).
                if (BuiltInRegistries.ITEM.containsKey(fullId)) {
                    continue;
                }

                DeferredRegister<Item> dr = byNamespace.computeIfAbsent(
                        TARGET_NAMESPACE,
                        ns -> DeferredRegister.create(Registries.ITEM, ns)
                );

                dr.register(idPath, () -> createFunctionalPlaceholderItem(TARGET_NAMESPACE, idPath));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load placeholder item list: " + LIST_RESOURCE, e);
        }

        DeferredRegister<Item> dr = byNamespace.computeIfAbsent(
                TARGET_NAMESPACE,
                ns -> DeferredRegister.create(Registries.ITEM, ns)
        );
        for (String idPath : FORCED_ARTIFACT_PLACEHOLDERS) {
            ResourceLocation fullId = new ResourceLocation(TARGET_NAMESPACE, idPath);
            if (!registeredPaths.contains(idPath) && !BuiltInRegistries.ITEM.containsKey(fullId)) {
                registeredPaths.add(idPath);
                dr.register(idPath, () -> new PlaceholderArtifactForge(TARGET_NAMESPACE, idPath));
            }
        }

        // Must be registered to the mod event bus after all DeferredRegister objects were populated.
        byNamespace.values().forEach(reg -> reg.register(bus));
    }

    private static Item createFunctionalPlaceholderItem(String namespace, String idPath) {
        try {
            Item.Properties settings = new Item.Properties();

            if (isMeleeWeaponId(idPath)) {
                return createMeleeWeapon(namespace, idPath);
            }
            if (isRangedWeaponId(idPath)) {
                return createRangedWeapon(namespace, idPath);
            }
            if (isArmorPieceId(idPath)) {
                return createArmorPiece(namespace, idPath);
            }
            if (isArtifactId(idPath)) {
                return new PlaceholderArtifactForge(namespace, idPath);
            }

            if (isFoodId(idPath)) {
                Object food = createBasicFoodObject();
                if (food != null) {
                    for (Method m : settings.getClass().getMethods()) {
                        if (!"food".equals(m.getName()) || m.getParameterCount() != 1) continue;
                        m.invoke(settings, food);
                        break;
                    }
                }
                return new PlaceholderNamedItemForge(namespace, idPath, settings, null);
            }

            String potionField = potionFieldForId(idPath);
            if (potionField != null) {
                tryInvokeMethod(settings, "stacksTo", new Class<?>[]{int.class}, new Object[]{1});
                Object potion = tryGetStaticField("net.minecraft.world.item.alchemy.Potions", potionField);
                if (potion == null) {
                    potion = tryGetStaticField("net.minecraft.potion.Potions", potionField);
                }
                if (potion != null) {
                    if (potion instanceof Potion potionObj) {
                        return new PlaceholderNamedItemForge(namespace, idPath, settings, potionObj);
                    }
                }
            }

            if (isSingleUseId(idPath)) {
                tryInvokeMethod(settings, "stacksTo", new Class<?>[]{int.class}, new Object[]{1});
            }

            return new PlaceholderNamedItemForge(namespace, idPath, settings, null);
        } catch (Throwable ignored) {
            return new PlaceholderNamedItemForge(namespace, idPath, new Item.Properties(), null);
        }
    }

    private static Item createMeleeWeapon(String namespace, String idPath) {
        MeleeWeaponStatLookup.Stats st = MeleeWeaponStatLookup.get(idPath);
        return new PlaceholderMeleeWeaponForge(namespace, idPath, tierFromName(st.tier()), st.damage(), st.attackSpeed());
    }

    private static Tier tierFromName(String tierName) {
        if (tierName == null) {
            return Tiers.IRON;
        }
        return switch (tierName.toUpperCase(Locale.ROOT)) {
            case "WOOD" -> Tiers.WOOD;
            case "STONE" -> Tiers.STONE;
            case "IRON" -> Tiers.IRON;
            case "GOLD" -> Tiers.GOLD;
            case "DIAMOND" -> Tiers.DIAMOND;
            case "NETHERITE" -> Tiers.NETHERITE;
            default -> Tiers.IRON;
        };
    }

    private static boolean isMeleeWeaponId(String idPath) {
        if (idPath.startsWith("weapon_")) {
            return true;
        }
        String[] meleePrefixes = {
                "sword_", "axe_", "double_axe_", "dagger_", "soul_dagger_", "hammer_", "gauntlet_",
                "sickle_", "scythe_", "pick_", "glaive_", "spear_", "staff_", "whip_"
        };
        for (String p : meleePrefixes) {
            if (idPath.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private static Item createRangedWeapon(String namespace, String idPath) {
        RangedWeaponStatLookup.Stats st = RangedWeaponStatLookup.get(idPath);
        int durability = Math.max(64, RangedWeaponStatLookup.tierDurability(st.tier()));
        if (idPath.startsWith("crossbow_")) {
            return new PlaceholderCrossbowForge(namespace, idPath, durability, st.projectileDamage(), st.drawSpeed(), st.range());
        }
        return new PlaceholderBowForge(namespace, idPath, durability, st.projectileDamage(), st.drawSpeed(), st.range());
    }

    private static boolean isRangedWeaponId(String idPath) {
        return idPath.startsWith("bow_")
                || idPath.startsWith("shortbow_")
                || idPath.startsWith("longbow_")
                || idPath.startsWith("crossbow_");
    }

    private static boolean isArmorPieceId(String idPath) {
        return idPath.endsWith("_helmet")
                || idPath.endsWith("_chestplate")
                || idPath.endsWith("_leggings")
                || idPath.endsWith("_boots");
    }

    private static Item createArmorPiece(String namespace, String idPath) {
        ArmorStatLookup.Stats st = ArmorStatLookup.get(idPath);
        ArmorItem.Type type = armorTypeForId(idPath);
        return new PlaceholderArmorForge(
                namespace,
                idPath,
                type,
                st.protection(),
                Math.max(32, st.durability()),
                st.toughness(),
                st.knockback()
        );
    }

    private static ArmorItem.Type armorTypeForId(String idPath) {
        if (idPath.endsWith("_helmet")) {
            return ArmorItem.Type.HELMET;
        }
        if (idPath.endsWith("_chestplate")) {
            return ArmorItem.Type.CHESTPLATE;
        }
        if (idPath.endsWith("_leggings")) {
            return ArmorItem.Type.LEGGINGS;
        }
        return ArmorItem.Type.BOOTS;
    }

    private static boolean isArtifactId(String idPath) {
        if (ARTIFACT_IDS.contains(idPath)) return true;
        String s = idPath.toLowerCase(Locale.ROOT);
        if (s.startsWith("artifact_")) return true;
        return s.contains("_quiver")
                || s.contains("totem_of_")
                || s.contains("_tome")
                || s.contains("_rod")
                || s.contains("_amulet")
                || s.contains("_mushroom")
                || s.contains("_cloak")
                || s.contains("_horn")
                || s.contains("_feather")
                || s.contains("_kit")
                || s.contains("_nest")
                || s.contains("_seeds")
                || s.contains("_medallion")
                || s.contains("_fungus")
                || s.contains("_healer")
                || s.contains("_wheat")
                || s.contains("beacon")
                || s.contains("scatter")
                || s.contains("_mine")
                || s.contains("spinblade")
                || s.contains("powershaker")
                || s.contains("harvester")
                || s.contains("enchanters_tome");
    }

    private static boolean isFoodId(String idPath) {
        return "apple".equals(idPath)
                || "bread".equals(idPath)
                || "porkchop".equals(idPath)
                || "deathcapmushroom".equals(idPath);
    }

    private static boolean isSingleUseId(String idPath) {
        return idPath.contains("potion")
                || "healthpot".equals(idPath)
                || "shadowbrew".equals(idPath);
    }

    private static String potionFieldForId(String idPath) {
        return switch (idPath) {
            case "healthpot", "potionhealth" -> "HEALING";
            case "potionspeed" -> "SWIFTNESS";
            case "strengthpotion" -> "STRENGTH";
            case "shadowbrew" -> "INVISIBILITY";
            default -> null;
        };
    }

    private static Object createBasicFoodObject() {
        Object food = tryCreateFoodByBuilder(
                "net.minecraft.world.food.FoodProperties$Builder",
                "nutrition",
                "saturationMod"
        );
        if (food != null) return food;

        // Fallback (older versions / alternate mappings)
        return tryCreateFoodByBuilder(
                "net.minecraft.item.FoodComponent$Builder",
                "nutrition",
                "saturationMod"
        );
    }

    private static Object tryCreateFoodByBuilder(String builderClassName, String nutritionMethod, String saturationMethod) {
        try {
            Class<?> builderClass = Class.forName(builderClassName);
            Object builder = builderClass.getConstructor().newInstance();
            tryInvokeMethod(builder, nutritionMethod, new Class<?>[]{int.class}, new Object[]{BASIC_FOOD_NUTRITION});
            tryInvokeMethod(builder, saturationMethod, new Class<?>[]{float.class}, new Object[]{BASIC_FOOD_SATURATION});

            Method build = builderClass.getMethod("build");
            return build.invoke(builder);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object tryGetStaticField(String className, String fieldName) {
        try {
            Class<?> c = Class.forName(className);
            Field f = c.getField(fieldName);
            return f.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object tryInstantiate(String[] candidateClassNames, Object[] args) {
        for (String className : candidateClassNames) {
            Object item = tryInstantiate(className, args);
            if (item != null) return item;
        }
        return null;
    }

    private static Object tryInstantiate(String className, Object[] args) {
        try {
            Class<?> c = Class.forName(className);
            for (Constructor<?> ctor : c.getConstructors()) {
                if (ctor.getParameterCount() != args.length) continue;
                Class<?>[] paramTypes = ctor.getParameterTypes();

                boolean ok = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    Object arg = args[i];
                    if (arg != null && !paramTypes[i].isInstance(arg)) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) continue;

                return ctor.newInstance(args);
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return null;
    }

    private static void tryInvokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            m.invoke(target, args);
        } catch (Throwable ignored) {
            // ignore
        }
    }
}

