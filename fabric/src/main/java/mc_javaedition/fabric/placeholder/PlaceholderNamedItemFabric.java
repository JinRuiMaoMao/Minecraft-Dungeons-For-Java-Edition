package mc_javaedition.fabric.placeholder;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.lang.reflect.Method;

/**
 * Placeholder item that guarantees a readable name for /give and creative tabs.
 *
 * We do not rely on language JSON being valid for every item.
 */
public class PlaceholderNamedItemFabric extends Item {
    private final String namespace;
    private final String path;
    private final Potion potionToApply; // null => normal item/food

    public PlaceholderNamedItemFabric(String namespace, String path, Item.Settings settings, Potion potionToApply) {
        super(settings);
        this.namespace = namespace;
        this.path = path;
        this.potionToApply = potionToApply;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(prettyName(namespace, path));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (potionToApply == null) {
            return super.use(world, user, hand);
        }

        if (!world.isClient) {
            ItemStack temp = new ItemStack(Items.POTION);
            PotionUtil.setPotion(temp, potionToApply);
            List<StatusEffectInstance> effects = tryGetPotionEffects(temp);
            if (effects != null && !effects.isEmpty()) {
                for (StatusEffectInstance effect : effects) user.addStatusEffect(effect);
            }

            if (!user.isCreative()) {
                tryConsumeStack(stack, 1);
            }
        }

        user.swingHand(hand);
        return new TypedActionResult<>(ActionResult.SUCCESS, stack);
    }

    private static String prettyName(String namespace, String path) {
        String base = path.replace('/', ' ').replace('_', ' ').trim();
        StringBuilder sb = new StringBuilder();
        boolean cap = true;
        for (int i = 0; i < base.length(); i++) {
            char c = base.charAt(i);
            if (Character.isWhitespace(c)) {
                sb.append(' ');
                cap = true;
                continue;
            }
            if (cap) {
                sb.append(Character.toUpperCase(c));
                cap = false;
            } else {
                sb.append(c);
            }
        }
        return sb + " (" + namespace + ")";
    }

    @SuppressWarnings("unchecked")
    private static List<StatusEffectInstance> tryGetPotionEffects(ItemStack temp) {
        try {
            // We avoid relying on a single mapped method name (Fabric/Yarn variants).
            for (Method m : PotionUtil.class.getMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                if (m.getParameterCount() != 1) continue;
                if (!m.getParameterTypes()[0].isAssignableFrom(ItemStack.class)) continue;

                String name = m.getName().toLowerCase();
                // Candidate names: getMobEffects, getPotionEffects, getEffectsFromStack, ...
                if (!(name.contains("effect") || name.contains("mob"))) continue;

                Object result = m.invoke(null, temp);
                if (result instanceof List<?> list) {
                    if (list.isEmpty() || list.get(0) instanceof StatusEffectInstance) {
                        return (List<StatusEffectInstance>) list;
                    }
                }
            }
        } catch (Throwable ignored) {
            // ignore
        }
        return null;
    }

    private static void tryConsumeStack(ItemStack stack, int amount) {
        try {
            // Yarn-like name.
            Method m = ItemStack.class.getMethod("decrement", int.class);
            m.invoke(stack, amount);
            return;
        } catch (Throwable ignored) {
            // ignore
        }

        try {
            // Mojang-like name.
            Method m = ItemStack.class.getMethod("shrink", int.class);
            m.invoke(stack, amount);
        } catch (Throwable ignored) {
            // ignore
        }
    }
}

