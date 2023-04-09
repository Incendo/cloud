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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;


/**
 * A class containing information about completions
 * @since 1.9.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.9.0")
public interface Completion {

    /**
     * Creates a simple representation of the completion
     * @param completion The completion itself.
     * @return An instance of completion representing this string.
     */
    static @NonNull Completion of(@NonNull final String completion) {
        return new CompletionImpl(completion);
    }
    /**
     * Wraps multiple raw suggestions into a simple representation of the completion
     * @param suggestions The suggestions
     * @return A list with the instances of completions representing those raw suggestions.
     */
    static @NonNull List<@NonNull Completion> of(final @NonNull Iterable<@NonNull String> suggestions) {
        List<Completion> completion = new LinkedList<>();
        for (String raw: suggestions){
            completion.add(new CompletionImpl(raw));
        }
        return completion;
    }
    /**
     * Wraps multiple raw suggestions into a simple representation of the completion
     * @param suggestions The suggestions
     * @return A list with the instances of completions representing those raw suggestions.
     */
    static @NonNull List<@NonNull Completion> of(@NonNull final String @NonNull... suggestions) {
        ArrayList<Completion> completion = new ArrayList<>(suggestions.length);
        for (String raw: suggestions){
            completion.add(new CompletionImpl(raw));
        }
        return completion;
    }
    /**
     * Transforms multiple completions into raw suggestion
     * @param completions The completions
     * @return A list with suggestions
     */
    static @NonNull List<@NonNull String> raw(@NonNull final Iterable<@NonNull Completion> completions) {
        List<String> raw = new LinkedList<>();
        for (Completion completion : completions){
            raw.add(completion.suggestion());
        }
        return raw;
    }

    /**
     * Returns the raw suggestion which this completion contains.
     * @return a suggestion
     */
    @NonNull String suggestion();

    /**
     * Creates a new completion with given raw suggestion
     * @param suggestion new suggestion
     * @return a new instance with this suggestion
     */
    @NonNull Completion withSuggestion(@NonNull String suggestion);
}
