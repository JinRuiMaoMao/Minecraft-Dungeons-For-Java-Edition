package mc_javaedition.forge.legacy;

import mc_javaedition.forge.McJavaEditionForge;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Minimal legacy items registration for Forge dev runtime.
 *
 * We intentionally keep this very small: just enough to verify
 * `/give @s mcd_java:<id>` works and items appear in the creative inventory.
 */
public final class LegacyCreeperWoodsItemsRegistryForge {
    private static final String MOD_ID = McJavaEditionForge.MOD_ID;

    // Forge registry is already frozen after the mod lifecycle starts,
    // so we must use DeferredRegister to register during the correct phase.
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final RegistryObject<Item> APPLE = ITEMS.register("apple", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BREAD = ITEMS.register("bread", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PORKCHOP = ITEMS.register("porkchop", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEALTH_POTION = ITEMS.register(
            "healthpot",
            () -> new Item(new Item.Properties())
    );

    private LegacyCreeperWoodsItemsRegistryForge() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}

