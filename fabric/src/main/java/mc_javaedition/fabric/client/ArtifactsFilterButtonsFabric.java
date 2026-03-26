package mc_javaedition.fabric.client;

import mc_javaedition.fabric.placeholder.PlaceholderCreativeTabFabric;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class ArtifactsFilterButtonsFabric {
    private ArtifactsFilterButtonsFabric() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreativeInventoryScreen)) {
                return;
            }
            int x = Math.max(6, scaledWidth - 26);
            int y = 24;
            int w = 20;
            int h = 20;
            int gap = 2;

            var b1 = new ItemIconButton(x, y, w, h, artifactStack("corrupted_beacon"), Text.literal("Damaging artifacts"), () -> {
                PlaceholderCreativeTabFabric.toggleDamagingArtifactsFilter();
                reopenCreativeScreen();
            });
            b1.setTooltip(Tooltip.of(Text.literal("Damaging artifacts (" + (PlaceholderCreativeTabFabric.isDamagingArtifactsFilterEnabled() ? "ON" : "OFF") + ")")));
            screen.addDrawableChild(b1);

            ItemIconButton b2 = new ItemIconButton(x, y + (h + gap), w, h, artifactStack("totem_of_shielding"), Text.literal("Defensive artifacts"), () -> {});
            b2.active = false;
            b2.setTooltip(Tooltip.of(Text.literal("Defensive artifacts")));
            screen.addDrawableChild(b2);

            ItemIconButton b3 = new ItemIconButton(x, y + 2 * (h + gap), w, h, artifactStack("boots_of_swiftness"), Text.literal("Agility artifacts"), () -> {});
            b3.active = false;
            b3.setTooltip(Tooltip.of(Text.literal("Agility artifacts")));
            screen.addDrawableChild(b3);

            ItemIconButton b4 = new ItemIconButton(x, y + 3 * (h + gap), w, h, artifactStack("flaming_quiver"), Text.literal("Quiver artifacts"), () -> {});
            b4.active = false;
            b4.setTooltip(Tooltip.of(Text.literal("Quiver artifacts")));
            screen.addDrawableChild(b4);

            ItemIconButton b5 = new ItemIconButton(x, y + 4 * (h + gap), w, h, artifactStack("golem_kit"), Text.literal("Summoning artifacts"), () -> {});
            b5.active = false;
            b5.setTooltip(Tooltip.of(Text.literal("Summoning artifacts")));
            screen.addDrawableChild(b5);
        });
    }

    private static void reopenCreativeScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        try {
            for (var ctor : CreativeInventoryScreen.class.getConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length == 1 && p[0].isAssignableFrom(client.player.getClass())) {
                    client.setScreen((CreativeInventoryScreen) ctor.newInstance(client.player));
                    return;
                }
                if (p.length == 3 && p[0].isAssignableFrom(client.player.getClass())) {
                    Object features = client.world != null ? client.world.getEnabledFeatures() : null;
                    client.setScreen((CreativeInventoryScreen) ctor.newInstance(client.player, features, false));
                    return;
                }
            }
        } catch (Throwable ignored) {
            // Filter still applies next time screen is reopened.
        }
    }

    private static ItemStack artifactStack(String path) {
        Identifier id = new Identifier("mcdjava", path);
        if (Registries.ITEM.containsId(id)) {
            return Registries.ITEM.get(id).getDefaultStack();
        }
        return Items.BARRIER.getDefaultStack();
    }

    private static final class ItemIconButton extends PressableWidget {
        private final ItemStack icon;
        private final Runnable action;

        private ItemIconButton(int x, int y, int width, int height, ItemStack icon, Text message, Runnable action) {
            super(x, y, width, height, message);
            this.icon = icon;
            this.action = action;
        }

        @Override
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int bg = this.active ? 0xAA000000 : 0x66000000;
            context.fill(getX(), getY(), getX() + width, getY() + height, bg);
            context.drawBorder(getX(), getY(), width, height, this.isHovered() ? 0xFFFFFFFF : 0xFF777777);
            context.drawItem(icon, getX() + 2, getY() + 2);
        }
    }
}

