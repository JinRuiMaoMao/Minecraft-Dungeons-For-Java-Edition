package net.backupcup.mcde.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.screen.handler.RunicTableScreenHandler;
import net.backupcup.mcde.screen.util.EnchantmentSlotsRenderer;
import net.backupcup.mcde.screen.util.ScreenWithSlots;
import net.backupcup.mcde.screen.util.TextWrapUtils;
import net.backupcup.mcde.screen.util.TexturePos;
import net.backupcup.mcde.util.Choice;
import net.backupcup.mcde.util.EnchantmentSlot;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.backupcup.mcde.util.EnchantmentUtils;
import net.backupcup.mcde.util.SlotPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Text;
import net.minecraft.text.MutableComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

@Environment(EnvType.CLIENT)
public class RunicTableScreen extends AbstractContainerScreen<RunicTableScreenHandler> implements ScreenWithSlots {
    private static final Identifier TEXTURE =
        new Identifier(MCDEnchantments.MOD_ID, "textures/gui/runic_table.png");
    private Container inventory;
    private Optional<SlotPosition> opened = Optional.empty();
    private Optional<Tuple<SlotPosition, SlotPosition>> selected = Optional.empty();
    private EnchantmentSlotsRenderer slotsRenderer;

    private TexturePos background;
    private TexturePos touchButton;

    public RunicTableScreen(RunicTableScreenHandler handler, Inventory inventory, Text title) {
        super(handler, inventory, title);
        this.inventory = handler.getInventory();
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 125;
        titleLabelY = 10;

        background = TexturePos.of(((width - imageWidth) / 2) - 2, (height - imageHeight) / 2 + 25);
        slotsRenderer = EnchantmentSlotsRenderer.builder()
            .withScreen(this)
            .withDefaultGuiTexture(TEXTURE)
            .withDefaultSlotPositions(background)
            .withDimPredicate(choice -> {
                int level = 1;
                boolean isMaxedOut = false;
                if (choice.isChosen()) {
                    level = choice.getLevel() + 1;
                    isMaxedOut = choice.isMaxedOut();
                }
                return isMaxedOut || !RunicTableScreenHandler.canEnchant(minecraft.PlayerEntity, choice.getEnchantmentId(), level) ||
                    (EnchantmentHelper.getEnchantments(inventory.getItem(0)).keySet().stream().anyMatch(e -> !e.isCompatibleWith(choice.getEnchantment())) && 
                         !choice.isChosen() && MCDEnchantments.getConfig().isCompatibilityRequired());
            })
            .withClient(minecraft)
            .build();
        touchButton = background.add(-2, 38);
    }

