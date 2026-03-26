package mc_javaedition.forge.placeholder;

import mc_javaedition.combat.ArmorTextureLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class PlaceholderArmorForge extends ArmorItem {
    private final String namespace;
    private final String path;

    public PlaceholderArmorForge(
            String namespace,
            String path,
            Type type,
            int protection,
            int durability,
            float toughness,
            float knockback
    ) {
        super(new PlaceholderArmorMaterial(materialNameFromId(path), protection, durability, toughness, knockback), type, new Properties().stacksTo(1));
        this.namespace = namespace;
        this.path = path;
    }

    private static String materialNameFromId(String idPath) {
        if (idPath == null) {
            return "mcdjava:placeholder";
        }
        String base = idPath;
        if (base.endsWith("_helmet")) base = base.substring(0, base.length() - "_helmet".length());
        else if (base.endsWith("_chestplate")) base = base.substring(0, base.length() - "_chestplate".length());
        else if (base.endsWith("_leggings")) base = base.substring(0, base.length() - "_leggings".length());
        else if (base.endsWith("_boots")) base = base.substring(0, base.length() - "_boots".length());
        return "minecraft:" + ArmorTextureLookup.textureNameForArmorId(idPath);
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

    private static final class PlaceholderArmorMaterial implements ArmorMaterial {
        private final String materialName;
        private final int protection;
        private final int durability;
        private final float toughness;
        private final float knockback;

        private PlaceholderArmorMaterial(String materialName, int protection, int durability, float toughness, float knockback) {
            this.materialName = materialName;
            this.protection = protection;
            this.durability = durability;
            this.toughness = toughness;
            this.knockback = knockback;
        }

        @Override
        public int getDurabilityForType(Type type) {
            return durability;
        }

        @Override
        public int getDefenseForType(Type type) {
            return protection;
        }

        @Override
        public int getEnchantmentValue() {
            return 9;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.IRON_INGOT);
        }

        @Override
        public String getName() {
            return materialName;
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return knockback;
        }
    }
}
