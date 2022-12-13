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
package cloud.commandframework;

import java.util.LinkedList;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * A class containing information about suggestions
 */
public interface Suggestion {

    /**
     * Creates a simple representation of the suggestion
     * @param suggestion The suggestion itself.
     * @return An instance of suggestion representing this string.
     */
    static Suggestion of(String suggestion) {
        return new SimpleSuggestion(suggestion);
    }
    /**
     * Wraps multiple raw suggestions into a simple representation of the suggestion
     * @param suggestions The suggestions
     * @return A list with the instances of suggestions representing those raw suggestions.
     */
    static List<Suggestion> of(Iterable<String> suggestions) {
        List<Suggestion> suggestion = new LinkedList<>();
        for (String raw: suggestions){
            suggestion.add(new SimpleSuggestion(raw));
        }
        return suggestion;
    }
    /**
     * Wraps multiple raw suggestions into a simple representation of the suggestion
     * @param suggestions The suggestions
     * @return A list with the instances of suggestions representing those raw suggestions.
     */
    static List<String> raw(Iterable<Suggestion> suggestions) {
        List<String> raw = new LinkedList<>();
        for (Suggestion suggestion: suggestions){
            raw.add(suggestion.suggestion());
        }
        return raw;
    }

    /**
     * Returns the description of the suggestion.
     * <p>
     * Check platform specific implementation which support this
     * @return the description of the suggestion.
     */
    @Nullable String description();

    /**
     * Returns the suggestion itself.
     * @return the suggestion itself.
     */
    @NonNull String suggestion();

    /**
     * Creates a new suggestion with given raw suggestion
     * @param suggestion new suggestion
     * @return a new instance with this suggestion
     */
    @NonNull Suggestion withSuggestion(@NonNull String suggestion);

    /**
     * SimpleSuggestion is a suggestion that wraps around a string suggestion and has no description
     */
    final class SimpleSuggestion implements Suggestion {

        private final String suggestion;

        private SimpleSuggestion(final String suggestion) {
            this.suggestion = suggestion;
        }

        @Override
        public @Nullable String description() {
            return null;
        }

        @Override
        public @NonNull String suggestion() {
            return this.suggestion;
        }

        @Override
        public @NonNull Suggestion withSuggestion(@NonNull final String suggestion) {
            return new SimpleSuggestion(suggestion);
        }
    }
}