    @Override
    protected void renderBg(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        /*Runic Table UI*/
        ctx.blit(TEXTURE, background.x(), background.y(), 0, 109, 168, 150);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ItemStack stack = inventory.getItem(0);
        var slotsOptional = EnchantmentSlots.fromItemStack(stack);
        if (stack.isEmpty() || slotsOptional.isEmpty()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        var slots = slotsOptional.get();

        if (isTouchscreen() && selected.isPresent() && isInTouchButton((int)mouseX, (int)mouseY)) {
            var slot = selected.get().getA();
            var choice = selected.get().getB();
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SlotPosition.values().length * slot.ordinal() + choice.ordinal());
            return false;
        }

        for (var slot : slots) {
            if (slotsRenderer.isInSlotBounds(slot.getSlotPosition(), (int)mouseX, (int)mouseY)) {
                if (slot.getChosen().isPresent()) {
                    var chosen = slot.getChosen().get();
                    if (isTouchscreen()) {
                        selected = Optional.of(new Tuple<>(slot.getSlotPosition(), chosen.getChoicePosition()));
                        opened = Optional.empty();
                    } else {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SlotPosition.values().length * slot.ordinal());
                    }
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                if (opened.isEmpty()) {
                    opened = Optional.of(slot.getSlotPosition());
                } else if (opened.get() == slot.getSlotPosition()) {
                    opened = Optional.empty();
                    selected = Optional.empty();
                } else {
                    opened = Optional.of(slot.getSlotPosition());
                    selected = Optional.empty();
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            if (opened.isPresent() && opened.get() == slot.getSlotPosition()) {
                for (var choice : slot.choices()) {
                    if (slotsRenderer.isInChoiceBounds(slot.getSlotPosition(), choice.getChoicePosition(), (int) mouseX, (int) mouseY)) {
                        if (isTouchscreen()) {
                            selected = Optional.of(new Tuple<>(slot.getSlotPosition(), choice.getChoicePosition()));
                        } else if (!slotsRenderer.getDimPredicate().test(choice)) {
                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SlotPosition.values().length * slot.ordinal() + choice.ordinal());
                        }
                        return super.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        }
        opened = Optional.empty();
        selected = Optional.empty();

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // To not render Inventory and Title text
    @Override
    protected void renderLabels(GuiGraphics ctx, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        RenderSystem.setShaderTexture(0, TEXTURE);
        ItemStack itemStack = inventory.getItem(0);
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
        if (isTouchscreen()) {
            ctx.blit(TEXTURE, touchButton.x(), touchButton.y(), 216, 1, 13, 15);
        }
        if (itemStack.isEmpty() || slotsOptional.isEmpty()) {
            renderTooltip(ctx, mouseX, mouseY);
            return;
        }
        var slots = slotsOptional.get();
        if (!isTouchscreen()) {
            for (var slot : slots) {
                var pos = slot.getSlotPosition();
                if (!slotsRenderer.isInChoicesBounds(pos, mouseX, mouseY) && opened.map(slotPos -> slotPos.equals(pos)).orElse(false)) {
                    opened = Optional.empty();
                }
                if (slotsRenderer.isInSlotBounds(pos, mouseX, mouseY)) {
                    opened = Optional.of(slot.getSlotPosition());
                }
            }
        }

        var hoveredChoice = slotsRenderer.render(ctx, itemStack, mouseX, mouseY);

        if (isTouchscreen()) {
            selected.flatMap(pair -> slots.getEnchantmentSlot(pair.getA())
                    .map(slot -> new Choice(slot, pair.getB()))).ifPresent(choice -> {
                renderTooltip(ctx, choice, slots, background.x(), background.y() + 88);
                if (choice.isChosen()) {
                    slotsRenderer.drawHoverOutline(ctx, choice.getEnchantmentSlot().getSlotPosition());
                } else {
                    slotsRenderer.drawIconHoverOutline(ctx, choice.getEnchantmentSlot().getSlotPosition(), choice);
                }
                if (!slotsRenderer.getDimPredicate().test(choice)) {
                    int buttonX = 229;
                    if (isInTouchButton(mouseX, mouseY)) {
                        buttonX = 242;
                    }
                    ctx.blit(TEXTURE, touchButton.x(), touchButton.y(), buttonX, 1, 13, 13);
                }
            });
        } else {
            hoveredChoice.ifPresent(choice -> renderTooltip(ctx, choice, slots, mouseX, mouseY));
        }

        renderTooltip(ctx, mouseX, mouseY);
    }

    protected void renderTooltip(GuiGraphics ctx, Choice hovered, EnchantmentSlots slots, int x, int y) {
        var itemStack = inventory.getItem(0);
        Enchantment enchantment = hovered.getEnchantment();
        Identifier enchantmentId = hovered.getEnchantmentId();
        List<Text> tooltipLines = new ArrayList<>();
        int level = 1;
        boolean enoughLevels = RunicTableScreenHandler.canEnchant(minecraft.PlayerEntity, enchantmentId, level);
        MutableComponent enchantmentName = Text.translatable(enchantment.getDescriptionId())
            .withStyle(EnchantmentUtils.formatEnchantment(enchantmentId));
        if (hovered.isChosen()) {
            enchantmentName.append(" ")
                .append(Text.translatable("enchantment.level." + hovered.getLevel()))
                .append(" ");
            if (hovered.isMaxedOut()) {
                enchantmentName.append(Text.translatable("message.mcde.max_level"));
                enoughLevels = true;
            }
            else {
                enchantmentName
                    .append("→ ")
                    .append(Text.translatable("enchantment.level." + (hovered.getLevel() + 1)));
                level = hovered.getLevel() + 1;
                enoughLevels = RunicTableScreenHandler.canEnchant(minecraft.PlayerEntity, enchantmentId, level);

            }
        }
        tooltipLines.add(enchantmentName);

        tooltipLines.addAll(TextWrapUtils.wrapText(width, enchantment.getDescriptionId() + ".desc", Formatting.GRAY));
        if (!hovered.isMaxedOut() && !minecraft.PlayerEntity.isCreative()) {
            tooltipLines.addAll(TextWrapUtils.wrapText(width, Text.translatable(
                            "message.mcde.levels_required",
                            MCDEnchantments.getConfig().getEnchantCost(enchantmentId, level)),
                        Formatting.ITALIC, Formatting.DARK_GRAY));
        }
        if (!enoughLevels) {
            tooltipLines.addAll(TextWrapUtils.wrapText(width, "message.mcde.not_enough_levels", Formatting.DARK_RED, Formatting.ITALIC));
        }

        if (!hovered.isChosen()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(hovered.getEnchantment(), itemStack) > 0) {
                tooltipLines.addAll(TextWrapUtils.wrapText(width, "message.mcde.already_exists", Formatting.DARK_RED, Formatting.ITALIC));
            } else if (MCDEnchantments.getConfig().isCompatibilityRequired()) {
                var conflict = EnchantmentHelper.getEnchantments(itemStack).keySet().stream()
                    .filter(e -> !e.isCompatibleWith(hovered.getEnchantment())).findFirst();
                if (conflict.isPresent()) {
                    var conflicting = conflict.get();
                    tooltipLines.addAll(TextWrapUtils.wrapText(width, Text.translatable(
                                    "message.mcde.cant_combine",
                                    Text.translatable(conflicting.getDescriptionId())),
                                Formatting.DARK_RED, Formatting.ITALIC));
                }
            }
        }
        ctx.renderComponentTooltip(font, tooltipLines, x, y);
    }

    @Override
    public Optional<SlotPosition> getOpened() {
        return opened;
    }

    @Override
    public void setOpened(Optional<SlotPosition> opened) {
        this.opened = opened;
    }

    private boolean isTouchscreen() {
        return minecraft.options.touchscreen().get();
    }

    protected static boolean isInBounds(int posX, int posY, int mouseX, int mouseY, int startX, int endX, int startY, int endY) {
        return mouseX >= posX + startX &&
               mouseX <= posX + endX &&
               mouseY >= posY + startY &&
               mouseY <= posY + endY;
    }

    protected boolean isInTouchButton(int mouseX, int mouseY) {
        return isInBounds(touchButton.x(), touchButton.y(), mouseX, mouseY, 0, 13, 0, 15);
    }
}
