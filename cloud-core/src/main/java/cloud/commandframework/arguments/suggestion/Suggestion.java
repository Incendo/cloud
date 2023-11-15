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
    static @NonNull Suggestion simple(final @NonNull String suggestion) {
        return new SuggestionImpl(suggestion);
    }

    /**
     * Returns a typed {@code suggestion} with an accompanying {@code stringRepresentation}
     *
     * @param suggestion           the typed suggestion
     * @param stringRepresentation the string representation of the suggestion
     * @return the typed suggestion
     * @param <T> the type of the suggestion
     */
    static <T> @NonNull TypedSuggestion<T> typed(final @NonNull T suggestion, final @NonNull String stringRepresentation) {
        return new TypedSuggestionImpl<>(suggestion, stringRepresentation);
    }

    /**
     * Returns a typed {@code suggestion} using {@code suggestion.toString()} as the string representation
     *
     * @param suggestion the typed suggestion
     * @return the typed suggestion
     * @param <T> the type of the suggestion
     */
    static <T> @NonNull TypedSuggestion<T> typed(final @NonNull T suggestion) {
        return new TypedSuggestionImpl<>(suggestion);
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
