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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.util.StringUtils;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

@SuppressWarnings("unused")
@API(status = API.Status.STABLE)
public final class StringArgument<C> extends CommandArgument<C, String> {

    private static final Pattern QUOTED_DOUBLE = Pattern.compile("\"(?<inner>(?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern QUOTED_SINGLE = Pattern.compile("'(?<inner>(?:[^'\\\\]|\\\\.)*)'");

    private final StringMode stringMode;

    private StringArgument(
            final @NonNull String name,
            final @NonNull StringMode stringMode,
            final @NonNull SuggestionProvider<C> suggestionProvider,
            final @NonNull ArgumentDescription defaultDescription
    ) {
        super(name, new StringParser<>(stringMode, suggestionProvider),
                String.class, suggestionProvider, defaultDescription
        );
        this.stringMode = stringMode;
    }

    /**
     * Create a new {@link Builder}.
     *
     * @param name argument name
     * @param <C>  sender type
     * @return new {@link Builder}
     * @since 1.8.0
     */
    @API(status = API.Status.STABLE, since = "1.8.0")
    public static <C> @NonNull Builder<C> builder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     * @deprecated prefer {@link #builder(String)}
     */
    @API(status = API.Status.DEPRECATED, since = "1.8.0")
    @Deprecated
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return builder(name);
    }

    /**
     * Create a new required single string command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> of(final @NonNull String name) {
        return StringArgument.<C>builder(name).single().build();
    }

    /**
     * Create a new required command argument
     *
     * @param name       Argument name
     * @param stringMode String mode
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> of(
            final @NonNull String name,
            final @NonNull StringMode stringMode
    ) {
        return StringArgument.<C>builder(name).withMode(stringMode).build();
    }

    /**
     * Create a new required command argument with the 'single' parsing mode
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> single(final @NonNull String name) {
        return of(name, StringMode.SINGLE);
    }

    /**
     * Create a new required command argument with the 'greedy' parsing mode
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> greedy(final @NonNull String name) {
        return of(name, StringMode.GREEDY);
    }

    /**
     * Create a new required command argument with the 'greedy flag yielding' parsing mode
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     * @since 1.7.0
     */
    @API(status = API.Status.STABLE, since = "1.7.0")
    public static <C> @NonNull CommandArgument<C, String> greedyFlagYielding(final @NonNull String name) {
        return of(name, StringMode.GREEDY_FLAG_YIELDING);
    }

    /**
     * Create a new required command argument with the 'quoted' parsing mode
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> quoted(final @NonNull String name) {
        return of(name, StringMode.QUOTED);
    }

    /**
     * Get the string mode
     *
     * @return String mode
     */
    public @NonNull StringMode getStringMode() {
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
         * @since 1.7.0
         */
        @API(status = API.Status.STABLE, since = "1.7.0")
        GREEDY_FLAG_YIELDING
    }


    @API(status = API.Status.STABLE)
    public static final class Builder<C> extends CommandArgument.TypedBuilder<C, String, Builder<C>> {

        private StringMode stringMode = StringMode.SINGLE;
        private SuggestionProvider<C> suggestionProvider = (v1, v2) -> Collections.emptyList();

        private Builder(final @NonNull String name) {
            super(String.class, name);
        }

        /**
         * Set the String mode
         *
         * @param stringMode String mode to parse with
         * @return Builder instance
         */
        private @NonNull @This Builder<C> withMode(final @NonNull StringMode stringMode) {
            this.stringMode = stringMode;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C> greedy() {
            this.stringMode = StringMode.GREEDY;
            return this;
        }

        /**
         * Greedy string that will consume the input until a flag is present.
         *
         * @return Builder instance
         * @since 1.7.0
         */
        @API(status = API.Status.STABLE, since = "1.7.0")
        public @NonNull @This Builder<C> greedyFlagYielding() {
            this.stringMode = StringMode.GREEDY_FLAG_YIELDING;
            return this;
        }

        /**
         * Set the string mode to single
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C> single() {
            this.stringMode = StringMode.SINGLE;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull @This Builder<C> quoted() {
            this.stringMode = StringMode.QUOTED;
            return this;
        }

        /**
         * Set the suggestion provider
         *
         * @param suggestionProvider Suggestion provider
         * @return Builder instance
         */
        @Override
        public @NonNull @This Builder<C> withSuggestionProvider(
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            this.suggestionProvider = suggestionProvider;
            return this;
        }

        /**
         * Builder a new string argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull StringArgument<C> build() {
            return new StringArgument<>(this.getName(), this.stringMode,
                    this.suggestionProvider, this.getDefaultDescription()
            );
        }
    }


    @SuppressWarnings("UnnecessaryLambda")
    @API(status = API.Status.STABLE)
    public static final class StringParser<C> implements ArgumentParser<C, String> {

        private static final Pattern FLAG_PATTERN = Pattern.compile("(-[A-Za-z_\\-0-9])|(--[A-Za-z_\\-0-9]*)");

        private final StringMode stringMode;
        private final SuggestionProvider<C> suggestionProvider;

        /**
         * Construct a new string parser
         *
         * @param stringMode         String parsing mode
         * @param suggestionProvider Suggestion provider
         */
        public StringParser(
                final @NonNull StringMode stringMode,
                final @NonNull SuggestionProvider<C> suggestionProvider
        ) {
            this.stringMode = stringMode;
            this.suggestionProvider = suggestionProvider;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(
                        StringParser.class,
                        commandContext
                ));
            }

            if (this.stringMode == StringMode.SINGLE) {
                inputQueue.remove();
                return ArgumentParseResult.success(input);
            } else if (this.stringMode == StringMode.QUOTED) {
                return this.parseQuoted(commandContext, inputQueue);
            } else {
                return this.parseGreedy(commandContext, inputQueue);
            }
        }

        private @NonNull ArgumentParseResult<String> parseQuoted(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String peek = inputQueue.peek();
            if (peek != null && !peek.startsWith("'") && !peek.startsWith("\"")) {
                inputQueue.remove();
                return ArgumentParseResult.success(peek);
            }

            final StringJoiner sj = new StringJoiner(" ");
            for (final String string : inputQueue) {
                sj.add(string);
            }
            final String string = sj.toString();

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
                    inputQueue.remove();
                }
            } else {
                inner = inputQueue.peek();
                if (inner.startsWith("\"") || inner.startsWith("'")) {
                    return ArgumentParseResult.failure(new StringParseException(sj.toString(),
                            StringMode.QUOTED, commandContext
                    ));
                } else {
                    inputQueue.remove();
                }
            }

            inner = inner.replace("\\\"", "\"").replace("\\'", "'");

            return ArgumentParseResult.success(inner);
        }

        private @NonNull ArgumentParseResult<String> parseGreedy(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final StringJoiner sj = new StringJoiner(" ");
            final int size = inputQueue.size();

            for (int i = 0; i < size; i++) {
                final String string = inputQueue.peek();

                if (string == null) {
                    break;
                }

                if (this.stringMode == StringMode.GREEDY_FLAG_YIELDING) {
                    // The pattern requires a leading space.
                    if (FLAG_PATTERN.matcher(string).matches()) {
                        break;
                    }
                }

                sj.add(string);
                inputQueue.remove();
            }

            return ArgumentParseResult.success(sj.toString());
        }

        @Override
        public @NonNull List<@NonNull Suggestion> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.suggestionProvider.suggestions(commandContext, input);
        }

        @Override
        public boolean isContextFree() {
            return true;
        }

        /**
         * Get the string mode
         *
         * @return String mode
         */
        public @NonNull StringMode getStringMode() {
            return this.stringMode;
        }
    }


    @API(status = API.Status.STABLE)
    public static final class StringParseException extends ParserException {

        private static final long serialVersionUID = -8903115465005472945L;
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
                final @NonNull StringMode stringMode,
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
         * Get the input provided by the sender
         *
         * @return Input
         */
        public @NonNull String getInput() {
            return this.input;
        }

        /**
         * Get the string mode
         *
         * @return String mode
         */
        public @NonNull StringMode getStringMode() {
            return this.stringMode;
        }
    }
}
