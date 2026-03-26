package net.backupcup.mcde.screen.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TextWrapUtils {
    private enum Width {
        SMALL(320, 20), NORMAL(450, 35), WIDE(590, 50), ULTRAWIDE(750, 65);
        private final int screen;
        private final int line;

        private Width(int screen, int line) {
            this.screen = screen;
            this.line = line;
        }

        public int getLine() {
            return line;
        }

        public static int getLineWidth(int screenWidth) {
            return Arrays.stream(values())
                .reduce((current, next) -> next.screen <= screenWidth ? next : current)
                .map(Width::getLine).orElse(SMALL.line);
        }
    }
    private static Pattern generate(int screenWidth) {
        return Pattern.compile(String.format("(\\S.{1,%d})(?:\\s+|$)", Width.getLineWidth(screenWidth)));
    }

    public static List<Component> wrapText(int screenWidth, String translationKey, ChatFormatting formatting) {
        return generate(screenWidth).matcher(Component.translatable(translationKey).getString())
            .results().map(res -> (Component)Component.literal(res.group(1)).withStyle(formatting))
            .toList();
    }

    public static List<Component> wrapText(int screenWidth, String translationKey, ChatFormatting... formatting) {
        return generate(screenWidth).matcher(Component.translatable(translationKey).getString())
            .results().map(res -> (Component)Component.literal(res.group(1)).withStyle(formatting))
            .toList();
    }

    public static List<Component> wrapText(int screenWidth, Component text, ChatFormatting... formatting) {
        return generate(screenWidth).matcher(text.getString())
            .results().map(res -> (Component)Component.literal(res.group(1)).withStyle(formatting))
            .toList();
    }
    
}
