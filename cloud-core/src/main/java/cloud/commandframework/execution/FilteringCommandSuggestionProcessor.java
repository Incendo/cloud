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
package cloud.commandframework.execution;

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Command suggestion processor filters suggestions based on the remaining unconsumed input in the
 * queue.
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class FilteringCommandSuggestionProcessor<C> implements CommandSuggestionProcessor<C> {

    private final @NonNull Filter<C> filter;

    /**
     * Create a new {@link FilteringCommandSuggestionProcessor} filtering with {@link String#startsWith(String)} that does
     * not ignore case.
     */
    @API(status = API.Status.STABLE)
    public FilteringCommandSuggestionProcessor() {
        this(Filter.startsWith(false));
    }

    /**
     * Create a new {@link FilteringCommandSuggestionProcessor}.
     *
     * @param filter mode
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public FilteringCommandSuggestionProcessor(final @NonNull Filter<C> filter) {
        this.filter = filter;
    }

    @Override
    public @NonNull List<@NonNull String> apply(
            final @NonNull CommandPreprocessingContext<C> context,
            final @NonNull List<@NonNull String> strings
    ) {
        final String input;
        if (context.getInputQueue().isEmpty()) {
            input = "";
        } else {
            // Note: Pre-1.8.0 we used the queue head rather than remaining unconsumed input,
            // this is a behavioral change, but we've decided it's a fix. If you have a use case
            // where the old behavior was desired, we are open to PRs adding an option.
            input = String.join(" ", context.getInputQueue());
        }
        final List<String> suggestions = new ArrayList<>(strings.size());
        for (final String suggestion : strings) {
            final @Nullable String filtered = this.filter.filter(context, suggestion, input);
            if (filtered != null) {
                suggestions.add(filtered);
            }
        }
        return suggestions;
    }

    /**
     * Filter function that tests (and potentially changes) each suggestion against the input and context.
     *
     * @param <C> sender type
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    @FunctionalInterface
    public interface Filter<C> {

        /**
         * Filters a potential suggestion against the input and context.
         *
         * @param context    context
         * @param suggestion potential suggestion
         * @param input      remaining unconsumed input
         * @return possibly modified suggestion or null to deny
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
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
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
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
         * Returns a new {@link Filter} that tests this filter, and then
         * uses {@link #trimBeforeLastSpace()} if the result is non-null.
         *
         * @return combined filter
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        default Filter<C> andTrimBeforeLastSpace() {
            return this.and(trimBeforeLastSpace());
        }

        /**
         * Create a filter using {@link String#startsWith(String)} that can optionally ignore case.
         *
         * @param ignoreCase whether to ignore case
         * @param <C>        sender type
         * @return new filter
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
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
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        static <C> @NonNull Simple<C> contains(final boolean ignoreCase) {
            final BiPredicate<String, String> test = ignoreCase
                    ? (suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))
                    : String::contains;
            return Simple.contextFree(test);
        }

        /**
         * Create a filter which does extra processing when the input contains spaces.
         *
         * <p>Will return the portion of the suggestion which is after the last space in
         * the input.</p>
         *
         * @param <C> sender type
         * @return new filter
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        static <C> @NonNull Filter<C> trimBeforeLastSpace() {
            return (context, suggestion, input) -> {
                final int lastSpace = input.lastIndexOf(' ');
                // No spaces in input, don't do anything
                if (lastSpace == -1) {
                    return suggestion;
                }

                // Always use case-insensitive here. If case-sensitive filtering is desired it should
                // be done in another filter which this is appended to using #and/#andTrimBeforeLastSpace.
                if (suggestion.toLowerCase(Locale.ROOT).startsWith(input.toLowerCase(Locale.ROOT))) {
                    return suggestion.substring(lastSpace + 1);
                }

                return null;
            };
        }

        /**
         * Create a new context-free {@link Filter}.
         *
         * @param function function
         * @param <C>      sender type
         * @return filter
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
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
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        static <C> @NonNull Simple<C> simple(final Simple<C> filter) {
            return filter;
        }

        /**
         * Simple version of {@link Filter} which doesn't modify suggestions.
         *
         * <p>Returns boolean instead of nullable String.</p>
         *
         * @param <C> sender type
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        @FunctionalInterface
        interface Simple<C> extends Filter<C> {

            /**
             * Tests a suggestion against the context and input.
             *
             * @param context    context
             * @param suggestion potential suggestion
             * @param input      remaining unconsumed input
             * @return whether to accept the suggestion
             * @since 1.8.0
             */
            @API(status = API.Status.STABLE, since = "1.8.0")
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
             * @since 1.8.0
             */
            @API(status = API.Status.STABLE, since = "1.8.0")
            static <C> @NonNull Simple<C> contextFree(final @NonNull BiPredicate<String, String> test) {
                return (ctx, suggestion, input) -> test.test(suggestion, input);
            }
        }
    }
}
