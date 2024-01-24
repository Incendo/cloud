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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.execution.preprocessor.CommandPreprocessingContext;
import org.incendo.cloud.internal.CommandInputTokenizer;

/**
 * Command suggestion processor filters suggestions based on the remaining unconsumed input in the
 * queue.
 *
 * @param <C> command sender type
 */
@API(status = API.Status.STABLE)
public final class FilteringSuggestionProcessor<C> implements SuggestionProcessor<C> {

    private final @NonNull Filter<C> filter;

    /**
     * Create a new {@link FilteringSuggestionProcessor} filtering with {@link Filter#partialTokenMatches(boolean)} that
     * ignores case.
     */
    @API(status = API.Status.STABLE)
    public FilteringSuggestionProcessor() {
        this(Filter.partialTokenMatches(true));
    }

    /**
     * Create a new {@link FilteringSuggestionProcessor}.
     *
     * @param filter mode
     */
    @API(status = API.Status.STABLE)
    public FilteringSuggestionProcessor(final @NonNull Filter<C> filter) {
        this.filter = filter;
    }

    @Override
    public @NonNull Stream<@NonNull Suggestion> process(
            final @NonNull CommandPreprocessingContext<C> context,
            final @NonNull Stream<@NonNull Suggestion> suggestions
    ) {
        final String input;
        if (context.commandInput().isEmpty(true /* ignoreWhitespace */)) {
            input = "";
        } else {
            input = context.commandInput().skipWhitespace().remainingInput();
        }
        return suggestions.map(suggestion -> {
            final String filtered = this.filter.filter(context, suggestion.suggestion(), input);
            if (filtered == null) {
                return null;
            }
            return suggestion.withSuggestion(filtered);
        }).filter(Objects::nonNull);
    }

    /**
     * Filter function that tests (and potentially changes) each suggestion against the input and context.
     *
     * @param <C> command sender type
     */
    @API(status = API.Status.STABLE)
    @FunctionalInterface
    public interface Filter<C> {

        /**
         * Filters a potential suggestion against the input and context.
         *
         * @param context    context
         * @param suggestion potential suggestion
         * @param input      remaining unconsumed input
         * @return possibly modified suggestion or null to deny
         */
        @API(status = API.Status.STABLE)
        @Nullable String filter(
                @NonNull CommandPreprocessingContext<C> context,
                @NonNull String suggestion,
                @NonNull String input
        );

        /**
         * Returns a new {@link Filter} which tests this filter, and if the result
         * is non-null, then filters with {@code and}.
         *
         * @param and next filter
         * @return combined filter
         */
        @API(status = API.Status.STABLE)
        default @NonNull Filter<C> and(final @NonNull Filter<C> and) {
            return (ctx, suggestion, input) -> {
                final @Nullable String filtered = this.filter(ctx, suggestion, input);
                if (filtered == null) {
                    return null;
                }
                return and.filter(ctx, filtered, input);
            };
        }

        /**
         * Create a filter using {@link String#startsWith(String)} that can optionally ignore case.
         *
         * @param ignoreCase whether to ignore case
         * @param <C>        sender type
         * @return new filter
         */
        @API(status = API.Status.STABLE)
        static <C> @NonNull Simple<C> startsWith(final boolean ignoreCase) {
            final BiPredicate<String, String> test = ignoreCase
                    ? (suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))
                    : String::startsWith;
            return Simple.contextFree(test);
        }

        /**
         * Create a filter using {@link String#contains(CharSequence)} that can optionally ignore case.
         *
         * @param ignoreCase whether to ignore case
         * @param <C>        sender type
         * @return new filter
         */
        @API(status = API.Status.STABLE)
        static <C> @NonNull Simple<C> contains(final boolean ignoreCase) {
            final BiPredicate<String, String> test = ignoreCase
                    ? (suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))
                    : String::contains;
            return Simple.contextFree(test);
        }

        /**
         * Filter that requires every token of input to be a partial or full match for a single corresponding token in the
         * suggestion.
         *
         * @param ignoreCase whether to ignore case
         * @param <C>        command sender type
         * @return new filter
         */
        @API(status = API.Status.STABLE)
        static <C> @NonNull Simple<C> partialTokenMatches(final boolean ignoreCase) {
            return Simple.contextFree((suggestion, input) -> {
                final List<String> suggestionTokens = new CommandInputTokenizer(suggestion).tokenize();
                final List<String> inputTokens = new CommandInputTokenizer(input).tokenize();

                boolean passed = true;

                for (String inputToken : inputTokens) {
                    if (ignoreCase) {
                        inputToken = inputToken.toLowerCase(Locale.ROOT);
                    }

                    boolean foundMatch = false;

                    for (final Iterator<String> iterator = suggestionTokens.iterator(); iterator.hasNext();) {
                        final String suggestionToken = iterator.next();
                        final String suggestionTokenLower =
                                ignoreCase ? suggestionToken.toLowerCase(Locale.ROOT) : suggestionToken;
                        if (suggestionTokenLower.contains(inputToken)) {
                            iterator.remove();
                            foundMatch = true;
                            break;
                        }
                    }

                    if (!foundMatch) {
                        passed = false;
                        break;
                    }
                }

                return passed;
            });
        }

        /**
         * Create a new context-free {@link Filter}.
         *
         * @param function function
         * @param <C>      sender type
         * @return filter
         */
        @API(status = API.Status.STABLE)
        static <C> @NonNull Filter<C> contextFree(final @NonNull BiFunction<String, String, @Nullable String> function) {
            return (ctx, suggestion, input) -> function.apply(suggestion, input);
        }

        /**
         * Create a new {@link Simple}. This is a convenience method to allow
         * for more easily implementing {@link Simple} using a lambda without
         * casting, for methods which accept {@link Filter}.
         *
         * @param filter filter lambda
         * @param <C>    sender type
         * @return new simple filter
         */
        @API(status = API.Status.STABLE)
        static <C> @NonNull Simple<C> simple(final Simple<C> filter) {
            return filter;
        }

        /**
         * Simple version of {@link Filter} which doesn't modify suggestions.
         *
         * <p>Returns boolean instead of nullable String.</p>
         *
         * @param <C> command sender type
         */
        @API(status = API.Status.STABLE)
        @FunctionalInterface
        interface Simple<C> extends Filter<C> {

            /**
             * Tests a suggestion against the context and input.
             *
             * @param context    context
             * @param suggestion potential suggestion
             * @param input      remaining unconsumed input
             * @return whether to accept the suggestion
             */
            @API(status = API.Status.STABLE)
            boolean test(
                    @NonNull CommandPreprocessingContext<C> context,
                    @NonNull String suggestion,
                    @NonNull String input
            );

            @Override
            @SuppressWarnings("FunctionalInterfaceMethodChanged")
            default @Nullable String filter(
                    @NonNull CommandPreprocessingContext<C> context,
                    @NonNull String suggestion,
                    @NonNull String input
            ) {
                if (this.test(context, suggestion, input)) {
                    return suggestion;
                }
                return null;
            }

            /**
             * Create a new context-free {@link Simple}.
             *
             * @param test predicate
             * @param <C>  sender type
             * @return simple filter
             */
            @API(status = API.Status.STABLE)
            static <C> @NonNull Simple<C> contextFree(final @NonNull BiPredicate<String, String> test) {
                return (ctx, suggestion, input) -> test.test(suggestion, input);
            }
        }
    }
}
