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
 * Loads per-item melee stats for placeholder MCDW weapons from
 * {@code /mcdjava_melee_weapon_stats.json} (generated from MCDungeonsWeapons-1.20 enums).
 */
public final class MeleeWeaponStatLookup {

    public record Stats(String tier, int damage, float attackSpeed) {}

    private static final Stats FALLBACK_DEFAULTS = new Stats("IRON", 5, -2.4f);

    private static volatile boolean loaded;
    private static Stats defaults = FALLBACK_DEFAULTS;
    private static Map<String, Stats> byId = Map.of();

    private MeleeWeaponStatLookup() {}

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (MeleeWeaponStatLookup.class) {
            if (loaded) {
                return;
            }
            loadLocked();
            loaded = true;
        }
    }

    private static void loadLocked() {
        try (InputStream in = MeleeWeaponStatLookup.class.getResourceAsStream("/mcdjava_melee_weapon_stats.json")) {
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
        int damage = o.has("damage") ? o.get("damage").getAsInt() : fallback.damage();
        float attackSpeed = o.has("attackSpeed") ? o.get("attackSpeed").getAsFloat() : fallback.attackSpeed();
        return new Stats(tier, damage, attackSpeed);
    }

    public static Stats get(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return defaults;
        }
        return byId.getOrDefault(idPath.toLowerCase(Locale.ROOT), defaults);
    }

    /** True if {@code idPath} has an explicit entry in {@code mcdjava_melee_weapon_stats.json}. */
    public static boolean hasExplicitStats(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return false;
        }
        return byId.containsKey(idPath.toLowerCase(Locale.ROOT));
    }

    /**
     * Vanilla-style attack damage from tool tier only (added to item damage modifier for swords).
     * Matches 1.20.x Java tool materials.
     */
    public static int tierAttackDamageBonus(String tierName) {
        if (tierName == null) {
            return 2;
        }
        return switch (tierName.toUpperCase(Locale.ROOT)) {
            case "WOOD" -> 0;
            case "STONE" -> 1;
            case "IRON", "COPPER" -> 2;
            case "GOLD" -> 0;
            case "DIAMOND" -> 3;
            case "NETHERITE" -> 4;
            default -> 2;
        };
    }

    /**
     * Sort key for creative melee tab:
     * 0 = listed weapon with total attack > 0,
     * 1 = listed weapon with total attack <= 0,
     * 2 = not listed in stats JSON (shield etc.).
     */
    public static int creativeMeleeSortGroup(String idPath) {
        ensureLoaded();
        if (idPath == null || !byId.containsKey(idPath.toLowerCase(Locale.ROOT))) {
            return 2;
        }
        float total = creativeMeleeTotalAttack(idPath);
        return total > 0f ? 0 : 1;
    }

    /** Total attack rating for sorting (tier bonus + config damage); only meaningful when {@link #hasExplicitStats(String)}. */
    public static float creativeMeleeTotalAttack(String idPath) {
        ensureLoaded();
        if (idPath == null) {
            return 0f;
        }
        Stats st = byId.get(idPath.toLowerCase(Locale.ROOT));
        if (st == null) {
            return 0f;
        }
        return tierAttackDamageBonus(st.tier()) + st.damage();
    }
}
