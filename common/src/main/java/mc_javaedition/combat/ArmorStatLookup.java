package mc_javaedition.combat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loads per-item armor stats for placeholder MCDA armor pieces from
 * {@code /mcdjava_armor_stats.json}.
 */
public final class ArmorStatLookup {

    public record Stats(int protection, int durability, float toughness, float knockback) {}

    private static final Stats FALLBACK_DEFAULTS = new Stats(0, 165, 0.0f, 0.0f);

    private static volatile boolean loaded;
    private static Stats defaults = FALLBACK_DEFAULTS;
    private static Map<String, Stats> byId = Map.of();

    private ArmorStatLookup() {}

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (ArmorStatLookup.class) {
            if (loaded) {
                return;
            }
            loadLocked();
            loaded = true;
        }
    }

    private static void loadLocked() {
        try (InputStream in = ArmorStatLookup.class.getResourceAsStream("/mcdjava_armor_stats.json")) {
            if (in == null) {
                defaults = FALLBACK_DEFAULTS;
                byId = Map.of();
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            defaults = parseStats(root.get("defaults"), FALLBACK_DEFAULTS);
            JsonObject items = root.getAsJsonObject("items");
            Map<String, Stats> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : items.entrySet()) {
                if (!e.getValue().isJsonObject()) {
                    continue;
                }
                map.put(e.getKey().toLowerCase(Locale.ROOT), parseStats(e.getValue(), defaults));
            }
            byId = Collections.unmodifiableMap(map);
        } catch (Exception ignored) {
            defaults = FALLBACK_DEFAULTS;
            byId = Map.of();
        }
    }

    private static Stats parseStats(JsonElement el, Stats fallback) {
        if (el == null || !el.isJsonObject()) {
            return fallback;
        }
        JsonObject o = el.getAsJsonObject();
        int protection = o.has("protection") ? o.get("protection").getAsInt() : fallback.protection();
        int durability = o.has("durability") ? o.get("durability").getAsInt() : fallback.durability();
        float toughness = o.has("toughness") ? o.get("toughness").getAsFloat() : fallback.toughness();
        float knockback = o.has("knockback") ? o.get("knockback").getAsFloat() : fallback.knockback();
        return new Stats(protection, durability, toughness, knockback);
    }

    public static Stats get(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return defaults;
        }
        return byId.getOrDefault(idPath.toLowerCase(Locale.ROOT), defaults);
    }

    public static boolean hasExplicitStats(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return false;
        }
        return byId.containsKey(idPath.toLowerCase(Locale.ROOT));
    }

    public static int creativeArmorSortGroup(String idPath) {
        ensureLoaded();
        if (!hasExplicitStats(idPath)) {
            return 2;
        }
        return get(idPath).protection() > 0 ? 0 : 1;
    }

    public static int creativeArmorProtection(String idPath) {
        return get(idPath).protection();
    }

    public static int armorSlotOrder(String idPath) {
        if (idPath == null) {
            return 99;
        }
        if (idPath.endsWith("_helmet")) return 0;
        if (idPath.endsWith("_chestplate")) return 1;
        if (idPath.endsWith("_leggings")) return 2;
        if (idPath.endsWith("_boots")) return 3;
        return 99;
    }

    public static String armorSetBaseId(String idPath) {
        if (idPath == null) {
            return "";
        }
        if (idPath.endsWith("_helmet")) return idPath.substring(0, idPath.length() - "_helmet".length());
        if (idPath.endsWith("_chestplate")) return idPath.substring(0, idPath.length() - "_chestplate".length());
        if (idPath.endsWith("_leggings")) return idPath.substring(0, idPath.length() - "_leggings".length());
        if (idPath.endsWith("_boots")) return idPath.substring(0, idPath.length() - "_boots".length());
        return idPath;
    }

    /**
     * Sum of 4-piece protection (helmet+chestplate+leggings+boots) for ordering armor sets.
     * If any piece is missing from stats, returns Integer.MAX_VALUE so incomplete entries sink to the end.
     */
    public static int armorSetProtectionTotal(String idPath) {
        ensureLoaded();
        String base = armorSetBaseId(idPath);
        String h = base + "_helmet";
        String c = base + "_chestplate";
        String l = base + "_leggings";
        String b = base + "_boots";
        if (!hasExplicitStats(h) || !hasExplicitStats(c) || !hasExplicitStats(l) || !hasExplicitStats(b)) {
            return Integer.MAX_VALUE;
        }
        return get(h).protection() + get(c).protection() + get(l).protection() + get(b).protection();
    }
}
