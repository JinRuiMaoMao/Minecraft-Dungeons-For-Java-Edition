package net.backupcup.mcde.screen;

import java.util.Optional;
import net.backupcup.mcde.MCDEnchantments;
import net.backupcup.mcde.screen.handler.GildingFoundryScreenHandler;
import net.backupcup.mcde.screen.util.TexturePos;
import net.backupcup.mcde.util.EnchantmentSlots;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class GildingFoundryScreen extends AbstractContainerScreen<GildingFoundryScreenHandler> {
    private static enum GildingItemSilouette {
        GOLD, EMERALD
    }
    private GildingItemSilouette silouette = GildingItemSilouette.GOLD;
    private float silouetteTimer = 0f;
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MCDEnchantments.MOD_ID, "textures/gui/gilding_foundry.png");
    private static final TexturePos GOLD_BUTTON_OFFSET = TexturePos.of(2, 223);
    private static final TexturePos EMERALD_BUTTON_OFFSET = TexturePos.of(2, 239);

    private Container inventory;
    private Player playerEntity;


    private int backgroundX;
    private int backgroundY;
    private int buttonX;
    private int buttonY;

    private int progressX;
    private int progressY;

    public GildingFoundryScreen(GildingFoundryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.inventory = handler.getInventory();
        this.playerEntity = inventory.player;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title) + 4) / 2;
        titleLabelY = -3;
        inventoryLabelX = -200;
        inventoryLabelY = -200;
        backgroundX = ((width - imageWidth) / 2) - 2;
        backgroundY = (height - imageHeight) / 2;
        buttonX = backgroundX + 46;
        buttonY = backgroundY + 59;
        progressX = backgroundX + 63;
        progressY = backgroundY + 3;
    }

    @Override
    protected void renderBg(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        ctx.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        int posX = ((width - imageWidth) / 2) - 2;
        int posY = (height - imageHeight) / 2;
        Slot slot = this.menu.getSlot(1);

        ctx.blit(TEXTURE, posX, posY, 2, 20, 168, 167);
        ctx.blit(TEXTURE, posX + slot.x + 2, posY + slot.y, 239, switch (silouette) {
            case GOLD -> 166;
            case EMERALD -> 185;
        }, 16, 16);

        silouetteTimer += delta;
        
        var ingridient = inventory.getItem(1);
        if (ingridient.isEmpty() && silouetteTimer > 20f) {
            silouette = switch (silouette) {
                case GOLD -> GildingItemSilouette.EMERALD;
                case EMERALD -> GildingItemSilouette.GOLD;
            };
        } else if (ingridient.is(Items.GOLD_INGOT)) {
            silouette = GildingItemSilouette.GOLD;
        } else if (ingridient.is(Items.EMERALD)) {
            silouette = GildingItemSilouette.EMERALD;
        }

        if (silouetteTimer > 20f) {
            silouetteTimer = 0;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics ctx, int mouseX, int mouseY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInBounds(buttonX, buttonY, (int)mouseX, (int)mouseY, 0, 76, 0, 12) && isGildingButtonClickable()) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        if (!menu.hasProgress()) {
            ctx.blit(TEXTURE, buttonX, buttonY, 2, 207, 76, 15);
        }
        else {
            var buttonOffset = getButtonTextureOffset().add(156, 2);
            ctx.blit(TEXTURE, buttonX, buttonY, buttonOffset.x(), buttonOffset.y(), 76, 13);
        }

        drawProgress(ctx, menu.getProgress());

        if (!inventory.getItem(1).isEmpty() || playerEntity.isCreative() && !inventory.getItem(0).isEmpty()) {
            var buttonOffset = getButtonTextureOffset();
            if (isGildingButtonClickable()) {
                ctx.blit(TEXTURE, buttonX, buttonY, buttonOffset.x(), buttonOffset.y(), 76, 15);
                if (isInBounds(buttonX, buttonY, mouseX, mouseY, 0, 76, 0, 15)) {
                    ctx.blit(TEXTURE, buttonX, buttonY, buttonOffset.x() + 78, buttonOffset.y(), 76, 15);
                }
            }

            int color = hexToColor("#F6F6F6");
            int shadow = hexToColor("#6e2727");
            var text = Component.translatable("ui.mcde.gilding_button");
            if (buttonOffset.equals(EMERALD_BUTTON_OFFSET)) {
                shadow = hexToColor("#165a4c");
                text = Component.translatable("ui.mcde.regilding_button");
            }
            ctx.drawString(font, text, buttonX + (76 - font.width(text)) / 2 + 1, buttonY + 2 + 1, shadow, false);
            ctx.drawString(font, text, buttonX + (76 - font.width(text)) / 2, buttonY + 2, color, false);
        }
        renderTooltip(ctx, mouseX, mouseY);
    }

    private static boolean isInBounds(int posX, int posY, int mouseX, int mouseY, int startX, int endX, int startY, int endY) {
        return mouseX >= posX + startX &&
               mouseX <= posX + endX &&
               mouseY >= posY + startY &&
               mouseY <= posY + endY;
    }

    private boolean isGildingButtonClickable() {
        var weapon = inventory.getItem(0);
        var ingridient = inventory.getItem(1);
        var slotsOptional = EnchantmentSlots.fromItemStack(weapon);
        if (playerEntity.isCreative()) {
            return !weapon.isEmpty() &&
                !menu.hasProgress() &&
                menu.hasEnchantmentForGilding();

        }
        return !weapon.isEmpty() &&
            !ingridient.isEmpty() &&
            ingridient.getCount() >= MCDEnchantments.getConfig().getGildingCost() &&
            !menu.hasProgress() &&
            slotsOptional.filter(slots -> ingridient.is(Items.GOLD_INGOT) ^ slots.hasGilding()).isPresent() &&
            menu.hasEnchantmentForGilding();
    }

    private void drawProgress(GuiGraphics ctx, int progress) {
        progress = (int)((float)progress / MCDEnchantments.getConfig().getGildingDuration() * 25f);
        var progressOffset = getButtonTextureOffset(TexturePos.of(247, 204), TexturePos.of(247, 230));
        if (progress < 1) {
            return;
        }
        if (progress > 25) {
            ctx.blit(TEXTURE, progressX, progressY, progressOffset.x(), progressOffset.y(), 8, 25);
            ctx.blit(TEXTURE, progressX + 34, progressY, progressOffset.x(), progressOffset.y(), 8, 25);
            return;
        }
        ctx.blit(TEXTURE, progressX, progressY + 25 - progress, progressOffset.x(), progressOffset.y() + 25 - progress, 8, progress);
        ctx.blit(TEXTURE, progressX + 34, progressY + 25 - progress, progressOffset.x(), progressOffset.y() + 25 - progress, 8, progress);
    }

    private TexturePos getButtonTextureOffset(TexturePos gold, TexturePos emerald) {
        if (playerEntity.isCreative()) {
            return EnchantmentSlots.fromItemStack(inventory.getItem(0))
                .map(slots -> slots.hasGilding()).orElse(false) ?
                emerald : gold;
        }
        return inventory.getItem(1).is(Items.EMERALD) ?
            emerald : gold;
    }

    private TexturePos getButtonTextureOffset() {
        return getButtonTextureOffset(GOLD_BUTTON_OFFSET, EMERALD_BUTTON_OFFSET);
    }

    private static int hexToColor(String hex) {
        return Mth.color(
            Integer.parseInt(hex.substring(1, 3), 16) / 256f,
            Integer.parseInt(hex.substring(3, 5), 16) / 256f,
            Integer.parseInt(hex.substring(5, 7), 16) / 256f
        );
    }
}
