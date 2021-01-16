//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.minecraft.extras;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;
import java.util.regex.Pattern;

final class ComponentHelper {

    public static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

    private ComponentHelper() {
    }

    public static @NonNull Component highlight(
            final @NonNull Component component,
            final @NonNull TextColor highlightColor
    ) {
        return component.replaceText(config -> {
            config.match(SPECIAL_CHARACTERS_PATTERN);
            config.replacement(match -> match.color(highlightColor));
        });
    }

    public static @NonNull Component repeat(
            final @NonNull Component component,
            final int repetitions
    ) {
        final TextComponent.Builder builder = Component.text();
        for (int i = 0; i < repetitions; i++) {
            builder.append(component);
        }
        return builder.build();
    }

    public static int length(final @NonNull Component component) {
        int length = 0;
        if (component instanceof TextComponent) {
            length += ((TextComponent) component).content().length();
        }
        final Component translated = GlobalTranslator.render(component, Locale.getDefault());
        for (final Component child : translated.children()) {
            length += length(child);
        }
        return length;
    }

}
