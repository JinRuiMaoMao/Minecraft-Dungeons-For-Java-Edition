package mc_javaedition.forge.placeholder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class PlaceholderBowForge extends BowItem {
    private final String namespace;
    private final String path;
    private final double projectileDamage;
    private final int drawSpeed;
    private final float range;

    public PlaceholderBowForge(String namespace, String path, int durability, double projectileDamage, int drawSpeed, float range) {
        super(new Properties().stacksTo(1).durability(durability));
        this.namespace = namespace;
        this.path = path;
        this.projectileDamage = projectileDamage;
        this.drawSpeed = drawSpeed;
        this.range = range;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(prettyName(namespace, path));
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
