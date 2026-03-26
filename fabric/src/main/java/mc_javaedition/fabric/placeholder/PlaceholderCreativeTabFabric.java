package mc_javaedition.fabric.placeholder;

import mc_javaedition.combat.MeleeWeaponStatLookup;
import mc_javaedition.combat.RangedWeaponStatLookup;
import mc_javaedition.combat.ArmorStatLookup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom creative tabs for placeholder items.
 *
 * Tabs (user requested):
 * 1) melee
 * 2) ranged
 * 3) armor
 * 4) artifacts ("神器" - mcdar namespace)
 * 5) other
 */
public final class PlaceholderCreativeTabFabric {
    private static final String LIST_RESOURCE = "/placeholder_items.txt";
    private static final String TARGET_NAMESPACE = "mcdjava";

    private static final Identifier WEAPONS_TAB_ID = new Identifier("mcd_java", "placeholders/01_melee");
    private static final Identifier BOWS_TAB_ID = new Identifier("mcd_java", "placeholders/02_ranged");
    private static final Identifier ARMOR_TAB_ID = new Identifier("mcd_java", "placeholders/03_armor");
    private static final Identifier ARTIFACTS_TAB_ID = new Identifier("mcd_java", "placeholders/04_artifacts");
    private static final Identifier OTHER_TAB_ID = new Identifier("mcd_java", "placeholders/05_other");

