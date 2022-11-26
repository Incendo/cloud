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
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

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
        this(startsWith(false));
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
            input = String.join(" ", context.getInputQueue());
        }
        final List<String> suggestions = new ArrayList<>(strings.size());
        for (final String suggestion : strings) {
            if (this.filter.test(context, suggestion, input)) {
                suggestions.add(suggestion);
            }
        }
        return suggestions;
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
    public static <C> Filter<C> startsWith(final boolean ignoreCase) {
        if (ignoreCase) {
            return (ctx, suggestion, input) -> suggestion
                    .toLowerCase(Locale.ROOT)
                    .startsWith(input.toLowerCase(Locale.ROOT));
        }
        return (ctx, suggestion, input) -> suggestion.startsWith(input);
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
    public static <C> Filter<C> contains(final boolean ignoreCase) {
        if (ignoreCase) {
            return (ctx, suggestion, input) -> suggestion.toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT));
        }
        return (ctx, suggestion, input) -> suggestion.contains(input);
    }

    /**
     * Filter function that tests each suggestion against the input and context.
     *
     * @param <C> sender type
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    @FunctionalInterface
    public interface Filter<C> {

        /**
         * Tests a potential suggestion against the input and context.
         *
         * @param context    context
         * @param suggestion potential suggestion
         * @param input      remaining unconsumed input
         * @return whether to accept the suggestion
         * @since 1.8.0
         */
        @API(status = API.Status.STABLE, since = "1.8.0")
        boolean test(CommandPreprocessingContext<C> context, String suggestion, String input);
    }
}
