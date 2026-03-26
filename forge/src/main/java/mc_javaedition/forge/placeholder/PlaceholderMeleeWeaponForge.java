package mc_javaedition.forge.placeholder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class PlaceholderMeleeWeaponForge extends SwordItem {
    private static final String PROGRESS_KEY = "mcdjava_progress_points";
    private static final int MAX_PROGRESS = 200;

    private final String namespace;
    private final String path;

    public PlaceholderMeleeWeaponForge(String namespace, String path, Tier tier, int attackDamage, float attackSpeed) {
        super(tier, attackDamage, attackSpeed, new Properties().durability(tier.getUses()));
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(prettyName(namespace, path));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide()) {
            return result;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int progress = Math.max(0, tag.getInt(PROGRESS_KEY));

        // Progression damage points: every point grants a small bonus, capped for balance.
        float bonusDamage = Math.min(progress * 0.03f, 4.0f);
        if (bonusDamage > 0.0f) {
            target.hurt(attacker.damageSources().mobAttack(attacker), bonusDamage);
        }

        if (!target.isAlive() && attacker instanceof Player) {
            tag.putInt(PROGRESS_KEY, Math.min(MAX_PROGRESS, progress + 1));
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
