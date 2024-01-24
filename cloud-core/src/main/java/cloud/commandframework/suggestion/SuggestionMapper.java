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
package cloud.commandframework.suggestion;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Maps from {@link Suggestion} to {@link S}.
 *
 * @param <S> the suggestion type
 * @since 2.0.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface SuggestionMapper<S extends Suggestion> {

    /**
     * Returns a suggestion mapper that maps from {@link Suggestion} to {@link Suggestion}.
     *
     * @return the identity mapper
     */
    static @NonNull SuggestionMapper<Suggestion> identity() {
        return suggestion -> suggestion;
    }

    /**
     * Maps the suggestion to the responding suggestion of type {@link S}.
     *
     * @param suggestion the input suggestion
     * @return the output suggestion
     */
    @NonNull S map(@NonNull Suggestion suggestion);
}
