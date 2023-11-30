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
package cloud.commandframework.paper.suggestions.tooltips;

import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionMapper;
import net.kyori.adventure.text.Component;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Like {@link cloud.commandframework.brigadier.TooltipSuggestion} but using {@link Component} instead
 * of {@link com.mojang.brigadier.Message}.
 * <p>
 * To use this, you should set the {@link cloud.commandframework.CommandManager#suggestionMapper(SuggestionMapper)} to
 * {@link ComponentTooltipSuggestionMapper}.
 * If this is not done, then the tooltip will be ignored.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface ComponentTooltipSuggestion extends Suggestion {

    /**
     * Returns a new {@link ComponentTooltipSuggestion} with the given {@code suggestion} and {@code tooltip}.
     *
     * @param suggestion the suggestion
     * @param tooltip    the optional tooltip that is displayed when hovering over the suggestion
     * @return the suggestion instance
     */
    static @NonNull ComponentTooltipSuggestion tooltipSuggestion(
            final @NonNull String suggestion,
            final @Nullable Component tooltip
    ) {
        return new ComponentTooltipSuggestionImpl(suggestion, tooltip);
    }

    /**
     * Returns a new {@link ComponentTooltipSuggestion} that uses the given {@code suggestion} and has a {@code null} tooltip.
     *
     * @param suggestion the suggestion
     * @return the suggestion instance
     */
    static @NonNull ComponentTooltipSuggestion tooltipSuggestion(
            final @NonNull Suggestion suggestion
    ) {
        if (suggestion instanceof ComponentTooltipSuggestion) {
            return (ComponentTooltipSuggestion) suggestion;
        }
        return tooltipSuggestion(suggestion.suggestion(), null /* tooltip */);
    }

    /**
     * Returns the tooltip.
     *
     * @return the tooltip, or {@code null}
     */
    @Nullable Component tooltip();

    @Override
    default @NonNull ComponentTooltipSuggestion withSuggestion(@NonNull String string) {
        return tooltipSuggestion(string, this.tooltip());
    }

    /**
     * Returns a copy of this suggestion instance using the given {@code tooltip}
     *
     * @param tooltip the new tooltip
     * @return the new suggestion
     */
    default @NonNull ComponentTooltipSuggestion withTooltip(@NonNull Component tooltip) {
        return tooltipSuggestion(this.suggestion(), tooltip);
    }

    final class ComponentTooltipSuggestionImpl implements ComponentTooltipSuggestion {

        private final String suggestion;
        private final Component tooltip;

        private ComponentTooltipSuggestionImpl(
                final @NonNull String suggestion,
                final @Nullable Component tooltip
        ) {
            this.suggestion = suggestion;
            this.tooltip = tooltip;
        }

        @Override
        public @NonNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @Nullable Component tooltip() {
            return this.tooltip;
        }
    }
}
