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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Command suggestion processor that checks the input queue head and filters based on that
 *
 * @param <C> Command sender type
 */
@API(status = API.Status.STABLE)
public final class FilteringCommandSuggestionProcessor<C> implements CommandSuggestionProcessor<C> {

    private final @NonNull BiPredicate<@NonNull String, @NonNull String> filter;
    private final boolean ignoreCase;

    /**
     * {@link BiPredicate} invoking {@link String#startsWith(String)}.
     *
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static final @NonNull BiPredicate<String, String> STARTS_WITH = String::startsWith;

    /**
     * {@link BiPredicate} invoking {@link String#contains(CharSequence)}.
     *
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static final @NonNull BiPredicate<String, String> CONTAINS = String::contains;

    /**
     * Create a new {@link FilteringCommandSuggestionProcessor} filtering with {@link String#startsWith(String)} that does
     * not ignore case.
     */
    @API(status = API.Status.STABLE)
    public FilteringCommandSuggestionProcessor() {
        this(STARTS_WITH, false);
    }

    /**
     * Create a new {@link FilteringCommandSuggestionProcessor}.
     *
     * <p>The first argument of the function is the potential suggestion, and the second is the
     * remaining unconsumed input (or empty string).</p>
     *
     * <p>If {@code ignoreCase} is true, the filter function will be provided lower cased strings.</p>
     *
     * @param filter     mode
     * @param ignoreCase whether to ignore case
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public FilteringCommandSuggestionProcessor(
            final @NonNull BiPredicate<@NonNull String, @NonNull String> filter,
            final boolean ignoreCase
    ) {
        this.filter = filter;
        this.ignoreCase = ignoreCase;
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
        final @Nullable String inputLower = this.ignoreCase ? input.toLowerCase(Locale.ENGLISH) : null;
        final List<String> suggestions = new LinkedList<>();
        for (final String suggestion : strings) {
            if (this.ignoreCase) {
                if (this.filter.test(suggestion.toLowerCase(Locale.ENGLISH), inputLower)) {
                    suggestions.add(suggestion);
                }
            } else {
                if (this.filter.test(suggestion, input)) {
                    suggestions.add(suggestion);
                }
            }
        }
        return suggestions;
    }
}
