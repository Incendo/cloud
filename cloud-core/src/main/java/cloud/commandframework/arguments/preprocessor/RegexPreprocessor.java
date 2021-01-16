//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
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
@SuppressWarnings("unused")
public final class RegexPreprocessor<C> implements BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
        @NonNull ArgumentParseResult<Boolean>> {

    private final String rawPattern;
    private final Predicate<@NonNull String> predicate;
    private final Caption failureCaption;

    private RegexPreprocessor(
            final @NonNull String pattern,
            final @NonNull Caption failureCaption
    ) {
        this.rawPattern = pattern;
        this.predicate = Pattern.compile(pattern).asPredicate();
        this.failureCaption = failureCaption;
    }

    /**
     * Create a new preprocessor using {@link cloud.commandframework.captions.StandardCaptionKeys#ARGUMENT_PARSE_FAILURE_REGEX}
     * as the failure caption
     *
     * @param pattern Regular expression
     * @param <C>     Command sender type
     * @return Preprocessor instance
     */
    public static <C> @NonNull RegexPreprocessor<C> of(final @NonNull String pattern) {
        return of(pattern, StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_REGEX);
    }

    /**
     * Create a new preprocessor
     *
     * @param pattern        Regular expression
     * @param <C>            Command sender type
     * @param failureCaption Caption sent when the input is invalid
     * @return Preprocessor instance
     */
    public static <C> @NonNull RegexPreprocessor<C> of(
            final @NonNull String pattern,
            final @NonNull Caption failureCaption
    ) {
        return new RegexPreprocessor<>(pattern, failureCaption);
    }

    @Override
    public @NonNull ArgumentParseResult<Boolean> apply(
            final @NonNull CommandContext<C> context, final @NonNull Queue<@NonNull String> strings
    ) {
        final String head = strings.peek();
        if (head == null) {
            return ArgumentParseResult.failure(new NoInputProvidedException(
                    RegexPreprocessor.class,
                    context
            ));
        }
        if (predicate.test(head)) {
            return ArgumentParseResult.success(true);
        }
        return ArgumentParseResult.failure(
                new RegexValidationException(
                        this.rawPattern,
                        head,
                        this.failureCaption,
                        context
                )
        );
    }


    /**
     * Exception thrown when input fails regex matching in {@link RegexPreprocessor}
     */
    public static final class RegexValidationException extends IllegalArgumentException {

        private static final long serialVersionUID = 747826566058072233L;
        private final String pattern;
        private final String failedString;
        private final Caption failureCaption;
        private final CommandContext<?> commandContext;

        private RegexValidationException(
                final @NonNull String pattern,
                final @NonNull String failedString,
                final @NonNull Caption failureCaption,
                final @NonNull CommandContext<?> commandContext
        ) {
            this.pattern = pattern;
            this.failedString = failedString;
            this.failureCaption = failureCaption;
            this.commandContext = commandContext;
        }

        @Override
        public String getMessage() {
            return this.commandContext.formatMessage(
                    this.failureCaption,
                    CaptionVariable.of(
                            "input",
                            this.failedString
                    ),
                    CaptionVariable.of(
                            "pattern",
                            this.pattern
                    )
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
