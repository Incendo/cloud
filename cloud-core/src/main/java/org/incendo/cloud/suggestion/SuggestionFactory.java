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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;

/**
 * Factory that produces command suggestions from user input.
 *
 * @param <C> the command sender type
 * @param <S> the tooltip type
 */
@API(status = API.Status.STABLE)
public interface SuggestionFactory<C, S extends Suggestion> {

    /**
     * Returns command suggestions for the "next" argument that would yield a correctly
     * parsing command input
     *
     * @param context request context
     * @param input   input provided by the sender
     * @return the suggestions
     */
    @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(
            @NonNull CommandContext<C> context,
            @NonNull String input
    );

    /**
     * Returns command suggestions for the "next" argument that would yield a correctly
     * parsing command input
     *
     * @param sender the sender
     * @param input  input provided by the sender
     * @return the suggestions
     */
    @NonNull CompletableFuture<@NonNull Suggestions<C, S>> suggest(
            @NonNull C sender,
            @NonNull String input
    );

    /**
     * Returns command suggestions for the "next" argument that would yield a correctly
     * parsing command input
     *
     * @param sender the sender
     * @param input  input provided by the sender
     * @return the suggestions
     */
    default @NonNull Suggestions<C, S> suggestImmediately(
            final @NonNull C sender,
            final @NonNull String input
    ) {
        try {
            return this.suggest(sender, input).join();
        } catch (final CompletionException completionException) {
            final Throwable cause = completionException.getCause();
            // We unwrap if we can, otherwise we don't. There's no point in wrapping again.
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            throw completionException;
        }
    }

    /**
     * Returns a new suggestion factory that maps the results of {@code this} factory to the type {@link S2} using the
     * given {@code mapper}.
     *
     * @param <S2>   the new suggestion type
     * @param mapper the suggestion mapper
     * @return the mapped factory
     */
    default <S2 extends Suggestion> @NonNull SuggestionFactory<C, S2> mapped(final @NonNull SuggestionMapper<S2> mapper) {
        return new MappingSuggestionFactory<>(this, mapper);
    }
}
