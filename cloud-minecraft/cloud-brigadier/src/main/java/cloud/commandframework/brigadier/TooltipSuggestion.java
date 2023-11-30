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
package cloud.commandframework.brigadier;

import cloud.commandframework.arguments.suggestion.Suggestion;
import com.mojang.brigadier.Message;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link Suggestion} that has an optional Brigadier {@link Message message} tooltip.
 *
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
public interface TooltipSuggestion extends Suggestion {

    /**
     * Returns a new {@link TooltipSuggestion} with the given {@code suggestion} and {@code tooltip}.
     *
     * @param suggestion the suggestion
     * @param tooltip    the optional tooltip that is displayed when hovering over the suggestion
     * @return the suggestion instance
     */
    static @NonNull TooltipSuggestion tooltipSuggestion(
            final @NonNull String suggestion,
            final @Nullable Message tooltip
    ) {
        return new TooltipSuggestionImpl(suggestion, tooltip);
    }

    /**
     * Returns a new {@link TooltipSuggestion} that uses the given {@code suggestion} and has a {@code null} tooltip.
     *
     * @param suggestion the suggestion
     * @return the suggestion instance
     */
    static @NonNull TooltipSuggestion tooltipSuggestion(
            final @NonNull Suggestion suggestion
    ) {
        if (suggestion instanceof TooltipSuggestion) {
            return (TooltipSuggestion) suggestion;
        }
        return tooltipSuggestion(suggestion.suggestion(), null /* tooltip */);
    }

    /**
     * Returns the tooltip.
     *
     * @return the tooltip, or {@code null}
     */
    @Nullable Message tooltip();

    @Override
    default @NonNull TooltipSuggestion withSuggestion(@NonNull String string) {
        return tooltipSuggestion(string, this.tooltip());
    }

    /**
     * Returns a copy of this suggestion instance using the given {@code tooltip}
     *
     * @param tooltip the new tooltip
     * @return the new suggestion
     */
    default @NonNull TooltipSuggestion withTooltip(@NonNull Message tooltip) {
        return tooltipSuggestion(this.suggestion(), tooltip);
    }


    final class TooltipSuggestionImpl implements TooltipSuggestion {

        private final String suggestion;
        private final Message tooltip;

        private TooltipSuggestionImpl(
                final @NonNull String suggestion,
                final @Nullable Message tooltip
        ) {
            this.suggestion = suggestion;
            this.tooltip = tooltip;
        }

        @Override
        public @NonNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @Nullable Message tooltip() {
            return this.tooltip;
        }
    }
}
