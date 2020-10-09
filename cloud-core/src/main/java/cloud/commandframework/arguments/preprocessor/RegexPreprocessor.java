//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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
package cloud.commandframework.arguments.preprocessor;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Command preprocessor that filters based on regular expressions
 *
 * @param <C> Command sender type
 */
public final class RegexPreprocessor<C> implements BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
        @NonNull ArgumentParseResult<Boolean>> {

    private final String rawPattern;
    private final Predicate<@NonNull String> predicate;

    private RegexPreprocessor(final @NonNull String pattern) {
        this.rawPattern = pattern;
        this.predicate = Pattern.compile(pattern).asPredicate();
    }

    /**
     * Create a new preprocessor
     *
     * @param pattern Regular expression
     * @param <C>     Command sender type
     * @return Preprocessor instance
     */
    public static <C> @NonNull RegexPreprocessor<C> of(final @NonNull String pattern) {
        return new RegexPreprocessor<>(pattern);
    }

    @Override
    public @NonNull ArgumentParseResult<Boolean> apply(
            @NonNull final CommandContext<C> context, @NonNull final Queue<@NonNull String> strings
    ) {
        final String head = strings.peek();
        if (head == null) {
            throw new NullPointerException("No input");
        }
        if (predicate.test(head)) {
            return ArgumentParseResult.success(true);
        }
        return ArgumentParseResult.failure(
                new RegexValidationException(
                        this.rawPattern,
                        head
                )
        );
    }


    /**
     * Exception thrown when input fails regex matching in {@link RegexPreprocessor}
     */
    public static final class RegexValidationException extends IllegalArgumentException {

        private final String pattern;
        private final String failedString;

        private RegexValidationException(
                @NonNull final String pattern,
                @NonNull final String failedString
        ) {
            this.pattern = pattern;
            this.failedString = failedString;
        }

        @Override
        public String getMessage() {
            return String.format(
                    "Input '%s' does not match the required pattern '%s'",
                    failedString,
                    pattern
            );
        }

        /**
         * Get the string that failed the verification
         *
         * @return Failed string
         */
        public @NonNull String getFailedString() {
            return this.failedString;
        }

        /**
         * Get the pattern that caused the string to fail
         *
         * @return Pattern
         */
        public @NonNull String getPattern() {
            return this.pattern;
        }

    }

}
