package net.backupcup.mcde.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.screen.handler.RollBenchScreenHandler;
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
import net.minecraft.text.Text;
import net.minecraft.text.MutableComponent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.entity.PlayerEntity.Inventory;
import net.minecraft.screen.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class RollBenchScreen extends AbstractContainerScreen<RollBenchScreenHandler> implements ScreenWithSlots {
    private static enum RerollItemSilouette {
        LAPIS, ECHO_SHARD;
    }
    private static enum RerollButtonState {
        HIDDEN, EXTENDING, SHUTTING, SHOWED
    }
    private static final Identifier TEXTURE = new Identifier(MCDEnchantments.MOD_ID, "textures/gui/roll_bench.png");
    private Container inventory;
    private RerollItemSilouette silouette = RerollItemSilouette.LAPIS;
    private float silouetteTimer = 0f;
    private Optional<SlotPosition> opened = Optional.empty();
    private Optional<Tuple<SlotPosition, SlotPosition>> selected = Optional.empty();
    private EnchantmentSlotsRenderer slotsRenderer;

    private TexturePos background;

    private TexturePos rerollButton;
    private boolean drawRerollButton;
    private RerollButtonState rerollButtonState;
    private float rerollButtonAnimationProgress;
    private static final float rerollButtonAnimationDuration = 20.0f;

    private TexturePos touchButton;

    public RollBenchScreen(RollBenchScreenHandler handler, Inventory inventory, Text title) {
        super(handler, inventory, title);
        this.inventory = handler.getInventory();
    }

    @Override
    protected void init() {
        super.init();

        background = TexturePos.of(((width - imageWidth) / 2) - 2, (height - imageHeight) / 2 + 25);
        slotsRenderer = EnchantmentSlotsRenderer.builder()
            .withScreen(this)
            .withDefaultGuiTexture(TEXTURE)
            .withDefaultSlotPositions(background.add(-1, 0))
            .withDimPredicate(
                choice -> EnchantmentSlots.fromItemStack(inventory.getItem(0))
                .map(
                    slots -> !menu.canReroll(minecraft.PlayerEntity, choice.getEnchantmentId(), slots) ||
                        menu.isSlotLocked(choice.getEnchantmentSlot().getSlotPosition()).orElse(true)
                ).orElse(true)
            )
            .withClient(minecraft)
            .build();
        drawRerollButton = minecraft.PlayerEntity.isCreative();
        rerollButton = background.add(155, 12);
        rerollButtonState = RerollButtonState.HIDDEN;
        rerollButtonAnimationProgress = 0f;
        touchButton = background.add(-2, 38);
    }

    @Override
    protected void renderBg(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        ctx.blit(TEXTURE, background.x(), background.y(), 1, 109, 168, 149);
        var slot = menu.getSlot(1);
        var drawPos = TexturePos.of((width - imageWidth) / 2, (height - imageHeight) / 2)
            .add(slot.x, slot.y);
        ctx.blit(TEXTURE, drawPos.x(), drawPos.y(), 195, switch (silouette) {
            case LAPIS -> 175;
            case ECHO_SHARD -> 197;
        }, 18, 18);

        silouetteTimer += delta;

        ItemStack ingridient = inventory.getItem(1);
        if (ingridient.isEmpty() && silouetteTimer > 20f) {
            silouette = switch (silouette) {
                case LAPIS -> RerollItemSilouette.ECHO_SHARD;
                case ECHO_SHARD -> RerollItemSilouette.LAPIS;
            };
        } else if (ingridient.is(Items.LAPIS_LAZULI)) {
            silouette = RerollItemSilouette.LAPIS;
        } else if (ingridient.is(Items.ECHO_SHARD)) {
            silouette = RerollItemSilouette.ECHO_SHARD;
        }

        if (silouetteTimer > 20f) {
            silouetteTimer = 0;
        }

    }

    @Override
    protected void renderLabels(GuiGraphics ctx, int mouseX, int mouseY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ItemStack stack = inventory.getItem(0);

        if (stack.isEmpty())
            return super.mouseClicked(mouseX, mouseY, button);
        var slotsOptional = EnchantmentSlots.fromItemStack(stack);
        if (slotsOptional.isEmpty()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        var slots = slotsOptional.get();
        if (isTouchscreen() && selected.isPresent() && isInTouchButton((int)mouseX, (int)mouseY)) {
            var slotPos = selected.get().getA();
            var choicePos = selected.get().getB();
            if (slots.getEnchantmentSlot(slotPos).map(slot -> slotsRenderer.getDimPredicate().test(new Choice(slot, choicePos))).orElse(false)) {
                return false;
            }
            if (slots.getEnchantmentSlot(slotPos).filter(slot -> slot.getChosen().isPresent()).isPresent()) {
                selected = Optional.empty();
                opened = Optional.of(slotPos);
            }
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, SlotPosition.values().length * slotPos.ordinal() + choicePos.ordinal());
            return false;
        }

        for (var slot : slots) {
            if (slotsRenderer.isInSlotBounds(slot.getSlotPosition(), (int) mouseX, (int) mouseY)) {
                if (slot.getChosen().isPresent()) {
                    var chosen = slot.getChosen().get();
                    if (isTouchscreen()) {
                        selected = Optional.of(new Tuple<>(slot.getSlotPosition(), chosen.getChoicePosition()));
                        opened = Optional.empty();
                    } else if (!slotsRenderer.getDimPredicate().test(chosen)) {
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
        if (drawRerollButton && !inventory.getItem(0).isEmpty() && isInRerollButton((int)mouseX, (int)mouseY)) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, RollBenchScreenHandler.REROLL_BUTTON_ID);
            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);
        renderRerollButton(ctx, mouseX, mouseY, delta);

        if (isTouchscreen()) {
            ctx.blit(TEXTURE, touchButton.x(), touchButton.y(), 164, 0, 13, 15);
        }

        ItemStack itemStack = inventory.getItem(0);
        var slotsOptional = EnchantmentSlots.fromItemStack(itemStack);
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

        Optional<Choice> hoveredChoice = slotsRenderer.render(ctx, itemStack, mouseX, mouseY);

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
                    int buttonX = 177;
                    if (isInTouchButton(mouseX, mouseY)) {
                        buttonX = 190;
                    }
                    ctx.blit(TEXTURE, touchButton.x(), touchButton.y(), buttonX, 0, 13, 15);
                }
            });
        } else {
            hoveredChoice.ifPresent(choice -> renderTooltip(ctx, choice, slots, mouseX, mouseY));
        }

        renderTooltip(ctx, mouseX, mouseY);
    }

    @Override
    public Optional<SlotPosition> getOpened() {
        return opened;
    }

    @Override
    public void setOpened(Optional<SlotPosition> opened) {
        this.opened = opened;
    }

    private static Function<Float, Integer> frameEasing(Function<Float, Float> f) {
        // 20 is the number of frames
        return x -> (int)(f.apply(x) * 20f);
    }

    private static float easeOut(float x) {
        return (float)(1 - Math.pow(1 - x, 3));
    }

    private static float easeIn(float x) {
        return x * x * x;
    }

    protected void renderRerollButton(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        drawRerollButton = !inventory.getItem(0).isEmpty() && (inventory.getItem(1).is(Items.ECHO_SHARD) || minecraft.PlayerEntity.isCreative());
        if (drawRerollButton && (rerollButtonState == RerollButtonState.HIDDEN || rerollButtonState == RerollButtonState.SHUTTING)) {
            rerollButtonState = RerollButtonState.EXTENDING;
            rerollButtonAnimationProgress = 0f;
        }
        else if (!drawRerollButton && (rerollButtonState == RerollButtonState.SHOWED || rerollButtonState == RerollButtonState.EXTENDING)) {
            rerollButtonState = RerollButtonState.SHUTTING;
            rerollButtonAnimationProgress = 0f;
        }
        else if (rerollButtonState == RerollButtonState.EXTENDING && rerollButtonAnimationProgress > 1.0f) {
            rerollButtonState = RerollButtonState.SHOWED;
            rerollButtonAnimationProgress = 0f;
        }
        else if (rerollButtonState == RerollButtonState.SHUTTING && rerollButtonAnimationProgress > 1.0f) {
            rerollButtonState = RerollButtonState.HIDDEN;
            rerollButtonAnimationProgress = 0f;
        }

        if (rerollButtonState == RerollButtonState.SHOWED) {
            ctx.blit(TEXTURE, rerollButton.x(), rerollButton.y(), 232, 0, 24, 34);
        }
        else if (rerollButtonState == RerollButtonState.EXTENDING || rerollButtonState == RerollButtonState.SHUTTING) {
            Function<Float, Float> easing = switch (rerollButtonState) {
                case EXTENDING -> RollBenchScreen::easeOut;
                case SHUTTING -> RollBenchScreen::easeIn;
                default -> null;
            };
            float progress = switch (rerollButtonState) {
                case EXTENDING -> rerollButtonAnimationProgress;
                case SHUTTING -> 1 - rerollButtonAnimationProgress;
                default -> 0f;
            };
            drawAnimationRerollButtonFrame(ctx, frameEasing(easing).apply(progress), isInRerollButton(mouseX, mouseY));
            MCDEnchantments.LOGGER.info(String.format("progress: %.2f", rerollButtonAnimationProgress));
            rerollButtonAnimationProgress += delta / rerollButtonAnimationDuration;
        }

        if (rerollButtonState == RerollButtonState.SHOWED && isInRerollButton(mouseX, mouseY)) {
            ctx.blit(TEXTURE, rerollButton.x(), rerollButton.y(), 204, 0, 25, 36);
        }
    }
    
    private void drawAnimationRerollButtonFrame(GuiGraphics ctx, int progress, boolean hovered) {
        if (progress > 20) {
            progress = 20;
        }
        if (progress < 0) {
            progress = 0;
        }
        int x = 232, width = 4, height = 34;
        if (hovered && drawRerollButton) {
            x = 204;
            width = 5;
            height = 36;
        }
        TexturePos part = TexturePos.of(x + 20 - progress, 0);
        ctx.blit(TEXTURE, rerollButton.x(), rerollButton.y(), part.x(), part.y(), width + progress, height);
    }

    protected void renderTooltip(GuiGraphics ctx, Choice hovered, EnchantmentSlots slots, int x, int y) {
        Identifier enchantment = hovered.getEnchantmentId();
        String translationKey = enchantment.toLanguageKey("enchantment");
        List<Text> tooltipLines = new ArrayList<>();
        boolean canReroll = menu.canReroll(minecraft.PlayerEntity, enchantment, slots);
        MutableComponent enchantmentName = Text.translatable(translationKey)
                .withStyle(EnchantmentUtils.formatEnchantment(enchantment));
        if (hovered.isChosen() && hovered.getEnchantment().getMaxLevel() > 1) {
            enchantmentName.append(" ")
                .append(Text.translatable("enchantment.level." + hovered.getLevel()));
        }
        tooltipLines.add(enchantmentName);

        tooltipLines.addAll(TextWrapUtils.wrapText(width, translationKey + ".desc", Formatting.GRAY));
        if (!minecraft.PlayerEntity.isCreative()) {
            tooltipLines.addAll(TextWrapUtils.wrapText(width, Text.translatable(
                        "message.mcde.lapis_required",
                        slots.getNextRerollCost(enchantment)), Formatting.ITALIC, Formatting.DARK_GRAY));
        }
        if (!canReroll) {
            tooltipLines.addAll(TextWrapUtils.wrapText(width, Text.translatable("message.mcde.not_enough_lapis"),
                    Formatting.DARK_RED, Formatting.ITALIC));
        }
        if (menu.isSlotLocked(hovered.getEnchantmentSlot().getSlotPosition()).orElse(true)) {
            tooltipLines.addAll(TextWrapUtils.wrapText(width, Text.translatable("message.mcde.cant_generate"), Formatting.DARK_RED, Formatting.ITALIC));
        }
        ctx.renderComponentTooltip(font, tooltipLines, x, y);
    }

    protected static boolean isInBounds(int posX, int posY, int mouseX, int mouseY, int startX, int endX, int startY, int endY) {
        return mouseX >= posX + startX &&
               mouseX <= posX + endX &&
               mouseY >= posY + startY &&
               mouseY <= posY + endY;
    }

    protected boolean isInRerollButton(int mouseX, int mouseY) {
        return isInBounds(rerollButton.x(), rerollButton.y(), mouseX, mouseY, 0, 25, 0, 28);
    }

    protected boolean isInTouchButton(int mouseX, int mouseY) {
        return isInBounds(touchButton.x(), touchButton.y(), mouseX, mouseY, 0, 13, 0, 15);
    }

    private boolean isTouchscreen() {
        return minecraft.options.touchscreen().get();
    }
}
