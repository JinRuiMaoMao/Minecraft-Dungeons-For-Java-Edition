package mc_javaedition.combat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps armor item set id (e.g. "wolf_armor") to armor texture key
 * (e.g. "wolf"), based on MCDA ArmorSets textureName/setName mapping.
 */
public final class ArmorTextureLookup {
    private static volatile boolean loaded;
    private static Map<String, String> bySetName = Map.of();

    private ArmorTextureLookup() {}

    public static void ensureLoaded() {
        if (loaded) return;
        synchronized (ArmorTextureLookup.class) {
            if (loaded) return;
            loadLocked();
            loaded = true;
        }
    }

    private static void loadLocked() {
        try (InputStream in = ArmorTextureLookup.class.getResourceAsStream("/mcdjava_armor_texture_map.json")) {
            if (in == null) {
                bySetName = Map.of();
                return;
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject sets = root.getAsJsonObject("sets");
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> e : sets.entrySet()) {
                map.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue().getAsString());
            }
            bySetName = Map.copyOf(map);
        } catch (Exception ignored) {
            bySetName = Map.of();
        }
    }

    public static String textureNameForArmorId(String idPath) {
        ensureLoaded();
        if (idPath == null) return "placeholder";
        String base = idPath;
        if (base.endsWith("_helmet")) base = base.substring(0, base.length() - "_helmet".length());
        else if (base.endsWith("_chestplate")) base = base.substring(0, base.length() - "_chestplate".length());
        else if (base.endsWith("_leggings")) base = base.substring(0, base.length() - "_leggings".length());
        else if (base.endsWith("_boots")) base = base.substring(0, base.length() - "_boots".length());
        return bySetName.getOrDefault(base.toLowerCase(Locale.ROOT), base);
    }
}
