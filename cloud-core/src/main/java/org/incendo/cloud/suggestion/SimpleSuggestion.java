//
// MIT License
//
// Copyright (c) 2024 Incendo
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
package org.incendo.cloud.suggestion;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

final class SimpleSuggestion implements Suggestion {

    private final String suggestion;

    SimpleSuggestion(final @NonNull String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public @NonNull String suggestion() {
        return this.suggestion;
    }

    @Override
    public @NonNull Suggestion withSuggestion(final @NonNull String suggestion) {
        return new SimpleSuggestion(suggestion);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SimpleSuggestion that = (SimpleSuggestion) o;
        return Objects.equals(this.suggestion, that.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.suggestion);
    }

    @Override
    public String toString() {
        return this.suggestion;
    }
}
