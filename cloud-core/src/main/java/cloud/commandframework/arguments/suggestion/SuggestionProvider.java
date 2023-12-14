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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Provider of suggestions
 *
 * @param <C> command sender type
 * @since 2.0.0
 */
@API(status = API.Status.STABLE, since = "2.0.0")
@FunctionalInterface
public interface SuggestionProvider<C> {

    /**
     * Returns a suggestion provider that delegates to the {@code other} provider.
     *
     * @param <C>   the command sender type
     * @param other the other provider
     * @return the returned provider
     */
    static <C> @NonNull SuggestionProvider<C> delegating(final @NonNull SuggestionProvider<C> other) {
        return new DelegatingSuggestionProvider<>(other);
    }

    /**
     * Returns a future that completes with the suggestions for the given {@code input}.
     * <p>
     * If you don't need to return a future, you can implement {@link BlockingSuggestionProvider} instead.
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
            @NonNull CommandContext<C> context,
            @NonNull String input
    );

    /**
     * Get a suggestion provider that provides no suggestions.
     *
     * @param <C> sender type
     * @return suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    static <C> SuggestionProvider<C> noSuggestions() {
        return new NoSuggestions<C>() {
        };
    }

    /**
     * Utility method to simplify implementing {@link BlockingSuggestionProvider}
     * using a lambda, for methods that accept a {@link SuggestionProvider}.
     *
     * @param blockingSuggestionProvider suggestion provider
     * @param <C>                        sender type
     * @return suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    static <C> SuggestionProvider<C> blocking(final BlockingSuggestionProvider<C> blockingSuggestionProvider) {
        return blockingSuggestionProvider;
    }

    /**
     * Utility method to simplify implementing {@link BlockingSuggestionProvider.Strings}
     * using a lambda, for methods that accept a {@link SuggestionProvider}.
     *
     * @param blockingStringsSuggestionProvider suggestion provider
     * @param <C>                               sender type
     * @return suggestion provider
     * @since 2.0.0
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    static <C> SuggestionProvider<C> blockingStrings(
            final BlockingSuggestionProvider.Strings<C> blockingStringsSuggestionProvider
    ) {
        return blockingStringsSuggestionProvider;
    }

    @SuppressWarnings("FunctionalInterfaceMethodChanged")
    @FunctionalInterface
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

        @SuppressWarnings("FunctionalInterfaceMethodChanged")
        @FunctionalInterface
        interface Strings<C> extends BlockingSuggestionProvider<C> {

            /**
             * Returns a list of suggested arguments that would be correctly parsed by this parser
             * <p>
             * This method is likely to be called for every character provided by the sender and
             * so it may be necessary to cache results locally to prevent unnecessary computations
             *
             * @param commandContext Command context
             * @param input          Input string
             * @return List of suggestions
             * @since 2.0.0
             */
            @API(status = API.Status.STABLE, since = "2.0.0")
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

    interface NoSuggestions<C> extends SuggestionProvider<C> {

        @Override
        default @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
                final @NonNull CommandContext<C> context,
                final @NonNull String input
        ) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
