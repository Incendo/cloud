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
package cloud.commandframework.arguments.suggestion;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE, since = "2.0.0")
public interface Suggestion {

    /**
     * Returns a simple suggestion that returns the given {@code suggestion}
     *
     * @param suggestion the suggestion string
     * @return the created suggestion
     */
    static @NonNull Suggestion of(final @NonNull String suggestion) {
        return new SimpleSuggestion(suggestion);
    }

    /**
     * Returns a simple suggestion that returns the given {@code suggestion} together with the given {@code tooltip}
     * <p>
     * The supported types of tooltips depends on the platform. All platforms supporting tooltips should support
     * string tooltips, but they may also offer support for platform-specific tooltip types
     *
     * @param suggestion the suggestion string
     * @param tooltip    the suggestion tooltip
     * @param <T> the tooltip type
     * @return the created suggestion
     */
    static @NonNull <T> TooltipSuggestion<T> of(final @NonNull String suggestion, final @NonNull T tooltip) {
        return new SimpleTooltipSuggestion<>(suggestion, tooltip);
    }

    /**
     * Returns a simple suggestion that returns the given {@code suggestion}
     *
     * @param suggestion the suggestion string
     * @return the created suggestion
     */
    static @NonNull Suggestion suggestion(final @NonNull String suggestion) {
        return new SimpleSuggestion(suggestion);
    }

    /**
     * Returns a simple suggestion that returns the given {@code suggestion} together with the given {@code tooltip}
     * <p>
     * The supported types of tooltips depends on the platform. All platforms supporting tooltips should support
     * string tooltips, but they may also offer support for platform-specific tooltip types
     *
     * @param suggestion the suggestion string
     * @param tooltip    the suggestion tooltip
     * @param <T> the tooltip type
     * @return the created suggestion
     */
    static @NonNull <T> TooltipSuggestion<T> suggestion(final @NonNull String suggestion, final @NonNull T tooltip) {
        return new SimpleTooltipSuggestion<>(suggestion, tooltip);
    }

    /**
     * Returns a string representation of this suggestion, which can be parsed by the parser that suggested it
     *
     * @return the suggestions
     */
    @NonNull String suggestion();

    /**
     * Returns a copy of this suggestion instance using the given {@code suggestion}
     *
     * @param suggestion the suggestion string
     * @return the new suggestion
     */
    @NonNull Suggestion withSuggestion(@NonNull String suggestion);
}
