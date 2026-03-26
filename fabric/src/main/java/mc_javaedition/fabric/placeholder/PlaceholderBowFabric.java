package mc_javaedition.fabric.placeholder;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PlaceholderBowFabric extends BowItem {
    private final String namespace;
    private final String path;
    private final double projectileDamage;
    private final int drawSpeed;
    private final float range;

    public PlaceholderBowFabric(String namespace, String path, int maxDamage, double projectileDamage, int drawSpeed, float range) {
        super(new Settings().maxCount(1).maxDamage(maxDamage));
        this.namespace = namespace;
        this.path = path;
        this.projectileDamage = projectileDamage;
        this.drawSpeed = drawSpeed;
        this.range = range;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(prettyName(namespace, path));
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
