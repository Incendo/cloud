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
import cloud.commandframework.context.CommandInput;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
     * <p>The {@code input} parameter contains all sender-provided input that has not yet been consumed by the argument parsers.
     * If the component that the suggestion provider is generating suggestions for consumes multiple tokens the suggestion
     * provider might receive a {@link CommandInput} instance containing multiple tokens.
     * {@link CommandInput#lastRemainingToken()} may be used to extract the part of the command that is currently being
     * completed by the command sender.</p>
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    @NonNull Iterable<@NonNull Suggestion> suggestions(@NonNull CommandContext<C> context, @NonNull CommandInput input);

    @Override
    default @NonNull CompletableFuture<@NonNull Iterable<@NonNull Suggestion>> suggestionsFuture(
            final @NonNull CommandContext<C> context,
            final @NonNull CommandInput input
    ) {
        return CompletableFuture.completedFuture(this.suggestions(context, input));
    }

    /**
     * Specialized variant of {@link cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider} that has {@link String} results
     * instead of {@link Suggestion} results.
     *
     * <p>The provided default implementation of {@link #suggestions(CommandContext, CommandInput)}
     * maps the {@link String} results to {@link Suggestion suggestions} using {@link Suggestion#simple(String)}.</p>
     *
     * @param <C> command sender type
     */
    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface Strings<C> extends cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider<C> {

        /**
         * Returns the suggestions for the given {@code input}.
         *
         * <p>The {@code input} parameter contains all sender-provided input that has not yet been consumed by the argument parsers.
         * If the component that the suggestion provider is generating suggestions for consumes multiple tokens the suggestion
         * provider might receive a {@link CommandInput} instance containing multiple tokens.
         * {@link CommandInput#lastRemainingToken()} may be used to extract the part of the command that is currently being
         * completed by the command sender.</p>
         *
         * @param commandContext Command context
         * @param input          Input string
         * @return List of suggestions
         */
        @NonNull Iterable<@NonNull String> stringSuggestions(
                @NonNull CommandContext<C> commandContext,
                @NonNull CommandInput input
        );

        @Override
        default @NonNull Iterable<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> context,
                final @NonNull CommandInput input
        ) {
            return StreamSupport.stream(this.stringSuggestions(context, input).spliterator(), false)
                    .map(Suggestion::simple)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Specialized variant of {@link cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider} that has {@link String} results
     * instead of {@link Suggestion} results.
     *
     * <p>The provided default implementation of {@link #suggestions(CommandContext, CommandInput)}
     * maps the {@link String} results to {@link Suggestion suggestions} using {@link Suggestion#simple(String)}.</p>
     *
     * @param <C> command sender type
     */
    @FunctionalInterface
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface ConstantStrings<C> extends cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider<C> {

        /**
         * Returns the suggestions for the given {@code input}.
         *
         * @return list of suggestions
         */
        @NonNull Iterable<@NonNull String> stringSuggestions();

        @Override
        default @NonNull Iterable<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> context,
                final @NonNull CommandInput input
        ) {
            return StreamSupport.stream(this.stringSuggestions().spliterator(), false)
                    .map(Suggestion::simple)
                    .collect(Collectors.toList());
        }
    }
}
