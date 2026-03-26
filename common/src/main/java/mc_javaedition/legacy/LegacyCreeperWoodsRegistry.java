package mc_javaedition.legacy;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.piston.PistonBehavior;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class LegacyCreeperWoodsRegistry {
    private static final String LEGACY_NAMESPACE = "mcd_java";

    private static final String[] LEGACY_BLOCK_IDS = new String[] {
            "bigwell", "blueurn", "chiseledstonecube", "chiseledstonecubemossy", "chiseledstoneplus",
            "chiseledstoneplusdirt", "chiseledstoneplusdirty", "cobblestonefloordirty", "cobblestonefloordirty2",
            "cobblestonefloordirty3", "cobblestonemossy", "cobblestonemossy2", "crackedandesite", "cwandesite",
            "cwchiseledstonebrick", "cwcobblestone", "cwcrackedmossystone", "cwcrackedstonebrick", "cwdarkerdirt",
            "cwdirt", "cwflowers", "cwglowmushroom", "cwgrass", "cwgrassblock", "cwgrassdirt", "cwgrasspath",
            "cwleaves", "cwmossyandesite", "cwmossycobblestone", "cwmossycobblestone2", "cwmossydirt", "cwmossystone",
            "cwmossystonebricks", "cwmossystonebricks2", "cwstone", "cwstonebricks", "cwtallgrassbottom", "cwtallgrassup",
            "darkerstone", "diamondchest", "dirt", "dirtmossy", "dirtmossystone", "dirtpath", "dirtygranite",
            "dirtystonebricksskeleton", "emeraldchest", "fancytest", "floorcobweb", "glowingplantblock", "glowingplants",
            "grass", "grassdirt", "grave", "lightbblue", "lightbwhite", "lightbyellow", "mcdtent", "skeletonchest",
            "stonefloor", "stonefloordirty", "stonefloordirty2", "stonefloordirty3", "stonefloorpath", "stonefloorpath2",
            "stonefloorpath3", "stonefloorpath4", "stonefloorpathdirtstone", "stonefloorpathstone", "stonegranitesmooth",
            "woodenchest"
    };

    private LegacyCreeperWoodsRegistry() {}

    public static void register() {
        for (String id : LEGACY_BLOCK_IDS) {
            registerBlockWithItem(id);
        }
    }

    private static void registerBlockWithItem(String path) {
        Object id = createId(LEGACY_NAMESPACE, path);

        Block block = new Block(AbstractBlock.Settings.create()
                .strength(1.5f, 6.0f)
                .sounds(BlockSoundGroup.STONE)
                .pistonBehavior(PistonBehavior.NORMAL));

        // Forge runtime (official mappings) uses core registries; Fabric/common compile (yarn) might use registry.*.
        // Reflection keeps this compatible across mapping namespaces.
        registerToMinecraftRegistry("BLOCK", id, block);
        registerToMinecraftRegistry("ITEM", id, new BlockItem(block, new Item.Settings()));
    }

    private static void registerToMinecraftRegistry(String fieldName, Object id, Object value) {
        // Try Forge/core first, then Fabric/yarn.
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
            return false; // this mapping side doesn't have these classes
        } catch (Throwable t) {
            throw new RuntimeException("Legacy registry reflection failed for field=" + fieldName + ", id=" + id, t);
        }
    }

    private static Object createId(String namespace, String path) {
        // Yarn compile side: net.minecraft.util.Identifier
        // Forge runtime side: net.minecraft.resources.ResourceLocation
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
}
