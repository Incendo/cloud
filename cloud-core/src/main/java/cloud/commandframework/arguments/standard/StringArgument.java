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
package cloud.commandframework.arguments.standard;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.captions.StandardCaptionKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.ParserException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public final class StringArgument<C> extends CommandArgument<C, String> {

    private final StringMode stringMode;

    private StringArgument(
            final boolean required,
            final @NonNull String name,
            final @NonNull StringMode stringMode,
            final @NonNull String defaultValue,
            final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String,
                    @NonNull List<@NonNull String>> suggestionsProvider
    ) {
        super(required, name, new StringParser<>(stringMode, suggestionsProvider),
                defaultValue, String.class, suggestionsProvider
        );
        this.stringMode = stringMode;
    }

    /**
     * Create a new builder
     *
     * @param name Name of the argument
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> StringArgument.@NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new StringArgument.Builder<>(name);
    }

    /**
     * Create a new required single string command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> of(final @NonNull String name) {
        return StringArgument.<C>newBuilder(name).single().asRequired().build();
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
        return StringArgument.<C>newBuilder(name).withMode(stringMode).asRequired().build();
    }

    /**
     * Create a new optional single string command argument
     *
     * @param name Argument name
     * @param <C>  Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> optional(final @NonNull String name) {
        return StringArgument.<C>newBuilder(name).single().asOptional().build();
    }

    /**
     * Create a new optional command argument
     *
     * @param name       Argument name
     * @param stringMode String mode
     * @param <C>        Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> optional(
            final @NonNull String name,
            final @NonNull StringMode stringMode
    ) {
        return StringArgument.<C>newBuilder(name).withMode(stringMode).asOptional().build();
    }

    /**
     * Create a new required command argument with a default value
     *
     * @param name          Argument name
     * @param defaultString Default string
     * @param <C>           Command sender type
     * @return Created argument
     */
    public static <C> @NonNull CommandArgument<C, String> optional(
            final @NonNull String name,
            final @NonNull String defaultString
    ) {
        return StringArgument.<C>newBuilder(name).asOptionalWithDefault(defaultString).build();
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


    public enum StringMode {
        SINGLE,
        GREEDY,
        QUOTED
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, String> {

        private StringMode stringMode = StringMode.SINGLE;
        private BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider = (v1, v2) -> Collections.emptyList();

        protected Builder(final @NonNull String name) {
            super(String.class, name);
        }

        /**
         * Set the String mode
         *
         * @param stringMode String mode to parse with
         * @return Builder instance
         */
        private @NonNull Builder<C> withMode(final @NonNull StringMode stringMode) {
            this.stringMode = stringMode;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull Builder<C> greedy() {
            this.stringMode = StringMode.GREEDY;
            return this;
        }

        /**
         * Set the string mode to single
         *
         * @return Builder instance
         */
        public @NonNull Builder<C> single() {
            this.stringMode = StringMode.SINGLE;
            return this;
        }

        /**
         * Set the string mode to greedy
         *
         * @return Builder instance
         */
        public @NonNull Builder<C> quoted() {
            this.stringMode = StringMode.QUOTED;
            return this;
        }

        /**
         * Set the suggestions provider
         *
         * @param suggestionsProvider Suggestions provider
         * @return Builder instance
         */
        public @NonNull Builder<C> withSuggestionsProvider(
                final @NonNull BiFunction<@NonNull CommandContext<C>,
                        @NonNull String, @NonNull List<@NonNull String>> suggestionsProvider
        ) {
            this.suggestionsProvider = suggestionsProvider;
            return this;
        }

        /**
         * Builder a new string argument
         *
         * @return Constructed argument
         */
        @Override
        public @NonNull StringArgument<C> build() {
            return new StringArgument<>(this.isRequired(), this.getName(), this.stringMode,
                    this.getDefaultValue(), this.suggestionsProvider
            );
        }

    }


    public static final class StringParser<C> implements ArgumentParser<C, String> {

        private final StringMode stringMode;
        private final BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider;

        /**
         * Construct a new string parser
         *
         * @param stringMode          String parsing mode
         * @param suggestionsProvider Suggestions provider
         */
        public StringParser(
                final @NonNull StringMode stringMode,
                final @NonNull BiFunction<@NonNull CommandContext<C>, @NonNull String,
                        @NonNull List<@NonNull String>> suggestionsProvider
        ) {
            this.stringMode = stringMode;
            this.suggestionsProvider = suggestionsProvider;
        }

        @Override
        public @NonNull ArgumentParseResult<String> parse(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull Queue<@NonNull String> inputQueue
        ) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }

            if (this.stringMode == StringMode.SINGLE) {
                if (commandContext.isSuggestions()) {
                    final List<String> suggestions = this.suggestionsProvider.apply(commandContext, inputQueue.peek());
                    if (!suggestions.isEmpty() && !suggestions.contains(input)) {
                        return ArgumentParseResult.failure(new IllegalArgumentException(
                                String.format("'%s' is not one of: %s", input, String.join(", ", suggestions))
                        ));
                    }
                }
                inputQueue.remove();
                return ArgumentParseResult.success(input);
            }

            final StringJoiner sj = new StringJoiner(" ");
            final int size = inputQueue.size();

            boolean started = false;
            boolean finished = false;

            char start = ' ';
            for (int i = 0; i < size; i++) {
                String string = inputQueue.peek();

                if (string == null) {
                    break;
                }

                if (this.stringMode == StringMode.QUOTED) {
                    if (!started) {
                        if (string.startsWith("\"") || string.startsWith("'")) {
                            start = string.charAt(0);
                            string = string.substring(1);
                            started = true;
                        } else {
                            /* Just read a single string instead */
                            inputQueue.remove();
                            return ArgumentParseResult.success(string);
                        }
                    } else if (string.endsWith(Character.toString(start))) {
                        sj.add(string.substring(0, string.length() - 1));
                        inputQueue.remove();
                        finished = true;
                        break;
                    }
                }

                sj.add(string);
                inputQueue.remove();
            }

            if (this.stringMode == StringMode.QUOTED && !finished) {
                return ArgumentParseResult.failure(new StringParseException(sj.toString(), StringMode.QUOTED, commandContext));
            }

            return ArgumentParseResult.success(sj.toString());
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(
                final @NonNull CommandContext<C> commandContext,
                final @NonNull String input
        ) {
            return this.suggestionsProvider.apply(commandContext, input);
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