    private static final RegistryKey<ItemGroup> WEAPONS_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), WEAPONS_TAB_ID);
    private static final RegistryKey<ItemGroup> BOWS_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), BOWS_TAB_ID);
    private static final RegistryKey<ItemGroup> ARMOR_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), ARMOR_TAB_ID);
    private static final RegistryKey<ItemGroup> ARTIFACTS_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), ARTIFACTS_TAB_ID);
    private static final RegistryKey<ItemGroup> OTHER_TAB_KEY =
            RegistryKey.of(Registries.ITEM_GROUP.getKey(), OTHER_TAB_ID);

    private static boolean initialized = false;

    /** Melee weapons tab: sorted by attack power (low → high); unlisted / no-damage-modifier last. */
    private static final List<Identifier> MELEE = new ArrayList<>();
    private static final List<Identifier> BOWS = new ArrayList<>();
    private static final List<Identifier> ARMOR = new ArrayList<>();
    private static final List<Identifier> ARTIFACTS = new ArrayList<>();
    private static final List<Identifier> OTHER = new ArrayList<>();
    private static final List<Identifier> DAMAGING_ARTIFACTS = List.of(
            new Identifier(TARGET_NAMESPACE, "corrupted_beacon"),
            new Identifier(TARGET_NAMESPACE, "updraft_tome"),
            new Identifier(TARGET_NAMESPACE, "harvester"),
            new Identifier(TARGET_NAMESPACE, "lightning_rod"),
            new Identifier(TARGET_NAMESPACE, "scatter_mines"),
            new Identifier(TARGET_NAMESPACE, "blast_fungus"),
            new Identifier(TARGET_NAMESPACE, "spinblade"),
            new Identifier(TARGET_NAMESPACE, "eye_of_the_guardian"),
            new Identifier(TARGET_NAMESPACE, "corrupted_pumpkin")
    );
    private static boolean damagingArtifactsFilterEnabled = false;

    private PlaceholderCreativeTabFabric() {}

    public static void register() {
        if (initialized) return;
        initialized = true;

        loadAndCategorize();

        Registry.register(
                Registries.ITEM_GROUP,
                WEAPONS_TAB_KEY,
                buildPlaceholderGroup("MC Dungeons (Placeholder Items) - Melee", Items.IRON_SWORD)
        );
        Registry.register(
                Registries.ITEM_GROUP,
                BOWS_TAB_KEY,
                buildPlaceholderGroup("MC Dungeons (Placeholder Items) - Ranged", Items.BOW)
        );
        Registry.register(
                Registries.ITEM_GROUP,
                ARMOR_TAB_KEY,
                buildPlaceholderGroup("MC Dungeons (Placeholder Items) - Armor", Items.IRON_CHESTPLATE)
        );
        Registry.register(
                Registries.ITEM_GROUP,
                ARTIFACTS_TAB_KEY,
                buildPlaceholderGroup("MC Dungeons (Placeholder Items) - Artifacts", Items.ANVIL)
        );
        Registry.register(
                Registries.ITEM_GROUP,
                OTHER_TAB_KEY,
                buildPlaceholderGroup("MC Dungeons (Placeholder Items) - Other", Items.BOOK)
        );

        ItemGroupEvents.modifyEntriesEvent(WEAPONS_TAB_KEY).register(entries -> {
            for (Identifier id : MELEE) {
                if (!Registries.ITEM.containsId(id)) continue;
                entries.add(Registries.ITEM.get(id).getDefaultStack());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(BOWS_TAB_KEY).register(entries -> {
            for (Identifier id : BOWS) {
                if (!Registries.ITEM.containsId(id)) continue;
                entries.add(Registries.ITEM.get(id).getDefaultStack());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ARTIFACTS_TAB_KEY).register(entries -> {
            for (Identifier id : (damagingArtifactsFilterEnabled ? DAMAGING_ARTIFACTS : ARTIFACTS)) {
                if (!Registries.ITEM.containsId(id)) {
                    // Keep manual order stable even when a target artifact is missing.
                    entries.add(Items.BARRIER.getDefaultStack());
                    continue;
                }
                Item item = Registries.ITEM.get(id);
                if (!(item instanceof PlaceholderArtifactFabric)) {
                    // If wrong item got mapped to this artifact slot, force barrier placeholder.
                    entries.add(Items.BARRIER.getDefaultStack());
                    continue;
                }
                entries.add(item.getDefaultStack());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ARMOR_TAB_KEY).register(entries -> {
            for (Identifier id : ARMOR) {
                if (!Registries.ITEM.containsId(id)) continue;
                entries.add(Registries.ITEM.get(id).getDefaultStack());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(OTHER_TAB_KEY).register(entries -> {
            for (Identifier id : OTHER) {
                if (!Registries.ITEM.containsId(id)) continue;
                entries.add(Registries.ITEM.get(id).getDefaultStack());
            }
        });
    }

    private static void loadAndCategorize() {
        MELEE.clear();
        BOWS.clear();
        ARMOR.clear();
        ARTIFACTS.clear();
        OTHER.clear();
        Map<String, String> chosenNamespaceByPath = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                PlaceholderCreativeTabFabric.class.getResourceAsStream(LIST_RESOURCE),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 2);
                if (parts.length != 2) continue;
                String namespace = parts[0];
                String idPath = parts[1];
                if (isDisplayVariantId(idPath)) {
                    continue;
                }
                // User request: only keep "normal" namespaces; never show legacy namespace entries.
                if (!("mcd_java".equals(namespace)
                        || "mcda".equals(namespace)
                        || "mcdw".equals(namespace)
                        || "mcdar".equals(namespace)
                        || "mcde".equals(namespace))) {
                    continue;
                }
                String existing = chosenNamespaceByPath.get(idPath);
                if (existing == null || ("mcdar".equals(namespace) && !"mcdar".equals(existing))) {
                    // Prefer mcdar when same id exists in multiple namespaces, so artifacts tab won't disappear.
                    chosenNamespaceByPath.put(idPath, namespace);
                }
            }

            for (Map.Entry<String, String> e : chosenNamespaceByPath.entrySet()) {
                String idPath = e.getKey();
                String namespace = e.getValue();
                Identifier fullId = new Identifier(TARGET_NAMESPACE, idPath);

                if (isArtifactId(idPath)) {
                    ARTIFACTS.add(fullId);
                } else if (isBowId(idPath)) {
                    BOWS.add(fullId);
                } else if (isArmorId(idPath)) {
                    ARMOR.add(fullId);
                } else if (isMeleeTrailingWeapon(idPath) || isWeaponId(idPath)) {
                    MELEE.add(fullId);
                } else {
                    OTHER.add(fullId);
                }
            }
            reorderOtherItems();
            reorderArtifacts();
            sortMeleeTab();
            sortRangedTab();
            sortArmorTab();
        } catch (Exception e) {
            // If the file is missing, we just keep the tab empty.
        }
    }

    private static void reorderOtherItems() {
        Identifier apple = new Identifier(TARGET_NAMESPACE, "apple");
        Identifier bread = new Identifier(TARGET_NAMESPACE, "bread");
        Identifier porkchop = new Identifier(TARGET_NAMESPACE, "porkchop");
        Identifier gildingFoundry = new Identifier(TARGET_NAMESPACE, "gilding_foundry");
        Identifier rollBench = new Identifier(TARGET_NAMESPACE, "roll_bench");
        Identifier runicTable = new Identifier(TARGET_NAMESPACE, "runic_table");

        OTHER.remove(apple);
        OTHER.remove(bread);
        OTHER.remove(porkchop);
        OTHER.remove(gildingFoundry);
        OTHER.remove(rollBench);
        OTHER.remove(runicTable);

        int insertAt = 0;
        if (Registries.ITEM.containsId(apple)) OTHER.add(insertAt++, apple);
        if (Registries.ITEM.containsId(bread)) OTHER.add(insertAt++, bread);
        if (Registries.ITEM.containsId(porkchop)) OTHER.add(insertAt++, porkchop);
        if (Registries.ITEM.containsId(gildingFoundry)) OTHER.add(insertAt++, gildingFoundry);
        if (Registries.ITEM.containsId(rollBench)) OTHER.add(insertAt++, rollBench);
        if (Registries.ITEM.containsId(runicTable)) OTHER.add(insertAt, runicTable);
    }

    private static void reorderArtifacts() {
        Identifier corruptedBeacon = new Identifier(TARGET_NAMESPACE, "corrupted_beacon");
        Identifier updraftTome = new Identifier(TARGET_NAMESPACE, "updraft_tome");
        Identifier harvester = new Identifier(TARGET_NAMESPACE, "harvester");
        Identifier lightningRod = new Identifier(TARGET_NAMESPACE, "lightning_rod");
        Identifier scatterMines = new Identifier(TARGET_NAMESPACE, "scatter_mines");
        Identifier blastFungus = new Identifier(TARGET_NAMESPACE, "blast_fungus");
        Identifier spinblade = new Identifier(TARGET_NAMESPACE, "spinblade");
        Identifier eyeOfGuardian = new Identifier(TARGET_NAMESPACE, "eye_of_the_guardian");
        Identifier corruptedPumpkin = new Identifier(TARGET_NAMESPACE, "corrupted_pumpkin");
        ARTIFACTS.remove(corruptedPumpkin);
        ARTIFACTS.remove(eyeOfGuardian);
        ARTIFACTS.remove(spinblade);
        ARTIFACTS.remove(blastFungus);
        ARTIFACTS.remove(scatterMines);
        ARTIFACTS.remove(lightningRod);
        ARTIFACTS.remove(harvester);
        ARTIFACTS.remove(updraftTome);
        ARTIFACTS.remove(corruptedBeacon);
        if (Registries.ITEM.containsId(corruptedBeacon)) {
            ARTIFACTS.add(0, corruptedBeacon);
        }
        if (Registries.ITEM.containsId(updraftTome)) {
            ARTIFACTS.add(Math.min(1, ARTIFACTS.size()), updraftTome);
        }
        if (Registries.ITEM.containsId(harvester)) {
            ARTIFACTS.add(Math.min(2, ARTIFACTS.size()), harvester);
        }
        if (Registries.ITEM.containsId(lightningRod)) {
            ARTIFACTS.add(Math.min(3, ARTIFACTS.size()), lightningRod);
        }
        if (Registries.ITEM.containsId(scatterMines)) {
            ARTIFACTS.add(Math.min(4, ARTIFACTS.size()), scatterMines);
        }
        if (Registries.ITEM.containsId(blastFungus)) {
            ARTIFACTS.add(Math.min(5, ARTIFACTS.size()), blastFungus);
        }
        if (Registries.ITEM.containsId(spinblade)) {
            ARTIFACTS.add(Math.min(6, ARTIFACTS.size()), spinblade);
        }
        if (Registries.ITEM.containsId(eyeOfGuardian)) {
            ARTIFACTS.add(Math.min(7, ARTIFACTS.size()), eyeOfGuardian);
        }
        if (Registries.ITEM.containsId(corruptedPumpkin)) {
            ARTIFACTS.add(Math.min(8, ARTIFACTS.size()), corruptedPumpkin);
        }
    }

    private static boolean isBowId(String idPath) {
        return idPath.contains("bow") || idPath.contains("arrow");
    }

    private static boolean isDisplayVariantId(String idPath) {
        return idPath.endsWith("_arrow")
                || idPath.endsWith("_firework")
                || idPath.contains("_pulling_")
                || idPath.endsWith("_charged");
    }

    private static boolean isArtifactId(String idPath) {
        if (idPath == null) return false;
        String s = idPath.toLowerCase();
        if (s.equals("corrupted_beacon")
                || s.equals("updraft_tome")
                || s.equals("harvester")
                || s.equals("lightning_rod")
                || s.equals("scatter_mines")
                || s.equals("blast_fungus")
                || s.equals("spinblade")
                || s.equals("eye_of_the_guardian")
                || s.equals("corrupted_pumpkin")) {
            return true;
        }
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
                || s.contains("spinblade")
                || s.contains("harvester")
                || s.contains("powershaker")
                || s.contains("scatter")
                || s.contains("_mine");
    }

    /**
     * Weapon types that belong in melee but should appear at the end of that tab (after swords/axes/etc.).
     */
    private static boolean isMeleeTrailingWeapon(String idPath) {
        if (idPath.contains("armor")) return false;
        return idPath.startsWith("gauntlet_")
                || idPath.startsWith("glaive_")
                || idPath.startsWith("pick_")
                || idPath.startsWith("scythe_")
                || idPath.startsWith("sickle_")
                || idPath.startsWith("spear_")
                || idPath.startsWith("whip_");
    }

    private static boolean isWeaponId(String idPath) {
        // weapons should exclude bows (bows handled by isBowId first)
        if (idPath.contains("armor")) return false; // prevent armor items appearing in melee weapons tab
        return idPath.startsWith("weapon_")
                || idPath.contains("sword")
                || idPath.contains("axe")
                || idPath.contains("dagger")
                || idPath.contains("blade")
                || idPath.contains("hammer")
                || idPath.contains("wand")
                || idPath.contains("staff")
                || idPath.contains("shield");
    }

    /**
     * Attack low → high. Listed weapons with damage modifier 0 go after positive-damage ones.
     * Items without stats JSON (e.g. shields) go last.
     */
    private static void sortMeleeTab() {
        MeleeWeaponStatLookup.ensureLoaded();
        Comparator<Identifier> cmp = (a, b) -> {
            String pa = a.getPath();
            String pb = b.getPath();
            int ga = MeleeWeaponStatLookup.creativeMeleeSortGroup(pa);
            int gb = MeleeWeaponStatLookup.creativeMeleeSortGroup(pb);
            if (ga != gb) {
                return Integer.compare(ga, gb);
            }
            float ta = MeleeWeaponStatLookup.creativeMeleeTotalAttack(pa);
            float tb = MeleeWeaponStatLookup.creativeMeleeTotalAttack(pb);
            int c = Float.compare(ta, tb);
            if (c != 0) {
                return c;
            }
            return pa.compareTo(pb);
        };
        MELEE.sort(cmp);
    }

    private static void sortRangedTab() {
        RangedWeaponStatLookup.ensureLoaded();
        Comparator<Identifier> cmp = (a, b) -> {
            String pa = a.getPath();
            String pb = b.getPath();
            int ga = RangedWeaponStatLookup.creativeRangedSortGroup(pa);
            int gb = RangedWeaponStatLookup.creativeRangedSortGroup(pb);
            if (ga != gb) {
                return Integer.compare(ga, gb);
            }
            double ta = RangedWeaponStatLookup.creativeRangedAttack(pa);
            double tb = RangedWeaponStatLookup.creativeRangedAttack(pb);
            int c = Double.compare(ta, tb);
            if (c != 0) {
                return c;
            }
            return pa.compareTo(pb);
        };
        BOWS.sort(cmp);
    }

    private static void sortArmorTab() {
        ArmorStatLookup.ensureLoaded();
        Comparator<Identifier> cmp = (a, b) -> {
            String pa = a.getPath();
            String pb = b.getPath();

            int ta = ArmorStatLookup.armorSetProtectionTotal(pa);
            int tb = ArmorStatLookup.armorSetProtectionTotal(pb);
            int tc = Integer.compare(ta, tb);
            if (tc != 0) {
                return tc;
            }

            String ba = ArmorStatLookup.armorSetBaseId(pa);
            String bb = ArmorStatLookup.armorSetBaseId(pb);
            int bc = ba.compareTo(bb);
            if (bc != 0) {
                return bc;
            }

            int sa = ArmorStatLookup.armorSlotOrder(pa);
            int sb = ArmorStatLookup.armorSlotOrder(pb);
            if (sa != sb) {
                return Integer.compare(sa, sb);
            }

            int ga = ArmorStatLookup.creativeArmorSortGroup(pa);
            int gb = ArmorStatLookup.creativeArmorSortGroup(pb);
            if (ga != gb) {
                return Integer.compare(ga, gb);
            }

            return pa.compareTo(pb);
        };
        ARMOR.sort(cmp);
    }

    private static boolean isArmorId(String idPath) {
        return idPath.contains("armor")
                || idPath.contains("helmet")
                || idPath.contains("chestplate")
                || idPath.contains("leggings")
                || idPath.contains("boots")
                || idPath.contains("gauntlets");
    }

    public static void toggleDamagingArtifactsFilter() {
        damagingArtifactsFilterEnabled = !damagingArtifactsFilterEnabled;
    }

    public static boolean isDamagingArtifactsFilterEnabled() {
        return damagingArtifactsFilterEnabled;
    }

    private static ItemGroup buildPlaceholderGroup(String displayName, Item iconItem) {
        var builder = FabricItemGroup.builder()
                .icon(() -> new ItemStack(iconItem));

        // FabricItemGroupBuilderImpl requires a display name at runtime,
        // but the expected MutableText parameter type differs across mappings.
        // Use reflection to invoke the right setter robustly.
        try {
            Object literal = Text.literal(displayName);
            Object literalCopy = Text.literal(displayName).copy();

            // Try common method names first.
            String[] candidateMethodNames = new String[]{"title", "displayName", "setTitle", "setDisplayName"};

            for (String methodName : candidateMethodNames) {
                for (Method m : builder.getClass().getMethods()) {
                    if (!methodName.equals(m.getName())) continue;
                    if (m.getParameterCount() != 1) continue;
                    try {
                        m.invoke(builder, literal);
                        return builder.build();
                    } catch (Throwable ignored) {
                        // try other candidate below
                    }
                    try {
                        m.invoke(builder, literalCopy);
                        return builder.build();
                    } catch (Throwable ignored) {
                        // keep searching
                    }
                }
            }

            // Fallback: try fields (name contains display/title).
            for (var f : builder.getClass().getDeclaredFields()) {
                String name = f.getName().toLowerCase();
                if (!(name.contains("display") || name.contains("title"))) continue;
                f.setAccessible(true);
                try {
                    f.set(builder, literal);
                    return builder.build();
                } catch (Throwable ignored) {
                    // try next
                }
                try {
                    f.set(builder, literalCopy);
                    return builder.build();
                } catch (Throwable ignored) {
                    // ignore and continue
                }
            }
        } catch (Throwable ignored) {
            // fallthrough to build (will throw if name is still unset)
        }

        return builder.build();
    }
}

