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
 * Loads per-item ranged stats for placeholder MCDW ranged weapons from
 * {@code /mcdjava_ranged_weapon_stats.json}.
 */
public final class RangedWeaponStatLookup {

    public record Stats(String tier, double projectileDamage, int drawSpeed, float range) {}

    private static final Stats FALLBACK_DEFAULTS = new Stats("IRON", 0.0, 20, 16.0f);

    private static volatile boolean loaded;
    private static Stats defaults = FALLBACK_DEFAULTS;
    private static Map<String, Stats> byId = Map.of();

    private RangedWeaponStatLookup() {}

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (RangedWeaponStatLookup.class) {
            if (loaded) {
                return;
            }
            loadLocked();
            loaded = true;
        }
    }

    private static void loadLocked() {
        try (InputStream in = RangedWeaponStatLookup.class.getResourceAsStream("/mcdjava_ranged_weapon_stats.json")) {
            if (in == null) {
                defaults = FALLBACK_DEFAULTS;
                byId = Map.of();
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            defaults = parseStats(root.get("defaults"), FALLBACK_DEFAULTS);
            JsonObject weapons = root.getAsJsonObject("weapons");
            Map<String, Stats> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> e : weapons.entrySet()) {
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
        String tier = o.has("tier") ? o.get("tier").getAsString() : fallback.tier();
        double projectileDamage = o.has("projectileDamage") ? o.get("projectileDamage").getAsDouble() : fallback.projectileDamage();
        int drawSpeed = o.has("drawSpeed") ? o.get("drawSpeed").getAsInt() : fallback.drawSpeed();
        float range = o.has("range") ? o.get("range").getAsFloat() : fallback.range();
        return new Stats(tier, projectileDamage, drawSpeed, range);
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

    public static int tierDurability(String tierName) {
        if (tierName == null) {
            return 250;
        }
        return switch (tierName.toUpperCase(Locale.ROOT)) {
            case "WOOD" -> 59;
            case "STONE" -> 131;
            case "IRON", "COPPER" -> 250;
            case "GOLD" -> 32;
            case "DIAMOND" -> 1561;
            case "NETHERITE" -> 2031;
            default -> 250;
        };
    }

    public static int creativeRangedSortGroup(String idPath) {
        ensureLoaded();
        if (idPath == null || !byId.containsKey(idPath.toLowerCase(Locale.ROOT))) {
            return 2;
        }
        return creativeRangedAttack(idPath) > 0.0 ? 0 : 1;
    }

    public static double creativeRangedAttack(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return 0.0;
        }
        Stats st = byId.get(idPath.toLowerCase(Locale.ROOT));
        if (st == null) {
            return 0.0;
        }
        return st.projectileDamage();
    }
}
