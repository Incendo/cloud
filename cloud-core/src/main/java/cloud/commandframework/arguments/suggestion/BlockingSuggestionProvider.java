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

import cloud.commandframework.context.CommandContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Specialized variant of {@link SuggestionProvider} that does work on the calling thread.
 *
 * <p>In the case that a specific thread context isn't required, this is usually simpler
 * to implement than {@link SuggestionProvider}.</p>
 *
 * @param <C> command sender type
 */
@FunctionalInterface
@API(status = API.Status.STABLE, since = "2.0.0")
public
interface BlockingSuggestionProvider<C> extends SuggestionProvider<C> {

    /**
     * Returns the suggestions for the given {@code input}.
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    @NonNull List<@NonNull Suggestion> suggestions(@NonNull CommandContext<C> context, @NonNull String input);

    @Override
    default @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> context,
            final @NonNull String input
    ) {
        return CompletableFuture.completedFuture(this.suggestions(context, input));
    }

    /**
     * Specialized variant of {@link cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider} that has {@link String} results
     * instead of {@link Suggestion} results.
     *
     * <p>The provided default implementation of {@link #suggestions(CommandContext, String)}
     * maps the {@link String} results to {@link Suggestion suggestions} using {@link Suggestion#simple(String)}.</p>
     *
     * @param <C> command sender type
     */
    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Strings<C> extends cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider<C> {

        /**
         * Returns a list of suggested arguments that would be correctly parsed by this parser
         * <p>
         * This method is likely to be called for every character provided by the sender and
         * so it may be necessary to cache results locally to prevent unnecessary computations
         *
         * @param commandContext Command context
         * @param input          Input string
         * @return List of suggestions
         */
        @NonNull List<@NonNull String> stringSuggestions(
                @NonNull CommandContext<C> commandContext,
                @NonNull String input
        );

        @Override
        default @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> context,
                final @NonNull String input
        ) {
            return this.stringSuggestions(context, input).stream()
                    .map(Suggestion::simple)
                    .collect(Collectors.toList());
        }
    }
}
