package mc_javaedition.forge.placeholder;

import mc_javaedition.combat.MeleeWeaponStatLookup;
import mc_javaedition.combat.RangedWeaponStatLookup;
import mc_javaedition.combat.ArmorStatLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Forge creative tab for placeholder items.
 *
 * Tabs (user requested):
 * 1) melee
 * 2) ranged
 * 3) armor
 * 4) artifacts ("神器" - mcdar namespace)
 * 5) other
 */
public final class PlaceholderCreativeTabForge {
    private static final String LIST_RESOURCE = "/placeholder_items.txt";
    private static final String MOD_ID = "mcd_java";
    private static final String TARGET_NAMESPACE = "mcdjava";

    private static boolean initialized = false;

    /** Melee tab: attack low → high; no-damage-modifier & unlisted last. */
    private static final List<ResourceLocation> MELEE = new ArrayList<>();
    private static final List<ResourceLocation> BOWS = new ArrayList<>();
    private static final List<ResourceLocation> ARMOR = new ArrayList<>();
    private static final List<ResourceLocation> ARTIFACTS = new ArrayList<>();
    private static final List<ResourceLocation> OTHER = new ArrayList<>();
    private static final List<ResourceLocation> DAMAGING_ARTIFACTS = List.of(
            new ResourceLocation(TARGET_NAMESPACE, "corrupted_beacon"),
            new ResourceLocation(TARGET_NAMESPACE, "updraft_tome"),
            new ResourceLocation(TARGET_NAMESPACE, "harvester"),
            new ResourceLocation(TARGET_NAMESPACE, "lightning_rod"),
            new ResourceLocation(TARGET_NAMESPACE, "scatter_mines"),
            new ResourceLocation(TARGET_NAMESPACE, "blast_fungus"),
            new ResourceLocation(TARGET_NAMESPACE, "spinblade"),
            new ResourceLocation(TARGET_NAMESPACE, "eye_of_the_guardian"),
            new ResourceLocation(TARGET_NAMESPACE, "corrupted_pumpkin")
    );
    private static final List<ResourceLocation> STATUS_ARTIFACTS = List.of(
            new ResourceLocation(TARGET_NAMESPACE, "corrupted_seeds"),
            new ResourceLocation(TARGET_NAMESPACE, "fishing_rod"),
            new ResourceLocation(TARGET_NAMESPACE, "gong_of_weakening"),
            new ResourceLocation(TARGET_NAMESPACE, "ice_wand"),
            new ResourceLocation(TARGET_NAMESPACE, "love_medallion"),
            new ResourceLocation(TARGET_NAMESPACE, "satchel_of_elements"),
            new ResourceLocation(TARGET_NAMESPACE, "shock_powder")
    );

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> PLACEHOLDER_WEAPONS =
            TABS.register("placeholders_01_melee", () -> CreativeModeTab.builder()
                    .title(Component.literal("MC Dungeons (Placeholder Items) - Melee"))
                    .icon(() -> new ItemStack(Items.IRON_SWORD))
                    .displayItems((parameters, output) -> {
                        ensureInitialized();
                        addCategory(output, MELEE);
                    })
                    .build()
            );

    public static final RegistryObject<CreativeModeTab> PLACEHOLDER_BOWS =
            TABS.register("placeholders_02_ranged", () -> CreativeModeTab.builder()
                    .title(Component.literal("MC Dungeons (Placeholder Items) - Ranged"))
                    .icon(() -> new ItemStack(Items.BOW))
                    .displayItems((parameters, output) -> {
                        ensureInitialized();
                        addCategory(output, BOWS);
                    })
                    .build()
            );

    public static final RegistryObject<CreativeModeTab> PLACEHOLDER_ARMOR =
            TABS.register("placeholders_03_armor", () -> CreativeModeTab.builder()
                    .title(Component.literal("MC Dungeons (Placeholder Items) - Armor"))
                    .icon(() -> new ItemStack(Items.IRON_CHESTPLATE))
                    .displayItems((parameters, output) -> {
                        ensureInitialized();
                        addCategory(output, ARMOR);
                    })
                    .build()
            );

    public static final RegistryObject<CreativeModeTab> PLACEHOLDER_ARTIFACTS =
            TABS.register("placeholders_04_artifacts", () -> CreativeModeTab.builder()
                    .title(Component.literal("MC Dungeons (Placeholder Items) - Artifacts"))
                    .icon(() -> new ItemStack(Items.ANVIL))
                    .displayItems((parameters, output) -> {
                        ensureInitialized();
                        addArtifactsCategory(output);
                    })
                    .build()
            );

