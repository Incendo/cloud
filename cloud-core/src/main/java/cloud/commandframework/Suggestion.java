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
package cloud.commandframework;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;


/**
 * A class containing information about suggestions
 */
public final class Suggestion {
    private final @NonNull String suggestion;
    private final @Nullable String description;


    /**
     * A simple constructor providing only basic information
     * @param suggestion the suggestion itself
     */
    public Suggestion(@NonNull final String suggestion) {
        this(suggestion, null);
    }

    /**
     * A constructor providing additional description to the suggestion
     * @param suggestion the suggestion itself
     * @param description the description of the suggestion
     */
    public Suggestion(@NonNull final String suggestion, @Nullable final String description) {
        this.suggestion = Objects.requireNonNull(suggestion, "The suggestion shouldn't be null");
        this.description = description;
    }

    /**
     * Returns the description of the suggestion.
     * @return the description of the suggestion.
     */
    public @Nullable String description() {
        return this.description;
    }

    /**
     * Returns the suggestion itself.
     * @return the suggestion itself.
     */
    public @NonNull String suggestion() {
        return this.suggestion;
    }
}
