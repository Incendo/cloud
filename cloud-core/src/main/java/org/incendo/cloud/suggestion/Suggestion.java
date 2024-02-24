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

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.STABLE)
public interface Suggestion {

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