    public static final RegistryObject<CreativeModeTab> PLACEHOLDER_OTHER =
            TABS.register("placeholders_05_other", () -> CreativeModeTab.builder()
                    .title(Component.literal("MC Dungeons (Placeholder Items) - Other"))
                    .icon(() -> new ItemStack(Items.BOOK))
                    .displayItems((parameters, output) -> {
                        ensureInitialized();
                        addCategory(output, OTHER);
                    })
                    .build()
            );

    private PlaceholderCreativeTabForge() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }

    private static void addCategory(CreativeModeTab.Output output, List<ResourceLocation> ids) {
        for (ResourceLocation id : ids) {
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                // Keep manual order stable even when a target artifact is missing.
                output.accept(Items.BARRIER.getDefaultInstance());
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            ItemStack stack = item.getDefaultInstance();
            output.accept(stack);
        }
    }

    private static void addArtifactsCategory(CreativeModeTab.Output output) {
        for (ResourceLocation id : ARTIFACTS) {
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                output.accept(Items.BARRIER.getDefaultInstance());
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (!(item instanceof PlaceholderArtifactForge)) {
                output.accept(Items.BARRIER.getDefaultInstance());
                continue;
            }
            output.accept(item.getDefaultInstance());
        }
    }

    private static void loadAndCategorize() {
        MELEE.clear();
        BOWS.clear();
        ARMOR.clear();
        ARTIFACTS.clear();
        OTHER.clear();
        Map<String, String> chosenNamespaceByPath = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                PlaceholderCreativeTabForge.class.getResourceAsStream(LIST_RESOURCE),
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
                ResourceLocation fullId = new ResourceLocation(TARGET_NAMESPACE, idPath);

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
        } catch (Exception ignored) {
            // If the file is missing, we just keep the tab empty.
        }
    }

    private static void reorderOtherItems() {
        ResourceLocation apple = new ResourceLocation(TARGET_NAMESPACE, "apple");
        ResourceLocation bread = new ResourceLocation(TARGET_NAMESPACE, "bread");
        ResourceLocation porkchop = new ResourceLocation(TARGET_NAMESPACE, "porkchop");
        ResourceLocation gildingFoundry = new ResourceLocation(TARGET_NAMESPACE, "gilding_foundry");
        ResourceLocation rollBench = new ResourceLocation(TARGET_NAMESPACE, "roll_bench");
        ResourceLocation runicTable = new ResourceLocation(TARGET_NAMESPACE, "runic_table");

        OTHER.remove(apple);
        OTHER.remove(bread);
        OTHER.remove(porkchop);
        OTHER.remove(gildingFoundry);
        OTHER.remove(rollBench);
        OTHER.remove(runicTable);

        int insertAt = 0;
        if (BuiltInRegistries.ITEM.containsKey(apple)) OTHER.add(insertAt++, apple);
        if (BuiltInRegistries.ITEM.containsKey(bread)) OTHER.add(insertAt++, bread);
        if (BuiltInRegistries.ITEM.containsKey(porkchop)) OTHER.add(insertAt++, porkchop);
        if (BuiltInRegistries.ITEM.containsKey(gildingFoundry)) OTHER.add(insertAt++, gildingFoundry);
        if (BuiltInRegistries.ITEM.containsKey(rollBench)) OTHER.add(insertAt++, rollBench);
        if (BuiltInRegistries.ITEM.containsKey(runicTable)) OTHER.add(insertAt, runicTable);
    }

    private static void reorderArtifacts() {
        for (ResourceLocation id : DAMAGING_ARTIFACTS) {
            ARTIFACTS.remove(id);
        }
        for (ResourceLocation id : STATUS_ARTIFACTS) {
            ARTIFACTS.remove(id);
        }

        int insertAt = 0;
        for (ResourceLocation id : DAMAGING_ARTIFACTS) {
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                ARTIFACTS.add(Math.min(insertAt, ARTIFACTS.size()), id);
                insertAt++;
            }
        }
        for (ResourceLocation id : STATUS_ARTIFACTS) {
            if (BuiltInRegistries.ITEM.containsKey(id)) {
                ARTIFACTS.add(Math.min(insertAt, ARTIFACTS.size()), id);
                insertAt++;
            }
        }
    }

    private static void ensureInitialized() {
        if (initialized) return;
        initialized = true;
        loadAndCategorize();
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
        // weapons exclude bows (handled by isBowId first)
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

    private static void sortMeleeTab() {
        MeleeWeaponStatLookup.ensureLoaded();
        Comparator<ResourceLocation> cmp = (a, b) -> {
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
        Comparator<ResourceLocation> cmp = (a, b) -> {
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
        Comparator<ResourceLocation> cmp = (a, b) -> {
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
}

