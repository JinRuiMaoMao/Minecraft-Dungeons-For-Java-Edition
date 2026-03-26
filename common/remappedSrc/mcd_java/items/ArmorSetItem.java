package mcd_java.items;

import mcd_java.Mcda;
import mcd_java.config.ArmorStats;
import mcd_java.registries.ItemGroupRegistry;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.UUID;

public class ArmorSetItem extends ArmorItem {

    protected static final UUID[] ARMOR_MODIFIERS = new UUID[] {
            UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"),
            UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"),
            UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"),
            UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

    protected final Multimap<Attribute, AttributeModifier> attributeModifiers;
    protected final ArmorSets set;

    public ArmorSetItem(ArmorSets set, ArmorItem.Type type) {
        super(set, type, new Item.Properties());
        ItemGroupEvents.modifyEntriesEvent(ItemGroupRegistry.ARMOR).register(entries -> entries.accept(this));
        this.set = set;

        int protection = set.getDefenseForType(type);
        float toughness = set.getToughness();

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        UUID uuid = ARMOR_MODIFIERS[type.getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier",
                protection, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness",
                toughness, AttributeModifier.Operation.ADDITION));
        if (this.knockbackResistance > 0) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance",
                    this.knockbackResistance, AttributeModifier.Operation.ADDITION));
        }

        ArmorStats armorStats = Mcda.CONFIG.mcdaArmorStatsConfig.armorStats.get(set);

        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid,
                "Armor attack damage boost",
                armorStats.attackDamageBoost,
                AttributeModifier.Operation.MULTIPLY_BASE));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(uuid,
                "Armor attack speed boost",
                armorStats.attackSpeedBoost, AttributeModifier.Operation.MULTIPLY_BASE));
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid,
                "Armor movement speed boost",
                armorStats.movementSpeedBoost, AttributeModifier.Operation.MULTIPLY_BASE));

        this.attributeModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == this.type.getSlot() ? this.attributeModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public Rarity getRarity(ItemStack itemStack) {
        return set.getRarity();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Level world, List<Component> tooltip, TooltipFlag tooltipContext) {
        super.appendHoverText(itemStack, world, tooltip, tooltipContext);

        String setId = switch (set) {
            case MYSTERY, BLUE_MYSTERY, GREEN_MYSTERY, PURPLE_MYSTERY, RED_MYSTERY -> "mystery_armor";
            default -> set.getSetName();
        };

        String translationKey = String.format("item.mcda.%s.tooltip_", setId);
        int i = 1;

        while (I18n.exists(translationKey + i)) {
            tooltip.add(Component.translatable(translationKey + i).withStyle(ChatFormatting.ITALIC));
            i++;
        }

        if (Mcda.CONFIG.mcdaArmorStatsConfig.setBonusTooltips) {
            translationKey = String.format("item.mcda.%s.effect.tooltip_", setId);
            i = 1;

            while (I18n.exists(translationKey + i)) {
                tooltip.add(Component.translatable(translationKey + i).withStyle(
                        Mcda.CONFIG.mcdaArmorStatsConfig.setBonusTooltipColors ?
                                switch (set) {
                                    case MYSTERY -> ChatFormatting.WHITE;
                                    case BLUE_MYSTERY, FROST, PHANTOM, FROST_BITE, NIMBLE_TURTLE, GLOW_SQUID -> ChatFormatting.DARK_AQUA;
                                    case GREEN_MYSTERY, HIGHLAND, CAVE_CRAWLER, HERO, OPULENT, VERDANT -> ChatFormatting.GREEN;
                                    case PURPLE_MYSTERY, CURIOUS, THIEF -> ChatFormatting.LIGHT_PURPLE;
                                    case RED_MYSTERY, LIVING_VINES, SPROUT, GHOST_KINDLER, GOURDIAN, BLACK_WOLF, RENEGADE, STALWART_MAIL, WITHER -> ChatFormatting.RED;
                                    case STURDY_SHULKER, SPIDER, SOULDANCER -> i == 1 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY;
                                    case SHADOW_WALKER -> i == 1 ? ChatFormatting.GREEN : ChatFormatting.LIGHT_PURPLE;
                                    case CAULDRON, TITAN, SPLENDID, TROUBADOUR -> ChatFormatting.BOLD;
                                    default -> ChatFormatting.GRAY;
                                } : ChatFormatting.GRAY
                ));
                i++;
            }


            if (type.getSlot() == EquipmentSlot.FEET && (set == ArmorSets.RUGGED_CLIMBING_GEAR || set == ArmorSets.SNOW || set == ArmorSets.GOAT)) {
                tooltip.add(Component.translatable("item.mcda.effect.lightfooted").withStyle(Mcda.CONFIG.mcdaArmorStatsConfig.setBonusTooltipColors ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            }

            if (FabricLoader.getInstance().isModLoaded("environmentz")) {
                if (set == ArmorSets.SNOW || set == ArmorSets.FROST || set == ArmorSets.FROST_BITE || set == ArmorSets.FOX || set == ArmorSets.ARCTIC_FOX || set == ArmorSets.WOLF
                        || set == ArmorSets.BLACK_WOLF || set == ArmorSets.GOAT || set == ArmorSets.CLIMBING_GEAR || set == ArmorSets.RUGGED_CLIMBING_GEAR || set == ArmorSets.GHOST_KINDLER) {
                    tooltip.add(Component.translatable("item.mcda.effect.freezing_protection").withStyle(Mcda.CONFIG.mcdaArmorStatsConfig.setBonusTooltipColors ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
                }
            }
        }
    }

    public ArmorSets getSet() {
        return this.set;
    }
}
