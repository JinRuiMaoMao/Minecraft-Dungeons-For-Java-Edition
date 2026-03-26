package mc_javaedition.forge.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Placeholder item that guarantees a readable name for /give and creative tabs.
 *
 * This avoids depending on language JSON correctness during migration.
 */
public class PlaceholderNamedItemForge extends Item {
    private final String namespace;
    private final String path;
    private final Potion potionToApply; // null => normal item/food

    public PlaceholderNamedItemForge(String namespace, String path, Item.Properties settings, Potion potionToApply) {
        super(settings);
        this.namespace = namespace;
        this.path = path;
        this.potionToApply = potionToApply;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(prettyName(namespace, path));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (potionToApply == null) {
            return super.use(world, user, hand);
        }

        if (!world.isClientSide) {
            ItemStack temp = new ItemStack(Items.POTION);
            PotionUtils.setPotion(temp, potionToApply);
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(temp);
            for (MobEffectInstance effect : effects) {
                // Apply vanilla potion effects directly.
                user.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier()));
            }

            if (!user.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        user.swing(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
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

}

