package mc_javaedition.legacy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public final class LegacyCreeperWoodsItemsRegistry {
    private static final String LEGACY_NAMESPACE = "mcd_java";

    private static final String[] LEGACY_ITEM_IDS = new String[] {
            "apple", "archerarmorhelmet", "bootsofswiftness", "bread", "deathcapmushroom", "firewor",
            "foxarmorbody", "foxarmorboots", "foxarmorhelmet", "healthpot", "highlandarmorbody",
            "highlandarmorhelmet", "hunterarmorbody", "nothing", "porkchop", "potionhealth", "potionspeed",
            "scalemailarmorbody", "shadowbrew", "strengthpotion", "tnt", "wolfsarmorbody", "wolfsarmorboots",
            "wolfsarmorhelmet"
    };

    private LegacyCreeperWoodsItemsRegistry() {}

    public static void register() {
        for (String id : LEGACY_ITEM_IDS) {
            registerItem(id);
        }
    }

    private static void registerItem(String path) {
        Object id = createId(LEGACY_NAMESPACE, path);
        boolean stackSizeOne = path.contains("armor")
                || path.contains("potion")
                || "healthpot".equals(path)
                || "shadowbrew".equals(path);

        Object item = createLegacyItem(stackSizeOne);
        registerToMinecraftRegistry("ITEM", id, item);
    }

    private static void registerToMinecraftRegistry(String fieldName, Object id, Object value) {
        if (tryRegister("net.minecraft.core.Registry", "net.minecraft.core.registries.Registries", fieldName, id, value)) {
            return;
        }
        if (tryRegister("net.minecraft.registry.Registry", "net.minecraft.registry.Registries", fieldName, id, value)) {
            return;
        }
        throw new IllegalStateException("Unable to register legacy entry: " + fieldName + " " + id);
    }

    private static boolean tryRegister(String registryClassName, String registriesClassName, String fieldName, Object id, Object value) {
        try {
            Class<?> registryClass = Class.forName(registryClassName);
            Class<?> registriesClass = Class.forName(registriesClassName);

            Field registryField = registriesClass.getField(fieldName);
            Object registryHolder = registryField.get(null);

            Method registerMethod = null;
            for (Method m : registryClass.getMethods()) {
                if ("register".equals(m.getName()) && m.getParameterCount() == 3) {
                    registerMethod = m;
                    break;
                }
            }
            if (registerMethod == null) {
                return false;
            }

            registerMethod.invoke(null, registryHolder, id, value);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Throwable t) {
            throw new RuntimeException("Legacy registry reflection failed for field=" + fieldName + ", id=" + id, t);
        }
    }

    private static Object createId(String namespace, String path) {
        String[] idClassCandidates = new String[]{
                "net.minecraft.util.Identifier",
                "net.minecraft.resources.ResourceLocation"
        };
        for (String idClassName : idClassCandidates) {
            try {
                Class<?> idClass = Class.forName(idClassName);
                return idClass.getConstructor(String.class, String.class).newInstance(namespace, path);
            } catch (ClassNotFoundException e) {
                // try next candidate
            } catch (Throwable t) {
                throw new RuntimeException("Failed to create legacy id using " + idClassName, t);
            }
        }
        throw new IllegalStateException("No compatible identifier class found for legacy namespace");
    }

    private static Object createLegacyItem(boolean stackSizeOne) {
        // Prefer runtime (official) item classes.
        Object item = tryCreateItem(
                "net.minecraft.world.item.Item",
                "net.minecraft.world.item.Item$Properties",
                stackSizeOne
        );
        if (item != null) {
            return item;
        }

        // Fallback to yarn/Fabric naming if runtime classes aren't present.
        item = tryCreateItem(
                "net.minecraft.item.Item",
                "net.minecraft.item.Item$Settings",
                stackSizeOne
        );
        if (item != null) {
            return item;
        }

        throw new IllegalStateException("Unable to create legacy item via reflection");
    }

    private static Object tryCreateItem(String itemClassName, String propsClassName, boolean stackSizeOne) {
        try {
            Class<?> itemClass = Class.forName(itemClassName);
            Class<?> propsClass = Class.forName(propsClassName);

            Object props = propsClass.getConstructor().newInstance();
            if (stackSizeOne) {
                // runtime method name: stacksTo(int) for Item$Properties
                try {
                    Method stacksTo = propsClass.getMethod("stacksTo", int.class);
                    stacksTo.invoke(props, 1);
                } catch (NoSuchMethodException ignored) {
                    // yarn fallback might use different method names; ignore.
                }
            }

            Constructor<?> ctor = null;
            for (Constructor<?> c : itemClass.getConstructors()) {
                if (c.getParameterCount() == 1) {
                    ctor = c;
                    break;
                }
            }
            if (ctor == null) {
                return null;
            }
            return ctor.newInstance(props);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to create legacy item: " + itemClassName, t);
        }
    }
}
