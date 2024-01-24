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
package cloud.commandframework.execution;

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.suggestion.Suggestion;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Processor that operates on the {@link Stream stream} of {@link Suggestion suggestions} before it is collected
 * for the suggestion result passed to platform implementations or other callers.
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public interface CommandSuggestionProcessor<C> {

    /**
     * Creates a {@link CommandSuggestionProcessor} that simply returns the input suggestions.
     *
     * @param <C> command sender type
     * @return the processor
     */
    static <C> @NonNull CommandSuggestionProcessor<C> passThrough() {
        return (ctx, suggestions) -> suggestions;
    }

    /**
     * Adds operations to the {@link Suggestion suggestions} {@link Stream stream} and returns the result.
     *
     * @param context     command preprocessing context which can be used to access the command context and command input
     * @param suggestions the suggestions to process
     * @return the processed suggestions
     */
    @NonNull Stream<@NonNull Suggestion> process(
            @NonNull CommandPreprocessingContext<C> context,
            @NonNull Stream<@NonNull Suggestion> suggestions
    );

    /**
     * Create a chained {@link CommandSuggestionProcessor processor} that invokes {@code this} processor and then the
     * {@code nextProcessor} with the result.
     *
     * @param nextProcessor next suggestion processor
     * @return chained processor
     */
    default @NonNull CommandSuggestionProcessor<C> and(final @NonNull CommandSuggestionProcessor<C> nextProcessor) {
        Objects.requireNonNull(nextProcessor, "nextProcessor");
        return new ChainedCommandSuggestionProcessor<>(Arrays.asList(this, nextProcessor));
    }
}
