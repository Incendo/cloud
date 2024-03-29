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
package org.incendo.cloud.parser.standard;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.caption.StandardCaptionKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.util.StringUtils;

@SuppressWarnings("UnnecessaryLambda")
@API(status = API.Status.STABLE)
public final class StringParser<C> implements ArgumentParser<C, String> {

    private static final Pattern QUOTED_DOUBLE = Pattern.compile("\"(?<inner>(?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern QUOTED_SINGLE = Pattern.compile("'(?<inner>(?:[^'\\\\]|\\\\.)*)'");
    private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");

    /**
     * Creates a new string parser using the given {@code mode}.
     *
     * @param <C>  the command sender type
     * @param mode the parsing mode
     * @return the created parser
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, String> stringParser(final @NonNull StringMode mode) {
        return ParserDescriptor.of(new StringParser<>(mode), String.class);
    }

    /**
     * Creates a new string parser that parses a single string.
     *
     * @param <C> the command sender type
     * @return the created parser.
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, String> stringParser() {
        return stringParser(StringMode.SINGLE);
    }

    /**
     * Creates a new string parser that parses all the remaining input.
     *
     * @param <C> the command sender type
     * @return the created parser.
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, String> greedyStringParser() {
        return stringParser(StringMode.GREEDY);
    }

    /**
     * Creates a new string parser that parses all the remaining input until it encounters a command flag.
     *
     * @param <C> the command sender type
     * @return the created parser.
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, String> greedyFlagYieldingStringParser() {
        return stringParser(StringMode.GREEDY_FLAG_YIELDING);
    }

    /**
     * Creates a new string parser that parses a string surrounded by either single or double quotes, or a single string
     * if no quotes are found.
     *
     * @param <C> the command sender type
     * @return the created parser.
     */
    @API(status = API.Status.STABLE)
    public static <C> @NonNull ParserDescriptor<C, String> quotedStringParser() {
        return stringParser(StringMode.QUOTED);
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #stringComponent(StringMode)} as the parser.
     *
     * @param <C>  the command sender type
     * @param mode the parsing mode
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, String> stringComponent(final @NonNull StringMode mode) {
        return CommandComponent.<C, String>builder().parser(stringParser(mode));
    }

    /**
     * Returns a {@link CommandComponent.Builder} using {@link #stringComponent(StringMode)} with {@link StringMode#SINGLE}
     * as the parser.
     *
     * @param <C> the command sender type
     * @return the component builder
     */
    @API(status = API.Status.STABLE)
    public static <C> CommandComponent.@NonNull Builder<C, String> stringComponent() {
        return CommandComponent.<C, String>builder().parser(stringParser(StringMode.SINGLE));
    }

    private final StringMode stringMode;

    /**
     * Construct a new string parser
     *
     * @param stringMode         String parsing mode
     */
    public StringParser(
            final StringParser.@NonNull StringMode stringMode
    ) {
        this.stringMode = stringMode;
    }

    @Override
    public @NonNull ArgumentParseResult<String> parse(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        if (this.stringMode == StringMode.SINGLE) {
            return ArgumentParseResult.success(commandInput.readString());
        } else if (this.stringMode == StringMode.QUOTED) {
            return this.parseQuoted(commandContext, commandInput);
        } else {
            return this.parseGreedy(commandInput);
        }
    }

    private @NonNull ArgumentParseResult<String> parseQuoted(
            final @NonNull CommandContext<C> commandContext,
            final @NonNull CommandInput commandInput
    ) {
        final char peek = commandInput.peek();
        if (peek != '\'' && peek != '"') {
            return ArgumentParseResult.success(commandInput.readString());
        }

        final String string = commandInput.remainingInput();

        final Matcher doubleMatcher = QUOTED_DOUBLE.matcher(string);
        String doubleMatch = null;
        if (doubleMatcher.find()) {
            doubleMatch = doubleMatcher.group("inner");
        }
        final Matcher singleMatcher = QUOTED_SINGLE.matcher(string);
        String singleMatch = null;
        if (singleMatcher.find()) {
            singleMatch = singleMatcher.group("inner");
        }

        String inner = null;
        if (singleMatch != null && doubleMatch != null) {
            final int singleIndex = string.indexOf(singleMatch);
            final int doubleIndex = string.indexOf(doubleMatch);
            inner = doubleIndex < singleIndex ? doubleMatch : singleMatch;
        } else if (singleMatch == null && doubleMatch != null) {
            inner = doubleMatch;
        } else if (singleMatch != null) {
            inner = singleMatch;
        }

        if (inner != null) {
            final int numSpaces = StringUtils.countCharOccurrences(inner, ' ');
            for (int i = 0; i <= numSpaces; i++) {
                commandInput.readString();
            }
        } else {
            inner = commandInput.peekString();
            if (inner.startsWith("\"") || inner.startsWith("'")) {
                return ArgumentParseResult.failure(new StringParseException(
                        commandInput.remainingInput(),
                        StringMode.QUOTED, commandContext
                ));
            } else {
                commandInput.readString();
            }
        }

        inner = inner.replace("\\\"", "\"").replace("\\'", "'");

        return ArgumentParseResult.success(inner);
    }

    private @NonNull ArgumentParseResult<String> parseGreedy(
            final @NonNull CommandInput commandInput
    ) {
        final int size = commandInput.remainingTokens();
        final StringJoiner stringJoiner = new StringJoiner(" ");

        for (int i = 0; i < size; i++) {
            final String string = commandInput.peekString();

            if (string.isEmpty()) {
                break;
            }

            if (this.stringMode == StringMode.GREEDY_FLAG_YIELDING) {
                // The pattern requires a leading space.
                if (FLAG_PATTERN.matcher(string).matches()) {
                    break;
                }
            }

            stringJoiner.add(commandInput.readStringSkipWhitespace(false /* preserveSingleSpace */));
        }

        return ArgumentParseResult.success(stringJoiner.toString());
    }

    /**
     * Returns the string mode of the parser.
     *
     * @return string mode
     */
    public @NonNull StringMode stringMode() {
        return this.stringMode;
    }


    @API(status = API.Status.STABLE)
    public enum StringMode {
        SINGLE,
        QUOTED,
        GREEDY,
        /**
         * Greedy string that will consume the input until a flag is present.
         *
         */
        @API(status = API.Status.STABLE)
        GREEDY_FLAG_YIELDING
    }


    @API(status = API.Status.STABLE)
    public static final class StringParseException extends ParserException {

        private final String input;
        private final StringMode stringMode;

        /**
         * Construct a new string parse exception
         *
         * @param input      Input
         * @param stringMode String mode
         * @param context    Command context
         */
        public StringParseException(
                final @NonNull String input,
                final StringParser.@NonNull StringMode stringMode,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    StringParser.class,
                    context,
                    StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_STRING,
                    CaptionVariable.of("input", input),
                    CaptionVariable.of("stringMode", stringMode.name())
            );
            this.input = input;
            this.stringMode = stringMode;
        }


        /**
         * Returns the input provided by the sender.
         *
         * @return input
         */
        public @NonNull String input() {
            return this.input;
        }

        /**
         * Returns the string mode.
         *
         * @return string mode
         */
        public @NonNull StringMode stringMode() {
            return this.stringMode;
        }
    }
}
