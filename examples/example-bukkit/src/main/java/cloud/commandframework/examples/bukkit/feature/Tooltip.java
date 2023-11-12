//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.examples.bukkit.feature;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.context.CommandContext;
import java.util.Arrays;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.suggestion.Suggestion.suggestion;
import static net.kyori.adventure.text.Component.text;

public final class Tooltip {

    @Suggestions("tooltip-suggestions")
    public @NonNull Stream<@NonNull Suggestion> suggestions(
            final @NonNull CommandContext<CommandSender> context,
            final @NonNull String input
    ) {
        return Arrays.stream(Species.values()).map(species -> suggestion(
                species.name().toLowerCase(),
                text(species.description(), species.color())
        ));
    }

    @CommandMethod("species <species>")
    public void speciesCommand(
            final @NonNull CommandSender sender,
            @Argument(value = "species", suggestions = "tooltip-suggestions") final @NonNull Species species
    ) {
        sender.sendMessage(String.format("Information about %s: %s", species.name().toLowerCase(), species.description()));
    }

    public enum Species {
        CAT("Cute cuddly animal", NamedTextColor.GOLD),
        SPIDER("Very not cute and has too many legs", NamedTextColor.RED),
        HORSE("A very tasty fruit", NamedTextColor.GREEN);

        private final String description;
        private final TextColor color;

        Species(final @NonNull String description, final @NonNull TextColor color) {
            this.description = description;
            this.color = color;
        }

        private @NonNull String description() {
            return this.description;
        }

        private @NonNull TextColor color() {
            return this.color;
        }
    }
}
