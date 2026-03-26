package mc_javaedition.fabric.placeholder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class PlaceholderMeleeWeaponFabric extends SwordItem {
    private static final String PROGRESS_KEY = "mcdjava_progress_points";
    private static final int MAX_PROGRESS = 200;

    private final String namespace;
    private final String path;

    public PlaceholderMeleeWeaponFabric(String namespace, String path, ToolMaterial tier, int attackDamage, float attackSpeed) {
        super(tier, attackDamage, attackSpeed, new Settings().maxDamage(tier.getDurability()));
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(prettyName(namespace, path));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.postHit(stack, target, attacker);
        if (attacker.getWorld().isClient()) {
            return result;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        int progress = Math.max(0, nbt.getInt(PROGRESS_KEY));

        // Progression damage points: every point grants a small bonus, capped for balance.
        float bonusDamage = Math.min(progress * 0.03f, 4.0f);
        if (bonusDamage > 0.0f) {
            target.damage(attacker.getDamageSources().mobAttack(attacker), bonusDamage);
        }

        if (!target.isAlive() && attacker instanceof PlayerEntity) {
            nbt.putInt(PROGRESS_KEY, Math.min(MAX_PROGRESS, progress + 1));
        }
        return result;
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
