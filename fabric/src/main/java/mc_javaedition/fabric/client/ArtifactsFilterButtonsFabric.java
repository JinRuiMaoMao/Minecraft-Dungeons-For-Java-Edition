package mc_javaedition.fabric.client;

import mc_javaedition.fabric.placeholder.PlaceholderCreativeTabFabric;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class ArtifactsFilterButtonsFabric {
    private static final Identifier ARTIFACTS_TAB_ID = new Identifier("mcd_java", "placeholders/04_artifacts");

    private ArtifactsFilterButtonsFabric() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreativeInventoryScreen)) {
                return;
            }
            int x = 6;
            int y = 24;
            int w = 20;
            int h = 20;
            int gap = 2;

            var b1 = new ItemIconButton(x, y, w, h, artifactStack("corrupted_beacon"), Text.literal("Damaging artifacts"), () -> {
                PlaceholderCreativeTabFabric.toggleDamagingArtifactsFilter();
                openCreativeToArtifactsTab();
            });
            b1.setTooltip(Tooltip.of(Text.literal("Damaging artifacts (" + (PlaceholderCreativeTabFabric.isDamagingArtifactsFilterEnabled() ? "ON" : "OFF") + ")")));
            Screens.getButtons(screen).add(b1);

            ItemIconButton b2 = new ItemIconButton(x, y + (h + gap), w, h, artifactStack("totem_of_shielding"), Text.literal("Defensive artifacts"), () -> {});
            b2.active = false;
            b2.setTooltip(Tooltip.of(Text.literal("Defensive artifacts")));
            Screens.getButtons(screen).add(b2);

            ItemIconButton b3 = new ItemIconButton(x, y + 2 * (h + gap), w, h, artifactStack("boots_of_swiftness"), Text.literal("Agility artifacts"), () -> {});
            b3.active = false;
            b3.setTooltip(Tooltip.of(Text.literal("Agility artifacts")));
            Screens.getButtons(screen).add(b3);

            ItemIconButton b4 = new ItemIconButton(x, y + 3 * (h + gap), w, h, artifactStack("flaming_quiver"), Text.literal("Quiver artifacts"), () -> {});
            b4.active = false;
            b4.setTooltip(Tooltip.of(Text.literal("Quiver artifacts")));
            Screens.getButtons(screen).add(b4);

            ItemIconButton b5 = new ItemIconButton(x, y + 4 * (h + gap), w, h, artifactStack("golem_kit"), Text.literal("Summoning artifacts"), () -> {});
            b5.active = false;
            b5.setTooltip(Tooltip.of(Text.literal("Summoning artifacts")));
            Screens.getButtons(screen).add(b5);
        });
    }

    private static void openCreativeToArtifactsTab() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        try {
            ItemGroup artifactsTab = Registries.ITEM_GROUP.get(ARTIFACTS_TAB_ID);

            for (var ctor : CreativeInventoryScreen.class.getConstructors()) {
                Class<?>[] p = ctor.getParameterTypes();
                if (p.length == 1 && p[0].isAssignableFrom(client.player.getClass())) {
                    CreativeInventoryScreen screen = (CreativeInventoryScreen) ctor.newInstance(client.player);
                    selectArtifactsTab(screen, artifactsTab);
                    client.setScreen(screen);
                    return;
                }
                if (p.length == 3 && p[0].isAssignableFrom(client.player.getClass())) {
                    Object features = client.world != null ? client.world.getEnabledFeatures() : null;
                    CreativeInventoryScreen screen = (CreativeInventoryScreen) ctor.newInstance(client.player, features, false);
                    selectArtifactsTab(screen, artifactsTab);
                    client.setScreen(screen);
                    return;
                }
            }
        } catch (Throwable ignored) {
            // Filter still applies next time screen is reopened.
        }
    }

    private static void selectArtifactsTab(CreativeInventoryScreen screen, ItemGroup tab) {
        if (screen == null || tab == null) return;
        try {
            // Prefer explicit "setSelectedTab" if available (handles tab pages internally).
            for (var m : screen.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (!ItemGroup.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
                String n = m.getName().toLowerCase(java.util.Locale.ROOT);
                if (!n.contains("selected") || !n.contains("tab")) continue;
                m.setAccessible(true);
                m.invoke(screen, tab);
                return;
            }
            for (var m : CreativeInventoryScreen.class.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (!ItemGroup.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                String n = m.getName().toLowerCase(java.util.Locale.ROOT);
                if (!n.contains("selected") || !n.contains("tab")) continue;
                m.setAccessible(true);
                m.invoke(null, tab);
                return;
            }

            // Fallback: any ItemGroup setter-like method.
            for (var m : screen.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (!ItemGroup.class.isAssignableFrom(m.getParameterTypes()[0])) continue;
                m.setAccessible(true);
                m.invoke(screen, tab);
                return;
            }

            // Final fallback: set obvious fields.
            for (var f : screen.getClass().getDeclaredFields()) {
                if (!ItemGroup.class.isAssignableFrom(f.getType())) continue;
                String n = f.getName().toLowerCase(java.util.Locale.ROOT);
                if (!(n.contains("selected") || n.contains("tab"))) continue;
                f.setAccessible(true);
                f.set(screen, tab);
                return;
            }
        } catch (Throwable ignored) {
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
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            int bg = this.active ? 0xAA000000 : 0x66000000;
            context.fill(getX(), getY(), getX() + width, getY() + height, bg);
            context.drawBorder(getX(), getY(), width, height, this.isHovered() ? 0xFFFFFFFF : 0xFF777777);
            context.drawItem(icon, getX() + 2, getY() + 2);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }
    }
}

