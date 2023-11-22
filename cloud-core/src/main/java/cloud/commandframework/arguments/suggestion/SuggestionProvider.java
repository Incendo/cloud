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
import java.util.concurrent.CompletionException;
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
     * Returns the suggestions for the given {@code input}.
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    @NonNull List<@NonNull Suggestion> suggestions(@NonNull CommandContext<C> context, @NonNull String input);

    /**
     * Returns a future that completes with the suggestions for the given {@code input}. By default, this delegates to
     * {@link #suggestions(CommandContext, String)}.
     * <p>
     * If you want to implement this method for your parser it is recommended to implement
     * {@link FutureSuggestionProvider} rather than {@link SuggestionProvider}.
     *
     * @param context the context of the suggestion lookup
     * @param input   the current input
     * @return the suggestions
     */
    default @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
            @NonNull CommandContext<C> context,
            @NonNull String input
    ) {
        return CompletableFuture.completedFuture(this.suggestions(context, input));
    }


    /**
     * Utility interface extending {@link SuggestionProvider} to make it easier to implement
     * {@link #suggestionsFuture(CommandContext, String)}.
     *
     * @param <C> the command sender type
     */
    @API(status = API.Status.STABLE, since = "2.0.0")
    interface FutureSuggestionProvider<C> extends SuggestionProvider<C> {

        @Override
        default @NonNull List<@NonNull Suggestion> suggestions(
                @NonNull CommandContext<C> context,
                @NonNull String input
        ) {
            try {
                return this.suggestionsFuture(context, input).join();
            } catch (final CompletionException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw exception;
            }
        }

        @Override
        @NonNull CompletableFuture<@NonNull List<@NonNull Suggestion>> suggestionsFuture(
                @NonNull CommandContext<C> context,
                @NonNull String input
        );
    }
}
